package com.matchpulse.live

import android.os.Bundle
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import android.webkit.WebView
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip

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
                        !settings.onboardingCompleted -> OnboardingFlow(viewModel, adMobManager, interstitialAdManager)
                        else -> MainApp(viewModel, adMobManager, interstitialAdManager)
                    }
                }
            }
        }
    }
}

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String,
)

private val onboardingPages = listOf(
    OnboardingPage(
        emoji = "\u26BD",
        title = "Welcome to MatchPulse",
        description = "Your ultimate companion for live football scores, match updates, and streaming. Never miss a moment of the action!",
    ),
    OnboardingPage(
        emoji = "\uD83D\uDCF1",
        title = "Live Scores in Real-Time",
        description = "Follow hundreds of leagues and competitions from around the world. Instant updates as goals happen.",
    ),
    OnboardingPage(
        emoji = "\uD83D\uDD14",
        title = "Instant Notifications",
        description = "Get alerted for goals, red cards, match starts, and final results. Stay connected to the game wherever you are.",
    ),
    OnboardingPage(
        emoji = "\uD83D\uDC4B",
        title = "Terms of Service",
        description = "By using MatchPulse, you agree to our terms. Your privacy matters - we only store your preferences locally. Ads help keep the app free.",
    ),
    OnboardingPage(
        emoji = "\uD83C\uDF1F",
        title = "You're All Set!",
        description = "Start exploring live scores, follow your favorite teams, and enjoy the beautiful game with MatchPulse.",
    ),
)

@Composable
fun OnboardingFlow(
    viewModel: MainViewModel,
    adMobManager: AdMobManager,
    interstitialAdManager: InterstitialAdManager,
) {
    val config = adMobManager.adConfig()
    val activity = LocalContext.current as? ComponentActivity
    var currentPage by remember { mutableIntStateOf(0) }
    var showInterstitial by remember { mutableStateOf(false) }
    var pendingPage by remember { mutableIntStateOf(-1) }
    val coroutineScope = rememberCoroutineScope()

    // Handle interstitial ad callback
    LaunchedEffect(showInterstitial) {
        if (showInterstitial && pendingPage >= 0 && activity != null) {
            showInterstitial = false
            interstitialAdManager.show(
                activity = activity,
                adUnitId = config.interstitialId,
                onDismissed = {
                    currentPage = pendingPage
                    pendingPage = -1
                }
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
    ) {
        // Main content area (weight 1f to push banner to bottom)
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val page = onboardingPages[currentPage]

            Spacer(Modifier.height(24.dp))

            // Emoji/Illustration
            Text(
                text = page.emoji,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(16.dp),
            )

            Spacer(Modifier.height(16.dp))

            // Title
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 32.dp),
            )

            Spacer(Modifier.height(12.dp))

            // Description
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 32.dp),
            )

            Spacer(Modifier.height(24.dp))

            // Page indicator dots
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                onboardingPages.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (index == currentPage) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentPage) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Next / Get Started button
            Button(
                onClick = {
                    if (currentPage < onboardingPages.size - 1) {
                        if (config.enabled && config.interstitialId.isNotBlank()) {
                            pendingPage = currentPage + 1
                            showInterstitial = true
                        } else {
                            currentPage++
                        }
                    } else {
                        coroutineScope.launch {
                            viewModel.completeOnboarding()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(50.dp),
            ) {
                Text(
                    if (currentPage < onboardingPages.size - 1) "Next" else "Get Started",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            // Skip button (only on non-last pages)
            if (currentPage < onboardingPages.size - 1) {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.completeOnboarding()
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(
                        "Skip",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // Bottom banner ad - positioned as shown in the screenshot
        if (config.enabled && config.bannerId.isNotBlank()) {
            BannerAd(
                adUnitId = config.bannerId,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(0.7f),
            )
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
    val currentRoute = navBackStackEntry?.destination?.route
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomTabs.map { it.route }) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    scope.launch {
                                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                                        viewModel.recordNavigation()
                                    }
                                }
                            },
                            icon = { Text(tab.label.first().toString()) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.Home) { HomeScreen(adMobManager) }
            composable(Routes.Settings) { val settings by viewModel.settings.collectAsStateWithLifecycle(); SettingsScreen(navController, viewModel, settings) }
            composable(Routes.About) { InfoPage("About", aboutText()) }
            composable(Routes.Privacy) { InfoPage("Privacy Policy", privacyText()) }
            composable(Routes.TermsPage) { InfoPage("Terms of Service", termsText()) }
        }
    }
}

@Composable
fun HomeScreen(adMobManager: AdMobManager) {
    val config = adMobManager.adConfig()
    val token = "YOUR_SCOREBAT_TOKEN"
    Column(modifier = Modifier.fillMaxSize()) {
        // Custom app header replacing ScoreBat branding
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "\u26BD MatchPulse Sports Streaming",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        ScoreBatWidget(token = token, modifier = Modifier.weight(1f))

        if (config.enabled && config.bannerId.isNotBlank()) {
            BannerAd(adUnitId = config.bannerId)
        }
    }
}

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
            // Block requests to known ad servers
            override fun shouldInterceptRequest(
                view: WebView?,
                request: android.webkit.WebResourceRequest?
            ): android.webkit.WebResourceResponse? {
                val url = request?.url?.toString()?.lowercase() ?: return null
                if (url.contains("doubleclick") ||
                    url.contains("googlesyndication") ||
                    url.contains("googleadservices") ||
                    url.contains("googletagmanager") ||
                    url.contains("googletagservices") ||
                    url.contains("scorebat.com/ad")) {
                    return android.webkit.WebResourceResponse(
                        "text/plain", "utf-8", java.io.ByteArrayInputStream("".toByteArray())
                    )
                }
                return null
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
