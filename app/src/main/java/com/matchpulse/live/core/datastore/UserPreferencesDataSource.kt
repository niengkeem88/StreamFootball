package com.matchpulse.live.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsStore by preferencesDataStore(name = "user_settings")

data class UserSettings(
    val onboardingCompleted: Boolean = false,
    val termsAccepted: Boolean = false,
    val darkMode: Boolean = false,
)

@Singleton
class UserPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val termsAccepted = booleanPreferencesKey("terms_accepted")
        val darkMode = booleanPreferencesKey("dark_mode")
    }

    val settings: Flow<UserSettings> = context.settingsStore.data.map { prefs ->
        UserSettings(
            onboardingCompleted = prefs[Keys.onboardingCompleted] ?: false,
            termsAccepted = prefs[Keys.termsAccepted] ?: false,
            darkMode = prefs[Keys.darkMode] ?: false,
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.settingsStore.edit { it[Keys.onboardingCompleted] = completed }
    }

    suspend fun setTermsAccepted(accepted: Boolean) {
        context.settingsStore.edit { it[Keys.termsAccepted] = accepted }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.settingsStore.edit { it[Keys.darkMode] = enabled }
    }

    suspend fun onboardingCompleted(): Boolean =
        context.settingsStore.data.first()[Keys.onboardingCompleted] ?: false

    suspend fun termsAccepted(): Boolean =
        context.settingsStore.data.first()[Keys.termsAccepted] ?: false
}
