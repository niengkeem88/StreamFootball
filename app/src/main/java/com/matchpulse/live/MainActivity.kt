package com.matchpulse.live

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.derivedStateOf
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.ads.AdSize
import com.matchpulse.live.core.ads.AdMobManager
import com.matchpulse.live.core.ads.AdPlacement
import com.matchpulse.live.core.ads.InterstitialAdManager
import com.matchpulse.live.core.design.components.AdContainer
import com.matchpulse.live.core.design.components.CompetitionCard
import com.matchpulse.live.core.design.components.EmptyState
import com.matchpulse.live.core.design.components.ErrorState
import com.matchpulse.live.core.design.components.HeroHeader
import com.matchpulse.live.core.design.components.LegalDisclaimerCard
import com.matchpulse.live.core.design.components.LoadingState
import com.matchpulse.live.core.design.components.MatchCard
import com.matchpulse.live.core.design.components.PrimaryButton
import com.matchpulse.live.core.design.components.SearchBar
import com.matchpulse.live.core.design.components.SectionHeader
import com.matchpulse.live.core.design.components.SettingGroup
import com.matchpulse.live.core.design.components.SettingRow
import com.matchpulse.live.core.design.components.Spacer16
import com.matchpulse.live.core.design.components.Spacer8
import com.matchpulse.live.core.design.components.StatusBadge
import com.matchpulse.live.core.design.components.TvCompetitionCard
import com.matchpulse.live.core.design.components.TvScheduleCard
import com.matchpulse.live.core.design.components.formatTime
import com.matchpulse.live.core.design.theme.MatchPulseTheme
import com.matchpulse.live.core.navigation.Routes
import com.matchpulse.live.core.navigation.bottomTabs
import com.matchpulse.live.ui.FootballWidgetScreen
import com.matchpulse.live.domain.model.Competition
import com.matchpulse.live.domain.model.DataState
import com.matchpulse.live.domain.model.FootballMatch
import com.matchpulse.live.domain.model.MatchEvent
import com.matchpulse.live.domain.model.MatchStats
import com.matchpulse.live.domain.model.TvGuideCompetition
import com.matchpulse.live.domain.model.TvProvider
import com.matchpulse.live.domain.model.TvScheduleItem
import com.matchpulse.live.feature.main.MainViewModel
import com.matchpulse.live.feature.main.ScoreFilter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    @Inject lateinit var adMobManager: AdMobManager
    @Inject lateinit var interstitialAdManager: InterstitialAdManager

    companion object {
        private var _interstitialAdManager: InterstitialAdManager? = null
        val globalInterstitialAdManager: InterstitialAdManager? get() = _interstitialAdManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        _interstitialAdManager = interstitialAdManager
        enableEdgeToEdge()

        // Gather UMP Consent
        adMobManager.gatherConsentAndInitialize(this)

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            MatchPulseTheme(darkTheme = settings.darkModeEnabled) {
                MatchPulseApp(
                    viewModel = viewModel,
                    adMobManager = adMobManager,
                    interstitialAdManager = interstitialAdManager,
                )
            }
        }
    }
}

