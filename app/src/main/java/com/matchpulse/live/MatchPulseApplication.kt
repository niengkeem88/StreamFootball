package com.matchpulse.live

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MatchPulseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
