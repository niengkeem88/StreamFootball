package com.matchpulse.live.core.ads

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.frequencyStore by preferencesDataStore(name = "ad_frequency")

@Singleton
class AdFrequencyCapManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val policy = AdFrequencyPolicy()

    private object Keys {
        val sessionStart = longPreferencesKey("session_start")
        val lastInterstitial = longPreferencesKey("last_interstitial")
        val sessionCount = intPreferencesKey("session_count")
        val dailyCount = intPreferencesKey("daily_count")
        val lastDay = longPreferencesKey("last_day")
        val navigationCount = intPreferencesKey("navigation_count")
        val interstitialEnabled = booleanPreferencesKey("interstitial_enabled")
    }

    val isInterstitialEnabled: Flow<Boolean> = context.frequencyStore.data.map { it[Keys.interstitialEnabled] ?: true }

    suspend fun canShowInterstitial(): Boolean {
        val prefs = context.frequencyStore.data.first()
        val now = System.currentTimeMillis()

        val sessionStart = prefs[Keys.sessionStart] ?: now
        val lastInterstitial = prefs[Keys.lastInterstitial] ?: 0L
        val sessionCount = prefs[Keys.sessionCount] ?: 0
        val dailyCount = prefs[Keys.dailyCount] ?: 0
        val lastDay = prefs[Keys.lastDay] ?: 0L
        val navigationCount = prefs[Keys.navigationCount] ?: 0
        val enabled = prefs[Keys.interstitialEnabled] ?: true

        if (!enabled) return false
        if ((now - sessionStart) < policy.firstSessionDelayMs) return false
        if ((now - lastInterstitial) < policy.minGapBetweenMs) return false
        if (sessionCount >= policy.maxPerSession) return false

        val isNewDay = lastDay > 0 && !isSameDay(lastDay, now)
        val effectiveDailyCount = if (isNewDay) 0 else dailyCount
        if (effectiveDailyCount >= policy.maxPerDay) return false
        if (navigationCount < policy.minNavigationsBetween) return false

        return true
    }

    suspend fun recordInterstitialShown() {
        context.frequencyStore.edit { prefs ->
            val now = System.currentTimeMillis()
            prefs[Keys.lastInterstitial] = now
            prefs[Keys.sessionCount] = (prefs[Keys.sessionCount] ?: 0) + 1

            val lastDay = prefs[Keys.lastDay] ?: 0L
            if (lastDay == 0L || !isSameDay(lastDay, now)) {
                prefs[Keys.dailyCount] = 1
            } else {
                prefs[Keys.dailyCount] = (prefs[Keys.dailyCount] ?: 0) + 1
            }
            prefs[Keys.lastDay] = now
            prefs[Keys.navigationCount] = 0
        }
    }

    suspend fun recordNavigation() {
        context.frequencyStore.edit { prefs ->
            prefs[Keys.navigationCount] = (prefs[Keys.navigationCount] ?: 0) + 1
        }
    }

    suspend fun resetSession() {
        context.frequencyStore.edit { prefs ->
            prefs[Keys.sessionStart] = System.currentTimeMillis()
            prefs[Keys.sessionCount] = 0
            prefs[Keys.navigationCount] = 0
        }
    }

    suspend fun enableInterstitial(enabled: Boolean) {
        context.frequencyStore.edit { prefs ->
            prefs[Keys.interstitialEnabled] = enabled
        }
    }

    suspend fun disableForSession() {
        enableInterstitial(false)
    }

    private fun isSameDay(millis1: Long, millis2: Long): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = millis1 }
        val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = millis2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
            cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }
}