@Composable
fun MatchPulseApp(
    viewModel: MainViewModel,
    adMobManager: AdMobManager,
    interstitialAdManager: InterstitialAdManager,
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(activity) {
        if (activity != null) interstitialAdManager.preload(activity)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { MainBottomBar(navController, viewModel) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Splash,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.Splash) {
                SplashRoute(viewModel, navController)
            }
            composable(Routes.Onboarding) {
                OnboardingScreen(
                    adMobManager = adMobManager,
                    onDone = {
                        viewModel.completeOnboarding()
                        navController.navigate(Routes.Region) { popUpTo(Routes.Onboarding) { inclusive = true } }
                    }
                )
            }
            composable(Routes.Region) {
                RegionScreen(
                    viewModel = viewModel,
                    adMobManager = adMobManager,
                    onContinue = {
                        navController.navigate(Routes.Terms) { popUpTo(Routes.Region) { inclusive = true } }
                    }
                )
            }
            composable(Routes.Terms) {
                TermsGateScreen(
                    adMobManager = adMobManager,
                    onAccepted = {
                        viewModel.acceptTerms()
                        navController.navigate(Routes.Home) { popUpTo(Routes.Splash) { inclusive = true } }
                    }
                )
            }
            composable(Routes.Home) { HomeScreen(viewModel, navController, adMobManager) }
            composable(Routes.Scores) { ScoresScreen(viewModel, navController, adMobManager) }
            composable(Routes.TvGuide) {
                TvGuideScreen(viewModel, adMobManager) { competition ->
                    val go = { navController.navigate(Routes.tvSchedule(competition.id)) }
                    if (activity != null) {
                        scope.launch { interstitialAdManager.showIfAllowed(activity, go) }
                    } else {
                        go()
                    }
                }
            }
            composable(Routes.Favorites) { FavoritesScreen(viewModel, navController, adMobManager) }
            composable(Routes.Settings) { SettingsScreen(viewModel, adMobManager, navController) }
            composable(
                Routes.MatchDetail,
                arguments = listOf(navArgument("matchId") { type = NavType.StringType }),
            ) { entry ->
                val matchId = entry.arguments?.getString("matchId").orEmpty()
                MatchDetailScreen(matchId, viewModel, adMobManager, navController)
            }
            composable(
                Routes.TvSchedule,
                arguments = listOf(navArgument("competitionId") { type = NavType.StringType }),
            ) { entry ->
                val competitionId = entry.arguments?.getString("competitionId").orEmpty()
                TvScheduleScreen(
                    competitionId = competitionId,
                    viewModel = viewModel,
                    adMobManager = adMobManager,
                    onProvider = { provider ->
                        val message = viewModel.providerMessage(provider)
                        if (message != null) {
                            scope.launch { snackbarHostState.showSnackbar(message) }
                            return@TvScheduleScreen
                        }
                        val open = {
                            val intent = Intent(Intent.ACTION_VIEW, provider.url.orEmpty().toUri())
                            context.startActivity(intent)
                        }
                        if (activity != null) {
                            scope.launch { interstitialAdManager.showIfAllowed(activity, open) }
                        } else {
                            open()
                        }
                    }
                )
            }
            composable(Routes.About) { InfoPage("About MatchPulse Live", aboutText()) }
            composable(Routes.Privacy) { InfoPage("Privacy Policy", privacyText()) }
            composable(Routes.TermsPage) { InfoPage("Terms and Conditions", termsText()) }
            composable(Routes.Diagnostics) {
                if (viewModel.appConfig.isDebugLike) {
                    DiagnosticsScreen(viewModel, adMobManager)
                } else {
                    InfoPage("Not Available", "Developer diagnostics are not available in production builds.")
                }
            }
            composable(Routes.Maintenance) { InfoPage("Maintenance", "MatchPulse Live is temporarily unavailable while service maintenance is active.") }
            composable(Routes.Update) { InfoPage("Update Required", "Please install the latest version of MatchPulse Live to continue.") }
        }
    }
}

