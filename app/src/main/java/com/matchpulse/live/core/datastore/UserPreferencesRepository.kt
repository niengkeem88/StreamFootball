package com.matchpulse.live.core.datastore

import com.matchpulse.live.domain.model.UserSettings
import com.matchpulse.live.domain.repository.FavoritesRepository
import com.matchpulse.live.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataSource: UserPreferencesDataSource,
) : SettingsRepository, FavoritesRepository {
    override fun observeSettings(): Flow<UserSettings> = dataSource.settings

    override suspend fun updateDarkMode(enabled: Boolean) = dataSource.setDarkModeEnabled(enabled)
    override suspend fun updateAutoRefresh(enabled: Boolean) = dataSource.setAutoRefreshEnabled(enabled)
    override suspend fun updateDataSaver(enabled: Boolean) = dataSource.setDataSaverEnabled(enabled)
    override suspend fun updatePushNotifications(enabled: Boolean) = dataSource.setPushNotificationsEnabled(enabled)
    override suspend fun updateSelectedRegion(regionId: String) = dataSource.setSelectedRegion(regionId)
    override suspend fun setOnboardingCompleted(completed: Boolean) = dataSource.setOnboardingCompleted(completed)
    override suspend fun setTermsAccepted(accepted: Boolean) = dataSource.setTermsAccepted(accepted)
    override suspend fun resetOnboardingDemo() = dataSource.resetDemo()

    override fun observeFavoriteMatchIds(): Flow<Set<String>> =
        dataSource.settings.map { it.favoriteMatchIds }

    override suspend fun toggleMatchFavorite(matchId: String) {
        val next = dataSource.favoriteIds().toMutableSet()
        if (!next.add(matchId)) next.remove(matchId)
        dataSource.setFavoriteIds(next)
    }

    override suspend fun isFavorite(matchId: String): Boolean =
        dataSource.favoriteIds().contains(matchId)
}
