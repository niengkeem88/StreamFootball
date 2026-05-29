package com.matchpulse.live.feature.main

import androidx.lifecycle.ViewModel
import com.matchpulse.live.core.config.AppConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val appConfig: AppConfig,
) : ViewModel()