@Composable
private fun MainBottomBar(navController: NavHostController, viewModel: MainViewModel) {
    val entry by navController.currentBackStackEntryAsState()
    val current = entry?.destination?.route
    if (current !in bottomTabs.map { it.route }) return
    NavigationBar {
        bottomTabs.forEach { tab ->
            NavigationBarItem(
                selected = current == tab.route,
                onClick = {
                    viewModel.recordMajorNavigation()
                    navController.navigate(tab.route) {
                        popUpTo(Routes.Home) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Text(tab.label.take(1)) },
                label = { Text(tab.label) },
            )
        }
    }
}

@Composable
private fun SplashRoute(viewModel: MainViewModel, navController: NavHostController) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val settingsLoaded by viewModel.settingsLoaded.collectAsStateWithLifecycle()
    val remote by viewModel.remoteConfig.collectAsStateWithLifecycle()
    LaunchedEffect(settingsLoaded, settings, remote) {
        if (!settingsLoaded) return@LaunchedEffect
        val remoteConfig = (remote as? DataState.Success)?.data
        val target = when {
            remoteConfig?.maintenanceMode == true -> Routes.Maintenance
            remoteConfig?.forceUpdate == true -> Routes.Update
            else -> viewModel.nextRouteFor(settings)
        }
        navController.navigate(target) { popUpTo(Routes.Splash) { inclusive = true } }
    }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        // Branding Logo Background (Watermark)
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_image),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(0.05f).padding(40.dp),
            contentScale = ContentScale.Fit
        )
        
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_image),
                contentDescription = "MatchPulse Logo",
                modifier = Modifier.size(120.dp).clip(RoundedCornerShape(28.dp))
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("MatchPulse Live", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Live Scores - Fixtures - TV Guide", color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OnboardingScreen(adMobManager: AdMobManager, onDone: () -> Unit) {
    val pages = listOf(
        "Follow Every Match" to "Track live scores, fixtures, and results from leagues around the world.",
        "Personalized Football" to "Choose your region and favorite competitions for a better football experience.",
        "Legal TV Guide" to "Find official broadcast options and football schedules in one place.",
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Branding Watermark Background
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_image),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(0.03f).padding(20.dp),
            contentScale = ContentScale.Fit
        )

        Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDone) { Text("Skip") }
            }
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_image),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp).alpha(0.8f).clip(RoundedCornerShape(16.dp))
                    )
                    Spacer16()
                    Text(pages[page].first, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer8()
                    Text(pages[page].second, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.8f))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    repeat(pages.size) { index ->
                        Surface(
                            modifier = Modifier.padding(4.dp).height(8.dp).width(if (index == pagerState.currentPage) 28.dp else 8.dp),
                            shape = RoundedCornerShape(999.dp),
                            color = if (index == pagerState.currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        ) {}
                    }
                }
                
                // Persistent Premium Large-Format Ad
            AdContainer(
                placement = AdPlacement.ONBOARDING,
                adMobManager = adMobManager,
                adSize = AdSize.MEDIUM_RECTANGLE
            )

            Button(
                    onClick = {
                        if (pagerState.currentPage == pages.lastIndex) {
                            if (activity != null) {
                                scope.launch {
                                    MainActivity.globalInterstitialAdManager?.showIfAvailable(activity, AdPlacement.ONBOARDING, onDone)
                                }
                            } else {
                                onDone()
                            }
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (pagerState.currentPage == pages.lastIndex) "Get Started" else "Next")
                }
            }
        }
    }
}

@Composable
private fun RegionScreen(viewModel: MainViewModel, adMobManager: AdMobManager, onContinue: () -> Unit) {
    var selected by remember { mutableStateOf<String?>(null) }
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_image),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(0.02f).padding(20.dp),
            contentScale = ContentScale.Fit
        )
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Choose Your Region", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("This helps tailor football competitions and TV guide content.")
            }
            items(viewModel.regions, key = { it.id }) { region ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { selected = region.id },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected == region.id) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface
                    ),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(region.icon, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Column {
                            Text(region.name, fontWeight = FontWeight.Bold)
                            Text(region.subtitle, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdContainer(
                    placement = AdPlacement.ONBOARDING,
                    adMobManager = adMobManager,
                    adSize = AdSize.MEDIUM_RECTANGLE
                )
                PrimaryButton("Continue", enabled = selected != null) {
                        val region = selected ?: return@PrimaryButton
                        viewModel.selectRegion(region)
                        onContinue()
                    }
                }
            }
        }
    }
}

@Composable
private fun TermsGateScreen(adMobManager: AdMobManager, onAccepted: () -> Unit) {
    var checked by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Terms and Conditions", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Card(Modifier.weight(1f), shape = RoundedCornerShape(18.dp)) {
            Text(
                termsText(),
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checked, onCheckedChange = { checked = it })
            Text("I accept the terms and legal TV guide policy.")
        }
        AdContainer(
            placement = AdPlacement.TERMS_GATE,
            adMobManager = adMobManager,
            adSize = AdSize.MEDIUM_RECTANGLE
        )
        PrimaryButton("Accept & Continue", enabled = checked, onClick = onAccepted)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(viewModel: MainViewModel, navController: NavHostController, adMobManager: AdMobManager) {
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val lastUpdated by viewModel.lastUpdated.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { viewModel.loadHome() }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.loadHome(force = true) }
    ) {
        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { HeroHeader("Today's Football", "Live scores, fixtures & official TV guide", lastUpdated) }
            item { SearchBar(search, { search = it }, "Search teams or competitions", Modifier.padding(horizontal = 16.dp)) }
            
            // Native ad placement before widget
            item {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    AdContainer(AdPlacement.HOME_DASHBOARD, adMobManager)
                }
            }
            
            // API-Football Widget Section
            item {
                PaddedSectionHeader("Live Football Scores")
            }
            item {
                FootballWidgetScreen(modifier = Modifier.padding(horizontal = 8.dp))
            }
            
            item { Spacer(Modifier.height(12.dp)) }
            
            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LegalDisclaimerCard()
                    Button(onClick = { navController.navigate(Routes.TvGuide) }, modifier = Modifier.fillMaxWidth()) { Text("Open Official TV Guide") }
                }
            }
        }
    }
}

