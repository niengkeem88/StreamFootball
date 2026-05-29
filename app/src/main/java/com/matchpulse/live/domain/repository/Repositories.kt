package com.matchpulse.live.domain.repository

import com.matchpulse.live.domain.model.AppRemoteConfig
import com.matchpulse.live.domain.model.Competition
import com.matchpulse.live.domain.model.DataState
import com.matchpulse.live.domain.model.FootballMatch
import com.matchpulse.live.domain.model.TvGuideCompetition
import com.matchpulse.live.domain.model.TvScheduleItem
import com.matchpulse.live.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface FootballRepository {
    fun getLiveMatches(forceRefresh: Boolean = false): Flow<DataState<List<FootballMatch>>>
    fun getTodayMatches(forceRefresh: Boolean = false): Flow<DataState<List<FootballMatch>>>
    fun getUpcomingMatches(forceRefresh: Boolean = false): Flow<DataState<List<FootballMatch>>>
    fun getFinishedMatches(forceRefresh: Boolean = false): Flow<DataState<List<FootballMatch>>>
    fun getMatchDetail(matchId: String, forceRefresh: Boolean = false): Flow<DataState<FootballMatch>>
    fun getCompetitions(region: String? = null, category: String? = null): Flow<DataState<List<Competition>>>
}

interface TvGuideRepository {
    fun getTvGuideCompetitions(region: String? = null, category: String? = null): Flow<DataState<List<TvGuideCompetition>>>
    fun getTvSchedule(competitionId: String, forceRefresh: Boolean = false): Flow<DataState<List<TvScheduleItem>>>
}

interface SettingsRepository {
    fun observeSettings(): Flow<UserSettings>
    suspend fun updateDarkMode(enabled: Boolean)
    suspend fun updateAutoRefresh(enabled: Boolean)
    suspend fun updateDataSaver(enabled: Boolean)
    suspend fun updatePushNotifications(enabled: Boolean)
    suspend fun updateSelectedRegion(regionId: String)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setTermsAccepted(accepted: Boolean)
    suspend fun resetOnboardingDemo()
}

interface FavoritesRepository {
    fun observeFavoriteMatchIds(): Flow<Set<String>>
    suspend fun toggleMatchFavorite(matchId: String)
    suspend fun isFavorite(matchId: String): Boolean
}

interface AppConfigRepository {
    fun getRemoteConfig(forceRefresh: Boolean = false): Flow<DataState<AppRemoteConfig>>
}
