package com.matchpulse.live

import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.matchpulse.live.core.ads.AdMobManager
import com.matchpulse.live.core.ads.BannerAd
import com.matchpulse.live.core.ads.InterstitialAdManager
import com.matchpulse.live.core.ads.WidgetBridge
import com.matchpulse.live.core.datastore.UserSettings
import com.matchpulse.live.core.navigation.Routes
import com.matchpulse.live.core.navigation.bottomTabs
import com.matchpulse.live.feature.main.MainViewModel
import kotlinx.coroutines.launch
import com.matchpulse.live.R
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
    onComplete: () -> Unit = {},
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

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Background: blurred image (uniform blur)
        Image(
            painter = painterResource(id = R.drawable.onboarding_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(15.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.85f,
        )

        // Dark vignette overlay to softly darken edges for depth
        // Reliable across all GPU configs (unlike BlendMode.DstIn)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.35f),
                            ),
                        ),
                        size = size,
                    )
                }
        )

        // Content column
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
        ) {
            // Top spacer
            Spacer(Modifier.weight(1f))

            // Content block with subtle scrim for text readability
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Subtle dark scrim only behind the text
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                )

                // Onboarding content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
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
                            onComplete()
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
                            onComplete()
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
            }

            // Bottom spacer
            Spacer(Modifier.weight(1f))

            // Bottom banner ad
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
    val config = adMobManager.adConfig()
    val activity = LocalContext.current as? ComponentActivity

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
                                        viewModel.recordNavigation()
                                        if (config.enabled && config.interstitialId.isNotBlank() && activity != null) {
                                            interstitialAdManager.show(
                                                activity = activity,
                                                adUnitId = config.interstitialId,
                                                onDismissed = {
                                                    navController.navigate(tab.route) {
                                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            )
                                        } else {
                                            navController.navigate(tab.route) {
                                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
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
            composable(Routes.Home) { HomeScreen(adMobManager, interstitialAdManager) }
            composable(Routes.Settings) { val settings by viewModel.settings.collectAsStateWithLifecycle(); SettingsScreen(navController, viewModel, settings) }
            composable(Routes.About) { InfoPage("About", aboutText()) }
            composable(Routes.Privacy) { InfoPage("Privacy Policy", privacyText()) }
            composable(Routes.TermsPage) { InfoPage("Terms of Service", termsText()) }
        }
    }
}

@Composable
fun HomeScreen(adMobManager: AdMobManager, interstitialAdManager: InterstitialAdManager) {
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

        val activity = LocalContext.current as? ComponentActivity
        val scope = rememberCoroutineScope()

        ScoreBatWidget(
            token = token,
            modifier = Modifier.weight(1f),
            onWidgetTabChange = {
                if (config.enabled && config.interstitialId.isNotBlank() && activity != null) {
                    scope.launch {
                        interstitialAdManager.show(
                            activity = activity,
                            adUnitId = config.interstitialId,
                            onDismissed = {}
                        )
                    }
                }
            }
        )

        if (config.enabled && config.bannerId.isNotBlank()) {
            BannerAd(adUnitId = config.bannerId)
        }
    }
}
@Composable
fun ScoreBatWidget(
    token: String,
    modifier: Modifier = Modifier,
    onWidgetTabChange: () -> Unit = {},
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
            setBackgroundColor(android.graphics.Color.parseColor("#FF07111F"))  // Dark Navy to avoid Samsung WebView transparency crash
            addJavascriptInterface(WidgetBridge(onWidgetTabChange), "MatchPulseBridge")
        }
    }

    // AndroidView lifecycle handles WebView destruction - no manual destroy needed

    AndroidView(
        factory = { webView },
        modifier = modifier,
    ) { view ->
        view.webViewClient = object : WebViewClient() {
            // Block requests to known ad servers & inject dark navy CSS into widget HTML
            override fun shouldInterceptRequest(
                view: WebView?,
                request: android.webkit.WebResourceRequest?
            ): android.webkit.WebResourceResponse? {
                val url = request?.url?.toString()?.lowercase() ?: return null
                // Block known ad domains
                if (url.contains("doubleclick") ||
                    url.contains("googlesyndication") ||
                    url.contains("googleadservices") ||
                    url.contains("googletagmanager") ||
                    url.contains("googletagservices") ||
                    url.contains("scorebat.com/ad")) {
                    return android.webkit.WebResourceResponse(
                        "text/plain", "utf-8", ByteArrayInputStream("".toByteArray())
                    )
                }
                // Intercept the main widget HTML to inject custom CSS
                if (url.contains("scorebat.com/embed/livescore") && !url.contains(".css") && !url.contains(".js") && !url.contains(".png") && !url.contains(".jpg")) {
                    try {
                        val connection = URL(request!!.url!!.toString()).openConnection() as HttpURLConnection
                        connection.connectTimeout = 10000
                        connection.readTimeout = 10000
                        val inputStream = connection.inputStream
                        val html = inputStream.bufferedReader().use { it.readText() }
                        connection.disconnect()
                        // Inject dark navy theme CSS into <head>
                        val css = """
                            <style id="mp-nav-theme">
                            * { box-shadow: none !important; }
                            body { background-color: #07111F !important; color: #E2E8F0 !important; }
                            [class*="container"], [class*="wrapper"], [class*="inner"], [class*="widget"] {
                                background-color: #07111F !important; }
                            [class*="card"], [class*="match"], [class*="item"],
                            [class*="row"], [class*="box"], [class*="panel"] {
                                background-color: #0F1D2E !important;
                                border-color: #1A2D42 !important; }
                            a, [class*="link"], [class*="tab"], [class*="button"], [class*="btn"] {
                                color: #2B6CB0 !important; }
                            a:hover, [class*="tab"]:hover, [class*="tab"].active, [class*="tab"].selected {
                                color: #4A9BEF !important; }
                            [class*="header"], [class*="title"], h1, h2, h3, h4 {
                                color: #F7FAFC !important; }
                            [class*="score"], [class*="goal"] {
                                color: #48BB78 !important; font-weight: bold !important; }
                            [class*="time"], [class*="date"], [class*="status"] {
                                color: #8899AA !important; }
                            [class*="live"], [class*="badge"] {
                                background-color: #E53E3E !important; color: #FFFFFF !important; }
                            [class*="divider"], [class*="separator"], hr {
                                border-color: #1A2D42 !important; background-color: #1A2D42 !important; }
                            [class*="footer"], [class*="branding"], [class*="powered"] {
                                display: none !important; }
                            td, th { border-color: #1A2D42 !important; }
                            table { background-color: #07111F !important; }
                            ::-webkit-scrollbar { width: 4px !important; }
                            ::-webkit-scrollbar-track { background: #0A1628 !important; }
                            ::-webkit-scrollbar-thumb { background: #1A3A5C !important; border-radius: 2px !important; }
                            </style>
                        """.trimIndent()
                        val modifiedHtml = html.replace("<head>", "<head>\n$css")
                        return android.webkit.WebResourceResponse(
                            "text/html", "utf-8", ByteArrayInputStream(modifiedHtml.toByteArray())
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                return null
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.evaluateJavascript(
                    "(function() {" +
                    "var lastUrl = location.href;" +
                    "var lastTrigger = 0;" +
                    "var minInterval = 5000;" +
                    "function triggerTabChange() {" +
                    "var now = Date.now();" +
                    "if (now - lastTrigger < minInterval) return;" +
                    "lastTrigger = now;" +
                    "setTimeout(function() {" +
                    "if (window.MatchPulseBridge) { window.MatchPulseBridge.onTabChanged(); }" +
                    "}, 200);" +
                    "}" +
                    "var origPushState = history.pushState;" +
                    "history.pushState = function() {" +
                    "origPushState.apply(this, arguments);" +
                    "triggerTabChange();" +
                    "};" +
                    "var origReplaceState = history.replaceState;" +
                    "history.replaceState = function() {" +
                    "origReplaceState.apply(this, arguments);" +
                    "triggerTabChange();" +
                    "};" +
                    "window.addEventListener('popstate', triggerTabChange);" +
                    "setInterval(function() {" +
                    "if (location.href !== lastUrl) {" +
                    "lastUrl = location.href;" +
                    "triggerTabChange();" +
                    "}" +
                    "}, 1000);" +
                    "setTimeout(function() {" +
                    "var observer = new MutationObserver(triggerTabChange);" +
                    "var root = document.querySelector('#root') || document.body;" +
                    "if (root) { observer.observe(root, { childList: true, subtree: true }); }" +
                    "}, 2000);" +
                    "})()", null
                )
                // Inject custom CSS to match dark navy app theme
                // Uses double quotes in CSS selectors to avoid breaking JS string
                view?.evaluateJavascript(
                    "(function() {" +
                    "var css = " +
                    "'* { box-shadow: none !important; }" +
                    "body { background-color: #07111F !important; color: #E2E8F0 !important; }" +
                    "[class*=\"container\"], [class*=\"wrapper\"], [class*=\"inner\"], [class*=\"widget\"] { " +
                    "background-color: #07111F !important; }" +
                    "[class*=\"card\"], [class*=\"match\"], [class*=\"item\"], " +
                    "[class*=\"row\"], [class*=\"box\"], [class*=\"panel\"] { " +
                    "background-color: #0F1D2E !important; " +
                    "border-color: #1A2D42 !important; }" +
                    "a, [class*=\"link\"], [class*=\"tab\"], [class*=\"button\"], " +
                    "[class*=\"btn\"] { color: #2B6CB0 !important; }" +
                    "a:hover, [class*=\"tab\"]:hover, [class*=\"tab\"].active, " +
                    "[class*=\"tab\"].selected { color: #4A9BEF !important; }" +
                    "[class*=\"header\"], [class*=\"title\"], h1, h2, h3, h4 { " +
                    "color: #F7FAFC !important; }" +
                    "[class*=\"score\"], [class*=\"goal\"] { " +
                    "color: #48BB78 !important; font-weight: bold !important; }" +
                    "[class*=\"time\"], [class*=\"date\"], [class*=\"status\"] { " +
                    "color: #8899AA !important; }" +
                    "[class*=\"live\"], [class*=\"badge\"] { " +
                    "background-color: #E53E3E !important; color: #FFFFFF !important; }" +
                    "[class*=\"divider\"], [class*=\"separator\"], hr { " +
                    "border-color: #1A2D42 !important; background-color: #1A2D42 !important; }" +
                    "[class*=\"footer\"], [class*=\"branding\"], [class*=\"powered\"] { " +
                    "display: none !important; }" +
                    "td, th { border-color: #1A2D42 !important; }" +
                    "table { background-color: #07111F !important; }" +
                    "::-webkit-scrollbar { width: 4px !important; }" +
                    "::-webkit-scrollbar-track { background: #0A1628 !important; }" +
                    "::-webkit-scrollbar-thumb { background: #1A3A5C !important; border-radius: 2px !important; }" +
                    "';" +
                    "function applyStyles() {" +
                    "var style = document.getElementById('mp-nav-theme');" +
                    "if (!style) { style = document.createElement('style'); style.id = 'mp-nav-theme'; document.head.appendChild(style); }" +
                    "style.textContent = css;" +
                    "}" +
                    "applyStyles();" +
                    "setTimeout(applyStyles, 1500);" +
                    "setTimeout(applyStyles, 3000);" +
                    "var obs = new MutationObserver(function() { setTimeout(applyStyles, 100); });" +
                    "obs.observe(document.body, { childList: true, subtree: true, attributes: true, attributeFilter: ['class', 'style'] });" +
                    "})()", null
                )
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
