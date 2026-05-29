package com.matchpulse.live.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.matchpulse.live.core.ads.AdFrequencyPolicy
import com.matchpulse.live.domain.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.userPreferencesStore by preferencesDataStore(name = "matchpulse_preferences")

@Singleton
class UserPreferencesDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private object Keys {
        val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val selectedRegion = stringPreferencesKey("selected_region")
        val termsAccepted = booleanPreferencesKey("terms_accepted")
        val darkModeEnabled = booleanPreferencesKey("dark_mode_enabled")
        val pushNotificationsEnabled = booleanPreferencesKey("push_notifications_enabled")
        val autoRefreshEnabled = booleanPreferencesKey("auto_refresh_enabled")
        val dataSaverEnabled = booleanPreferencesKey("data_saver_enabled")
        val favoriteMatchIds = stringSetPreferencesKey("favorite_match_ids")
        val lastAppOpenTime = longPreferencesKey("last_app_open_time")
        val lastInterstitialShownAt = longPreferencesKey("last_interstitial_shown_at")
        val interstitialSessionCount = intPreferencesKey("interstitial_session_count")
        val interstitialDailyCount = intPreferencesKey("interstitial_daily_count")
        val interstitialDailyKey = stringPreferencesKey("interstitial_daily_key")
        val majorNavigationCount = intPreferencesKey("major_navigation_count")
    }

    val settings: Flow<UserSettings> = context.userPreferencesStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            UserSettings(
                onboardingCompleted = preferences[Keys.onboardingCompleted] ?: false,
                selectedRegion = preferences[Keys.selectedRegion],
                termsAccepted = preferences[Keys.termsAccepted] ?: false,
                darkModeEnabled = preferences[Keys.darkModeEnabled] ?: true,
                pushNotificationsEnabled = preferences[Keys.pushNotificationsEnabled] ?: false,
                autoRefreshEnabled = preferences[Keys.autoRefreshEnabled] ?: true,
                dataSaverEnabled = preferences[Keys.dataSaverEnabled] ?: false,
                favoriteMatchIds = preferences[Keys.favoriteMatchIds] ?: emptySet(),
            )
        }

    suspend fun setOnboardingCompleted(value: Boolean) = setBoolean(Keys.onboardingCompleted, value)
    suspend fun setTermsAccepted(value: Boolean) = setBoolean(Keys.termsAccepted, value)
    suspend fun setSelectedRegion(value: String) = setString(Keys.selectedRegion, value)
    suspend fun setDarkModeEnabled(value: Boolean) = setBoolean(Keys.darkModeEnabled, value)
    suspend fun setPushNotificationsEnabled(value: Boolean) = setBoolean(Keys.pushNotificationsEnabled, value)
    suspend fun setAutoRefreshEnabled(value: Boolean) = setBoolean(Keys.autoRefreshEnabled, value)
    suspend fun setDataSaverEnabled(value: Boolean) = setBoolean(Keys.dataSaverEnabled, value)

    suspend fun favoriteIds(): Set<String> = settings.first().favoriteMatchIds

    suspend fun setFavoriteIds(ids: Set<String>) {
        context.userPreferencesStore.edit { it[Keys.favoriteMatchIds] = ids }
    }

    suspend fun markAppOpened(nowMillis: Long) {
        context.userPreferencesStore.edit {
            it[Keys.lastAppOpenTime] = nowMillis
            it[Keys.interstitialSessionCount] = 0
            it[Keys.majorNavigationCount] = 0
        }
    }

    suspend fun adCounters(): AdCounterSnapshot {
        val preferences = context.userPreferencesStore.data.first()
        return AdCounterSnapshot(
            lastAppOpenTime = preferences[Keys.lastAppOpenTime] ?: 0L,
            lastInterstitialShownAt = preferences[Keys.lastInterstitialShownAt] ?: 0L,
            sessionCount = preferences[Keys.interstitialSessionCount] ?: 0,
            dailyCount = preferences[Keys.interstitialDailyCount] ?: 0,
            dailyKey = preferences[Keys.interstitialDailyKey] ?: "",
            majorNavigationCount = preferences[Keys.majorNavigationCount] ?: 0,
        )
    }

    suspend fun recordMajorNavigation() {
        context.userPreferencesStore.edit {
            it[Keys.majorNavigationCount] = (it[Keys.majorNavigationCount] ?: 0) + 1
        }
    }

    suspend fun recordInterstitialShown(nowMillis: Long, dayKey: String) {
        context.userPreferencesStore.edit {
            val existingKey = it[Keys.interstitialDailyKey] ?: ""
            val dailyCount = if (existingKey == dayKey) (it[Keys.interstitialDailyCount] ?: 0) + 1 else 1
            it[Keys.lastInterstitialShownAt] = nowMillis
            it[Keys.interstitialSessionCount] = (it[Keys.interstitialSessionCount] ?: 0) + 1
            it[Keys.interstitialDailyKey] = dayKey
            it[Keys.interstitialDailyCount] = dailyCount
            it[Keys.majorNavigationCount] = 0
        }
    }

    suspend fun resetAdFrequencyCapsForTesting(nowMillis: Long) {
        context.userPreferencesStore.edit {
            it[Keys.lastAppOpenTime] = nowMillis - AdFrequencyPolicy.FIRST_SESSION_DELAY_MS
            it[Keys.lastInterstitialShownAt] = 0L
            it[Keys.interstitialSessionCount] = 0
            it[Keys.interstitialDailyCount] = 0
            it[Keys.majorNavigationCount] = AdFrequencyPolicy.MAJOR_NAVIGATION_GAP
        }
    }

    suspend fun resetDemo() {
        context.userPreferencesStore.edit {
            it.remove(Keys.onboardingCompleted)
            it.remove(Keys.selectedRegion)
            it.remove(Keys.termsAccepted)
        }
    }

    private suspend fun setBoolean(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, value: Boolean) {
        context.userPreferencesStore.edit { it[key] = value }
    }

    private suspend fun setString(key: androidx.datastore.preferences.core.Preferences.Key<String>, value: String) {
        context.userPreferencesStore.edit { it[key] = value }
    }
}

data class AdCounterSnapshot(
    val lastAppOpenTime: Long,
    val lastInterstitialShownAt: Long,
    val sessionCount: Int,
    val dailyCount: Int,
    val dailyKey: String,
    val majorNavigationCount: Int,
)