@Composable
private fun PaddedSectionHeader(title: String) {
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { SectionHeader(title) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScoresScreen(viewModel: MainViewModel, navController: NavHostController, adMobManager: AdMobManager) {
    val state by viewModel.scores.collectAsStateWithLifecycle()
    val filter by viewModel.scoreFilter.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val lastUpdated by viewModel.lastUpdated.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    
    val filteredMatches by remember(state, search) {
        derivedStateOf {
            (state as? DataState.Success)?.data?.filterSearch(search) ?: emptyList()
        }
    }

    Column(Modifier.fillMaxSize()) {
        HeroHeader("Scores", "Live, today, upcoming and finished matches", lastUpdated)
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadScores(filter, force = true) }
        ) {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                item { SearchBar(search, { search = it }, "Search matches") }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ScoreFilter.entries, key = { it.name }) { chip ->
                            FilterChip(
                                selected = filter == chip,
                                onClick = { viewModel.loadScores(chip, force = true) },
                                label = { Text(chip.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            )
                        }
                    }
                }
                item { OutlinedButton(onClick = { viewModel.loadScores(filter, force = true) }, modifier = Modifier.fillMaxWidth()) { Text("Refresh scores") } }
                when (val result = state) {
                    DataState.Loading -> item { LoadingState(Modifier.height(180.dp)) }
                    is DataState.Error -> item { ErrorState(result.message, onRetry = { viewModel.loadScores(filter, force = true) }) }
                    is DataState.Success -> {
                        if (filteredMatches.isEmpty()) {
                            item { EmptyState("No matches", "Try another filter or search term.") }
                        } else {
                            // Insert ads every 6 matches
                            filteredMatches.chunked(6).forEachIndexed { index, chunk ->
                                items(chunk, key = { it.id }) { match ->
                                    MatchCard(match, favorites.contains(match.id), { viewModel.toggleFavorite(match.id) }, { navController.navigate(Routes.match(match.id)) })
                                }
                                item { AdContainer(AdPlacement.SCORES_LIST, adMobManager) }
                            }
                        }
                        if (result.stale) item { Text("Showing cached results while refreshing.", color = MaterialTheme.colorScheme.tertiary) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchDetailScreen(matchId: String, viewModel: MainViewModel, adMobManager: AdMobManager, navController: NavHostController) {
    val state by viewModel.matchDetail.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    LaunchedEffect(matchId) { viewModel.loadMatch(matchId) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        when (val result = state) {
            DataState.Loading -> item { LoadingState(Modifier.height(220.dp)) }
            is DataState.Error -> item { ErrorState(result.message) }
            is DataState.Success -> {
                val match = result.data
                item {
                    TextButton(onClick = { navController.popBackStack() }) { Text("Back") }
                    Text(match.competition.name, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("${match.homeTeam.name} vs ${match.awayTeam.name}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer8()
                    StatusBadge(match.status, match.minute)
                }
                item {
                    Scoreboard(match, favorites.contains(match.id)) { viewModel.toggleFavorite(match.id) }
                }
                item { StatsCard(match.stats) }
                item { TimelineCard(match.events) }
                item { LegalDisclaimerCard() }
                item { AdContainer(AdPlacement.MATCH_DETAIL, adMobManager) }
            }
        }
    }
}

@Composable
private fun Scoreboard(match: FootballMatch, favorite: Boolean, onFavorite: () -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(match.homeTeam.shortName, fontWeight = FontWeight.Bold)
                Text("${match.homeScore ?: "-"} : ${match.awayScore ?: "-"}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(match.awayTeam.shortName, fontWeight = FontWeight.Bold)
            }
            Text(match.venue)
            TextButton(onClick = onFavorite) { Text(if (favorite) "Remove Favorite" else "Add Favorite") }
        }
    }
}

@Composable
private fun StatsCard(stats: MatchStats?) {
    if (stats == null) return
    Card(shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader("Stats")
            StatRow("Possession", "${stats.homePossession}%", "${stats.awayPossession}%")
            StatRow("Shots", stats.homeShots.toString(), stats.awayShots.toString())
            StatRow("Shots on target", stats.homeShotsOnTarget.toString(), stats.awayShotsOnTarget.toString())
            StatRow("Corners", stats.homeCorners.toString(), stats.awayCorners.toString())
            StatRow("Fouls", stats.homeFouls.toString(), stats.awayFouls.toString())
        }
    }
}

@Composable
private fun StatRow(label: String, home: String, away: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(home, fontWeight = FontWeight.Bold)
        Text(label)
        Text(away, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TimelineCard(events: List<MatchEvent>) {
    Card(shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader("Timeline")
            if (events.isEmpty()) Text("Timeline will appear when match events are available.")
            events.forEach { event ->
                Text("${event.minute}' ${event.type.name.replace('_', ' ')} - ${event.detail}")
            }
        }
    }
}

@Composable
private fun TvGuideScreen(viewModel: MainViewModel, adMobManager: AdMobManager, onSchedule: (TvGuideCompetition) -> Unit) {
    val state by viewModel.tvCompetitions.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }
    val categories = listOf("All", "International", "Europe", "Africa", "Americas", "Asia", "Domestic")
    LaunchedEffect(Unit) { viewModel.loadTvGuide() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Legal TV Guide", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Based on selected region: ${settings.selectedRegion ?: "Not selected"}")
        }
        item { LegalDisclaimerCard() }
        item { SearchBar(search, { search = it }, "Search competitions or providers") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { chip ->
                    FilterChip(selected = category == chip, onClick = { category = chip }, label = { Text(chip) })
                }
            }
        }
        when (val result = state) {
            DataState.Loading -> item { LoadingState(Modifier.height(180.dp)) }
            is DataState.Error -> item { ErrorState(result.message) }
            is DataState.Success -> {
                val data = result.data
                    .filter { category == "All" || it.category.equals(category, true) || it.region.equals(category, true) }
                    .filter { it.name.contains(search, true) || it.description.contains(search, true) }
                
                if (data.isEmpty()) {
                    item { EmptyState("No TV guide entries", "Try a different region, category, or search term.") }
                } else {
                    // Insert ads every 5 competitions
                    data.chunked(5).forEachIndexed { index, chunk ->
                        items(chunk, key = { it.id }) { competition -> 
                            TvCompetitionCard(competition, onSchedule = { onSchedule(competition) }) 
                        }
                        item { AdContainer(AdPlacement.TV_GUIDE, adMobManager) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvScheduleScreen(competitionId: String, viewModel: MainViewModel, adMobManager: AdMobManager, onProvider: (TvProvider) -> Unit) {
    val state by viewModel.tvSchedule.collectAsStateWithLifecycle()
    LaunchedEffect(competitionId) { viewModel.loadTvSchedule(competitionId) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Official Schedule", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            LegalDisclaimerCard()
        }
        item { AdContainer(AdPlacement.TV_GUIDE, adMobManager) }
        when (val result = state) {
            DataState.Loading -> item { LoadingState(Modifier.height(180.dp)) }
            is DataState.Error -> item { ErrorState(result.message) }
            is DataState.Success -> {
                if (result.data.isEmpty()) item { EmptyState("No schedule", "Provider schedules will appear when configured.") }
                items(result.data, key = { it.id }) { item ->
                    TvScheduleCard(item = item, onProviderClick = { providerId ->
                        item.providers.firstOrNull { it.id == providerId }?.let(onProvider)
                    })
                }
                item {
                    Card(shape = RoundedCornerShape(18.dp)) {
                        Text(viewModel.playbackMessage(), modifier = Modifier.padding(16.dp))
                    }
                }
                item { AdContainer(AdPlacement.TV_SCHEDULE_DETAIL, adMobManager) }
            }
        }
    }
}

@Composable
private fun FavoritesScreen(viewModel: MainViewModel, navController: NavHostController, adMobManager: AdMobManager) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val live by viewModel.liveMatches.collectAsStateWithLifecycle()
    val upcoming by viewModel.upcomingMatches.collectAsStateWithLifecycle()
    val scores by viewModel.scores.collectAsStateWithLifecycle()
    val matches = (live.dataOrEmpty() + upcoming.dataOrEmpty() + scores.dataOrEmpty()).distinctBy { it.id }.filter { favorites.contains(it.id) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Favorites", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Favorite matches, teams, and competitions.")
        }
        if (matches.isEmpty()) {
            item { EmptyState("No favorites yet", "Save match cards to build your matchday list.") }
        } else {
            items(matches, key = { it.id }) { match ->
                MatchCard(match, true, { viewModel.toggleFavorite(match.id) }, { navController.navigate(Routes.match(match.id)) })
            }
            item { AdContainer(AdPlacement.FAVORITES_BOTTOM, adMobManager) }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp)) {
                Text("Favorite teams and competitions placeholders are ready for backend-backed personalization.", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun SettingsScreen(viewModel: MainViewModel, adMobManager: AdMobManager, navController: NavHostController) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        item {
            SettingGroup("Preferences") {
                SettingRow("Push Notifications", "Optional future match alerts", settings.pushNotificationsEnabled, viewModel::togglePush)
                SettingRow("Dark Mode", "Apply instantly and persist", settings.darkModeEnabled, viewModel::toggleDarkMode)
                SettingRow("Auto Refresh", "Refresh live scores automatically", settings.autoRefreshEnabled, viewModel::toggleAutoRefresh)
                SettingRow("Data Saver", "Use slower refresh intervals", settings.dataSaverEnabled, viewModel::toggleDataSaver)
                SettingRow("Selected Region", settings.selectedRegion ?: "Choose region", onClick = { navController.navigate(Routes.Region) })
                
                if (adMobManager.isPrivacyOptionsRequired()) {
                    SettingRow("Privacy Settings", "Review your ad consent choices", onClick = {
                        if (activity != null) adMobManager.showPrivacyOptions(activity)
                    })
                }
            }
        }
        item {
            SettingGroup("App Actions") {
                SettingRow("Share App", "Tell another football fan", onClick = {
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Try MatchPulse Live for scores, fixtures, and legal TV guide.")
                    }, "Share MatchPulse Live"))
                })
                SettingRow("Rate App", "Open Play Store listing when published", onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=com.matchpulse.live".toUri()))
                })
            }
        }
        item {
            SettingGroup("About") {
                SettingRow("About App", "Version 1.0.0", onClick = { navController.navigate(Routes.About) })
                SettingRow("Privacy Policy", "Local preferences, favorites, ads, and backend data", onClick = { navController.navigate(Routes.Privacy) })
                SettingRow("Terms and Conditions", "Legal TV guide and no unauthorized streams", onClick = { navController.navigate(Routes.TermsPage) })
            }
        }
        if (viewModel.appConfig.isDebugLike) {
            item {
                SettingGroup("Developer") {
                    SettingRow("Developer Tools / API Diagnostics", "Debug and non-production only", onClick = { navController.navigate(Routes.Diagnostics) })
                    SettingRow("Reset Onboarding Demo", "Clears onboarding, region, and terms gates", onClick = { viewModel.resetOnboardingDemo(); navController.navigate(Routes.Splash) })
                }
            }
        }
    }
}

@Composable
private fun DiagnosticsScreen(viewModel: MainViewModel, adMobManager: AdMobManager) {
    val diagnostics by viewModel.diagnostics.collectAsStateWithLifecycle()
    val apiSummary by viewModel.apiSummary.collectAsStateWithLifecycle()
    val status = adMobManager.status()
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("System Diagnostics", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        
        item { 
            DiagnosticsCard("API Performance") {
                Text("Total Requests: ${apiSummary.requestCount}")
                Text("Last Code: ${apiSummary.lastStatusCode ?: "N/A"}")
                if (apiSummary.isQuotaExceeded) {
                    Text("QUOTA EXCEEDED", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
                Text("Last Endpoint: ${apiSummary.lastEndpoint?.split("/")?.lastOrNull() ?: "None"}")
            }
        }

        item {
            DiagnosticsCard("AdMob & Config") {
                Text("APP_ENV: ${viewModel.appConfig.appEnv}")
                Text("API_BASE_URL: ${viewModel.appConfig.apiBaseUrl}")
                Text("ENABLE_ADS: ${status.adsEnabled}")
                Text("INITIALIZED: ${status.initialized}")
                Text("CAN_REQUEST_ADS: ${status.canRequestAds}")
            }
        }

        item { SectionHeader("Recent API Logs") }
        items(apiSummary.logs) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (log.statusCode in 200..299) Color.Transparent else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(log.endpoint.split("/").lastOrNull() ?: "", fontWeight = FontWeight.Bold)
                        Text("${log.statusCode}", color = if (log.statusCode in 200..299) Color.Unspecified else Color.Red)
                    }
                    Text(log.message, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item { SectionHeader("Actions") }
        item { DiagnosticButton("Force Refresh All Data", viewModel::retryAll) }
        item { DiagnosticButton("Test Backend Health", viewModel::testBackendHealth) }
        item { DiagnosticButton("Reset Frequency Caps", viewModel::resetFrequencyCaps) }
        
        item { AdContainer(AdPlacement.DIAGNOSTICS_TEST, adMobManager) }
    }
}

@Composable
private fun DiagnosticsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

@Composable
private fun DiagnosticButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(label) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoPage(title: String, body: String) {
    Scaffold(topBar = { TopAppBar(title = { Text(title) }) }) { padding ->
        Text(
            body,
            modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun <T> DataBlock(state: DataState<List<T>>, onRetry: (() -> Unit)? = null, content: @Composable (List<T>) -> Unit) {
    when (state) {
        DataState.Loading -> LoadingState(Modifier.height(180.dp))
        is DataState.Error -> ErrorState(state.message, onRetry = onRetry)
        is DataState.Success -> if (state.data.isEmpty()) EmptyState("Nothing here yet", "Data will appear when matches are available.") else content(state.data)
    }
}

private fun DataState<List<FootballMatch>>.dataOrEmpty(): List<FootballMatch> =
    when (this) {
        is DataState.Success -> data
        is DataState.Error -> cachedData.orEmpty()
        DataState.Loading -> emptyList()
    }

private fun List<FootballMatch>.filterSearch(search: String): List<FootballMatch> =
    if (search.isBlank()) this else filter {
        it.homeTeam.name.contains(search, true) ||
            it.awayTeam.name.contains(search, true) ||
            it.competition.name.contains(search, true)
    }

private fun aboutText(): String = """
MatchPulse Live provides football scores, fixtures, and legal TV guide information.

It does not host, distribute, or promote unauthorized live streams. The app is designed for official provider discovery and future licensed playback readiness only.

Current version: 1.0.0
""".trimIndent()

private fun privacyText(): String = """
MatchPulse Live stores local preferences such as onboarding status, selected region, dark mode, refresh choices, and favorite match IDs.

If ads are enabled, Google Mobile Ads may process advertising data according to Google's policies. Analytics and push notifications are placeholders for future optional use.

Football data is loaded through a configured backend. API-Football keys must never be stored in the Android app.

MatchPulse Live does not collect, host, distribute, or promote unauthorized streaming links.
""".trimIndent()

private fun termsText(): String = """
1. Acceptance of Terms
By using MatchPulse Live, you agree to use the app responsibly and only for lawful football information.

2. Use License
The app is provided for personal football companion use. Do not misuse, reverse engineer, or attempt to bypass service restrictions.

3. Football Data
Scores, fixtures, and match details are informational. Accuracy, timing, and availability are not guaranteed.

4. Legal TV Guide
The TV guide is informational and may point to official providers only when legal backend configuration is available.

5. No Unauthorized Streams
MatchPulse Live does not host, distribute, or promote unauthorized live streams. Pirate streaming links, IPTV/M3U scraping, random streaming sites, and user-submitted stream links are not supported.

6. Advertising
Ads may appear only in allowed app areas when enabled and configured. Ads are never shown on onboarding, region selection, terms, or privacy screens.

7. Privacy
Local preferences and favorites remain on the device. Backend football data is requested through configured app endpoints.

8. Changes
Terms, privacy copy, and provider configuration may be updated as the product evolves.
""".trimIndent()
