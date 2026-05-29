package com.matchpulse.live.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matchpulse.live.core.ads.AdFrequencyCapManager
import com.matchpulse.live.core.config.AppConfig
import com.matchpulse.live.core.legal.ProviderLinkValidator
import com.matchpulse.live.core.media.LegalPlaybackGate
import com.matchpulse.live.core.network.ApiService
import com.matchpulse.live.core.network.NetworkErrorMapper
import com.matchpulse.live.domain.model.AppRemoteConfig
import com.matchpulse.live.domain.model.Competition
import com.matchpulse.live.domain.model.DataState
import com.matchpulse.live.domain.model.FootballMatch
import com.matchpulse.live.domain.model.Region
import com.matchpulse.live.domain.model.TvGuideCompetition
import com.matchpulse.live.domain.model.TvProvider
import com.matchpulse.live.domain.model.TvScheduleItem
import com.matchpulse.live.domain.model.UserSettings
import com.matchpulse.live.domain.repository.AppConfigRepository
import com.matchpulse.live.domain.repository.FavoritesRepository
import com.matchpulse.live.domain.repository.FootballRepository
import com.matchpulse.live.domain.repository.SettingsRepository
import com.matchpulse.live.domain.repository.TvGuideRepository
import com.matchpulse.live.core.util.ApiDiagnosticsManager
import com.matchpulse.live.core.util.DiagnosticsSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScoreFilter { LIVE, TODAY, UPCOMING, FINISHED }

data class DiagnosticsState(
    val backendHealth: String = "Not tested",
    val providerHealth: String = "Not tested",
    val lastSuccessfulCall: String = "None",
    val lastApiError: String = "None",
    val bannerAdTest: String = "Ready",
    val interstitialAdTest: String = "Ready",
)

