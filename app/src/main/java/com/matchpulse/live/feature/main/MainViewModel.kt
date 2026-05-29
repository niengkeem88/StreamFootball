package com.matchpulse.live.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matchpulse.live.core.ads.AdFrequencyCapManager
import com.matchpulse.live.core.datastore.UserPreferencesRepository
import com.matchpulse.live.core.datastore.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val adFrequencyCapManager: AdFrequencyCapManager,
) : ViewModel() {

    val settings: StateFlow<UserSettings> = userPreferencesRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())

    fun completeOnboarding() {
        viewModelScope.launch { userPreferencesRepository.setOnboardingCompleted(true) }
    }

    fun acceptTerms() {
        viewModelScope.launch { userPreferencesRepository.setTermsAccepted(true) }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setDarkMode(enabled) }
    }

    fun recordNavigation() {
        viewModelScope.launch { adFrequencyCapManager.recordNavigation() }
    }

    suspend fun canShowInterstitial(): Boolean = adFrequencyCapManager.canShowInterstitial()

    fun recordInterstitialShown() {
        viewModelScope.launch { adFrequencyCapManager.recordInterstitialShown() }
    }
}
