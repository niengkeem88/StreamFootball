package com.matchpulse.live.core.datastore

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataSource: UserPreferencesDataSource,
) {
    val settings: Flow<UserSettings> = dataSource.settings

    suspend fun setOnboardingCompleted(completed: Boolean) = dataSource.setOnboardingCompleted(completed)
    suspend fun setTermsAccepted(accepted: Boolean) = dataSource.setTermsAccepted(accepted)
    suspend fun setDarkMode(enabled: Boolean) = dataSource.setDarkMode(enabled)
    suspend fun onboardingCompleted(): Boolean = dataSource.onboardingCompleted()
    suspend fun termsAccepted(): Boolean = dataSource.termsAccepted()
}