@HiltViewModel
class MainViewModel @Inject constructor(
    val appConfig: AppConfig,
    private val footballRepository: FootballRepository,
    private val tvGuideRepository: TvGuideRepository,
    private val settingsRepository: SettingsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val appConfigRepository: AppConfigRepository,
    private val apiService: ApiService,
    private val linkValidator: ProviderLinkValidator,
    private val legalPlaybackGate: LegalPlaybackGate,
    private val adFrequencyCapManager: AdFrequencyCapManager,
    private val apiDiagnosticsManager: ApiDiagnosticsManager
) : ViewModel() {
    private val _settingsLoaded = MutableStateFlow(false)
    val settingsLoaded = _settingsLoaded.asStateFlow()

    val settings: StateFlow<UserSettings> = settingsRepository.observeSettings()
        .onEach { _settingsLoaded.value = true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())

    val favorites: StateFlow<Set<String>> = favoritesRepository.observeFavoriteMatchIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _liveMatches = MutableStateFlow<DataState<List<FootballMatch>>>(DataState.Loading)
    val liveMatches = _liveMatches.asStateFlow()
    private val _upcomingMatches = MutableStateFlow<DataState<List<FootballMatch>>>(DataState.Loading)
    val upcomingMatches = _upcomingMatches.asStateFlow()
    private val _competitions = MutableStateFlow<DataState<List<Competition>>>(DataState.Loading)
    val competitions = _competitions.asStateFlow()
    private val _scores = MutableStateFlow<DataState<List<FootballMatch>>>(DataState.Loading)
    val scores = _scores.asStateFlow()
    private val _scoreFilter = MutableStateFlow(ScoreFilter.LIVE)
    val scoreFilter = _scoreFilter.asStateFlow()
    private val _matchDetail = MutableStateFlow<DataState<FootballMatch>>(DataState.Loading)
    val matchDetail = _matchDetail.asStateFlow()
    private val _tvCompetitions = MutableStateFlow<DataState<List<TvGuideCompetition>>>(DataState.Loading)
    val tvCompetitions = _tvCompetitions.asStateFlow()
    private val _tvSchedule = MutableStateFlow<DataState<List<TvScheduleItem>>>(DataState.Loading)
    val tvSchedule = _tvSchedule.asStateFlow()
    private val _remoteConfig = MutableStateFlow<DataState<AppRemoteConfig>>(DataState.Success(AppRemoteConfig()))
    val remoteConfig = _remoteConfig.asStateFlow()
    private val _diagnostics = MutableStateFlow(DiagnosticsState())
    val diagnostics = _diagnostics.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _lastUpdated = MutableStateFlow(0L)
    val lastUpdated = _lastUpdated.asStateFlow()

    val apiSummary: StateFlow<DiagnosticsSummary> = apiDiagnosticsManager.summary

    val regions = listOf(
        Region("Africa", "Africa", "CAF competitions and regional football", "AF"),
        Region("Europe", "Europe", "European leagues and continental nights", "EU"),
        Region("Asia", "Asia", "AFC fixtures and regional schedules", "AS"),
        Region("North America", "North America", "MLS and regional competitions", "NA"),
        Region("South America", "South America", "South American fixtures and guide", "SA"),
        Region("Oceania", "Oceania", "OFC and regional coverage", "OC"),
    )

    init {
        viewModelScope.launch { adFrequencyCapManager.markAppOpened() }
        loadRemoteConfig()
        loadHome()
        loadScores(ScoreFilter.LIVE)
        loadTvGuide()
        startAutoRefreshLoop()
    }

    private fun startAutoRefreshLoop() = viewModelScope.launch {
        while (true) {
            val hasLiveMatches = (_liveMatches.value as? DataState.Success)?.data?.any { 
                it.status == com.matchpulse.live.domain.model.MatchStatus.LIVE || 
                it.status == com.matchpulse.live.domain.model.MatchStatus.HALFTIME 
            } ?: false
            
            val delayMs = if (hasLiveMatches) 60_000L else 900_000L // 1 min for live, 15 min for scheduled
            kotlinx.coroutines.delay(delayMs)
            
            if (settings.value.autoRefreshEnabled) {
                loadHome(force = false)
                if (_scoreFilter.value == ScoreFilter.LIVE || _scoreFilter.value == ScoreFilter.TODAY) {
                    loadScores(force = false)
                }
            }
        }
    }

    fun retryAll() {
        loadHome(force = true)
        loadScores(force = true)
        loadTvGuide(force = true)
    }

    fun nextRouteFor(settings: UserSettings): String = when {
        !settings.onboardingCompleted -> com.matchpulse.live.core.navigation.Routes.Onboarding
        settings.selectedRegion.isNullOrBlank() -> com.matchpulse.live.core.navigation.Routes.Region
        !settings.termsAccepted -> com.matchpulse.live.core.navigation.Routes.Terms
        else -> com.matchpulse.live.core.navigation.Routes.Home
    }

    fun loadRemoteConfig(force: Boolean = false) = viewModelScope.launch {
        appConfigRepository.getRemoteConfig(force).collect { _remoteConfig.value = it }
    }

    fun loadHome(force: Boolean = false) {
        if (force) _isRefreshing.value = true
        viewModelScope.launch { 
            footballRepository.getLiveMatches(force).collect { 
                _liveMatches.value = it
                if (it is DataState.Success || it is DataState.Error) {
                    _isRefreshing.value = false
                    if (it is DataState.Success) _lastUpdated.value = System.currentTimeMillis()
                }
            } 
        }
        viewModelScope.launch { footballRepository.getUpcomingMatches(force).collect { _upcomingMatches.value = it } }
        viewModelScope.launch { footballRepository.getCompetitions(settings.value.selectedRegion, null).collect { _competitions.value = it } }
    }

    fun loadScores(filter: ScoreFilter = _scoreFilter.value, force: Boolean = false) {
        _scoreFilter.value = filter
        if (force) _isRefreshing.value = true
        viewModelScope.launch {
            val flow = when (filter) {
                ScoreFilter.LIVE -> footballRepository.getLiveMatches(force)
                ScoreFilter.TODAY -> footballRepository.getTodayMatches(force)
                ScoreFilter.UPCOMING -> footballRepository.getUpcomingMatches(force)
                ScoreFilter.FINISHED -> footballRepository.getFinishedMatches(force)
            }
            flow.collect { 
                _scores.value = it
                if (it is DataState.Success || it is DataState.Error) {
                    _isRefreshing.value = false
                    if (it is DataState.Success) _lastUpdated.value = System.currentTimeMillis()
                }
            }
        }
    }

    fun loadMatch(matchId: String, force: Boolean = false) = viewModelScope.launch {
        footballRepository.getMatchDetail(matchId, force).collect { _matchDetail.value = it }
    }

    fun loadTvGuide(force: Boolean = false) = viewModelScope.launch {
        tvGuideRepository.getTvGuideCompetitions(settings.value.selectedRegion, null).collect { _tvCompetitions.value = it }
    }

    fun loadTvSchedule(competitionId: String, force: Boolean = false) = viewModelScope.launch {
        tvGuideRepository.getTvSchedule(competitionId, force).collect { _tvSchedule.value = it }
    }

    fun completeOnboarding() = viewModelScope.launch { settingsRepository.setOnboardingCompleted(true) }
    fun selectRegion(regionId: String) = viewModelScope.launch { settingsRepository.updateSelectedRegion(regionId) }
    fun acceptTerms() = viewModelScope.launch { settingsRepository.setTermsAccepted(true) }
    fun toggleDarkMode(value: Boolean) = viewModelScope.launch { settingsRepository.updateDarkMode(value) }
    fun toggleAutoRefresh(value: Boolean) = viewModelScope.launch { settingsRepository.updateAutoRefresh(value) }
    fun toggleDataSaver(value: Boolean) = viewModelScope.launch { settingsRepository.updateDataSaver(value) }
    fun togglePush(value: Boolean) = viewModelScope.launch { settingsRepository.updatePushNotifications(value) }
    fun resetOnboardingDemo() = viewModelScope.launch { settingsRepository.resetOnboardingDemo() }
    fun toggleFavorite(matchId: String) = viewModelScope.launch { favoritesRepository.toggleMatchFavorite(matchId) }
    fun recordMajorNavigation() = viewModelScope.launch { adFrequencyCapManager.recordMajorNavigation() }

    fun providerMessage(provider: TvProvider): String? =
        linkValidator.validationMessage(provider, appConfig.enableLegalProviderLinks)

    fun playbackMessage(): String = legalPlaybackGate.disabledMessage()

    fun testBackendHealth() = viewModelScope.launch {
        runDiagnostic("Backend health") { apiService.health().status }
    }

    fun testProviderHealth() = viewModelScope.launch {
        runDiagnostic("Provider health") { apiService.providerHealth().status }
    }

    fun testLiveMatches() = viewModelScope.launch {
        runDiagnostic("Live matches") { apiService.liveMatches().size.toString() + " matches" }
    }

    fun testTodayMatches() = viewModelScope.launch {
        runDiagnostic("Today matches") { apiService.todayMatches().size.toString() + " matches" }
    }

    fun testCompetitions() = viewModelScope.launch {
        runDiagnostic("Competitions") { apiService.competitions().size.toString() + " competitions" }
    }

    fun markBannerTest() {
        _diagnostics.value = _diagnostics.value.copy(bannerAdTest = "Banner test requested")
    }

    fun markInterstitialTest() {
        _diagnostics.value = _diagnostics.value.copy(interstitialAdTest = "Interstitial test requested")
    }

    fun resetFrequencyCaps() = viewModelScope.launch {
        adFrequencyCapManager.resetFrequencyCapsForTesting()
        _diagnostics.value = _diagnostics.value.copy(interstitialAdTest = "Caps reset")
    }

    private suspend fun runDiagnostic(label: String, block: suspend () -> String) {
        try {
            val result = block()
            _diagnostics.value = _diagnostics.value.copy(
                backendHealth = if (label == "Backend health") result else _diagnostics.value.backendHealth,
                providerHealth = if (label == "Provider health") result else _diagnostics.value.providerHealth,
                lastSuccessfulCall = "$label: $result",
                lastApiError = "None",
            )
        } catch (throwable: Throwable) {
            _diagnostics.value = _diagnostics.value.copy(lastApiError = NetworkErrorMapper.userMessage(throwable))
        }
    }
}
