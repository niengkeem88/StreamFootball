package com.matchpulse.live

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.matchpulse.live.core.ads.AdMobManager
import com.matchpulse.live.core.ads.BannerAd
import com.matchpulse.live.core.ads.InterstitialAdManager
import com.matchpulse.live.core.datastore.UserSettings
import com.matchpulse.live.core.design.theme.MatchPulseTheme
import com.matchpulse.live.core.navigation.Routes
import com.matchpulse.live.core.navigation.bottomTabs
import com.matchpulse.live.feature.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @Inject lateinit var adMobManager: AdMobManager
    @Inject lateinit var interstitialAdManager: InterstitialAdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        adMobManager.gatherConsentAndInitialize(this)

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()

            MatchPulseTheme(darkTheme = settings.darkMode) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    when {
                        !settings.onboardingCompleted -> OnboardingScreen(viewModel, adMobManager)
                        !settings.termsAccepted -> TermsScreen(viewModel, adMobManager)
                        else -> MainApp(viewModel, adMobManager, interstitialAdManager)
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen(viewModel: MainViewModel, adMobManager: AdMobManager) {
    val config = adMobManager.adConfig()
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Welcome to MatchPulse Live", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("Real-time football scores powered by ScoreBat.", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(32.dp))
            Button(onClick = { viewModel.completeOnboarding() }, modifier = Modifier.fillMaxWidth()) { Text("Get Started") }
        }
        if (config.enabled && config.bannerId.isNotBlank()) {
            BannerAd(adUnitId = config.bannerId, modifier = Modifier.padding(top = 8.dp).alpha(0.6f))
        }
    }
}

@Composable
fun TermsScreen(viewModel: MainViewModel, adMobManager: AdMobManager) {
    val config = adMobManager.adConfig()
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Terms of Service", style = MaterialTheme.typography.headlineMedium)
            Text(termsText(), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { viewModel.acceptTerms() }, modifier = Modifier.fillMaxWidth()) { Text("Accept & Continue") }
        }
        if (config.enabled && config.bannerId.isNotBlank()) {
            BannerAd(adUnitId = config.bannerId, modifier = Modifier.padding(top = 8.dp).alpha(0.6f))
        }
    }
}

@Composable
fun MainApp(
    viewModel: MainViewModel,
    adMobManager: AdMobManager,
    interstitialAdManager: InterstitialAdManager,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.Home
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current as android.app.Activity
    val adConfig = adMobManager.adConfig()

    // Track navigation for frequency caps
    LaunchedEffect(currentRoute) {
        viewModel.recordNavigation()
    }

    // Show interstitial when navigating away from Home (with frequency checking)
    val showInterstitial: (String) -> Unit = { targetRoute ->
        scope.launch {
            viewModel.canShowInterstitial().let { canShow ->
                if (canShow && adConfig.enabled && adConfig.interstitialId.isNotBlank()) {
                    interstitialAdManager.show(activity, adConfig.interstitialId) {
                        viewModel.recordInterstitialShown()
                    }
                }
            }
            navController.navigate(targetRoute) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            if (currentRoute != tab.route) {
                                showInterstitial(tab.route)
                            }
                        },
                        icon = {},
                        label = { Text(tab.label) },
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.Home) {
                HomeScreen(adMobManager)
            }
            composable(Routes.Settings) {
                SettingsScreen(navController, viewModel, settings)
            }
            composable(Routes.About) { InfoPage("About", aboutText()) }
            composable(Routes.Privacy) { InfoPage("Privacy", privacyText()) }
            composable(Routes.TermsPage) { InfoPage("Terms", termsText()) }
        }
    }
}

@Composable
fun HomeScreen(adMobManager: AdMobManager) {
    val config = adMobManager.adConfig()
    val token = "YOUR_SCOREBAT_TOKEN"

    Column(modifier = Modifier.fillMaxSize()) {
        ScoreBatWidget(token = token, modifier = Modifier.weight(1f))

        if (config.enabled && config.bannerId.isNotBlank()) {
            BannerAd(adUnitId = config.bannerId)
        }
    }
}

@Composable
fun ScoreBatWidget(token: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val webView = remember {
        WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadsImagesAutomatically = true
                useWideViewPort = true
                loadWithOverviewMode = true
                allowFileAccess = false
                allowContentAccess = false
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }
            isHorizontalScrollBarEnabled = false
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }
    }

    DisposableEffect(webView) {
        onDispose {
            try {
                webView.stopLoading()
                webView.loadUrl("about:blank")
                webView.removeAllViews()
                webView.destroy()
            } catch (_: Exception) {}
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier,
    ) { view ->
        view.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }
        view.loadUrl("https://www.scorebat.com/embed/livescore/?token=$token&theme=dark&lang=en")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController, viewModel: MainViewModel, settings: UserSettings) {
    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Display", style = MaterialTheme.typography.titleMedium)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Dark Mode")
                        Switch(checked = settings.darkMode, onCheckedChange = { viewModel.toggleDarkMode(it) })
                    }
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("App Info", style = MaterialTheme.typography.titleMedium)
                    Text("MatchPulse Live v1.0.1", style = MaterialTheme.typography.bodyMedium)
                    Text("Powered by ScoreBat", style = MaterialTheme.typography.bodySmall)
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Legal", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { navController.navigate(Routes.About) }, modifier = Modifier.fillMaxWidth()) { Text("About") }
                    Button(onClick = { navController.navigate(Routes.Privacy) }, modifier = Modifier.fillMaxWidth()) { Text("Privacy Policy") }
                    Button(onClick = { navController.navigate(Routes.TermsPage) }, modifier = Modifier.fillMaxWidth()) { Text("Terms of Service") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoPage(title: String, body: String) {
    Scaffold(topBar = { TopAppBar(title = { Text(title) }) }) { padding ->
        Text(body, modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), style = MaterialTheme.typography.bodyLarge)
    }
}

private fun aboutText(): String = """MatchPulse Live provides football live scores powered by ScoreBat.

Version: 1.0.1"""

private fun privacyText(): String = """MatchPulse Live stores local preferences on your device.

If ads are enabled, Google Mobile Ads may process advertising data according to Google's policies.

MatchPulse Live does not collect, store, or transmit personal data."""

private fun termsText(): String = """1. Acceptance of Terms
By using MatchPulse Live, you agree to use the app responsibly.

2. Use License
The app is for personal football companion use.

3. Football Data
Scores and fixtures are provided by ScoreBat.

4. Advertising
Ads may appear when enabled and configured.

5. Privacy
Local preferences remain on the device.

6. Changes
Terms may be updated as the product evolves."""
