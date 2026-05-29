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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.matchpulse.live.core.config.AppConfig
import com.matchpulse.live.core.design.theme.MatchPulseTheme
import com.matchpulse.live.core.navigation.Routes
import com.matchpulse.live.core.navigation.bottomTabs
import com.matchpulse.live.feature.main.MainViewModel
import com.matchpulse.live.BuildConfig
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MatchPulseTheme(darkTheme = false) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.Home

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            if (currentRoute != tab.route) {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
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
            composable(Routes.Home) { HomeScreen() }
            composable(Routes.Settings) { SettingsScreen(navController) }
            composable(Routes.About) { InfoPage("About", aboutText()) }
            composable(Routes.Privacy) { InfoPage("Privacy", privacyText()) }
            composable(Routes.TermsPage) { InfoPage("Terms", termsText()) }
        }
    }
}

@Composable
fun HomeScreen() {
    val token = "YOUR_SCOREBAT_TOKEN" // Replace with your ScoreBat token

    Column(modifier = Modifier.fillMaxSize()) {
        ScoreBatWidget(
            token = token,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun ScoreBatWidget(
    token: String,
    modifier: Modifier = Modifier,
) {
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

        // Load ScoreBat livescore widget with token
        view.loadUrl("https://www.scorebat.com/embed/livescore/?token=$token&theme=dark&lang=en")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("App Info", style = MaterialTheme.typography.titleMedium)
                    Text("MatchPulse Live v1.0.1", style = MaterialTheme.typography.bodyMedium)
                    Text("Powered by ScoreBat LiveScore Widget", style = MaterialTheme.typography.bodySmall)
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
        Text(
            body,
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

private fun aboutText(): String = """
MatchPulse Live provides football live scores powered by ScoreBat.

This app uses the ScoreBat LiveScore Widget to display real-time football scores, fixtures, and match information.

Current version: 1.0.1
""".trimIndent()

private fun privacyText(): String = """
MatchPulse Live stores local preferences such as dark mode selection on your device.

If ads are enabled, Google Mobile Ads may process advertising data according to Google's policies.

MatchPulse Live does not collect, store, or transmit any personal data.
""".trimIndent()

private fun termsText(): String = """
1. Acceptance of Terms
By using MatchPulse Live, you agree to use the app responsibly and only for lawful football information.

2. Use License
The app is provided for personal football companion use. Do not misuse, reverse engineer, or attempt to bypass service restrictions.

3. Football Data
Scores, fixtures, and match details are provided by ScoreBat and are informational. Accuracy, timing, and availability are not guaranteed.

4. Advertising
Ads may appear when enabled and configured.

5. Privacy
Local preferences remain on the device. No personal data is collected by the app.

6. Changes
Terms may be updated as the product evolves.
""".trimIndent()
