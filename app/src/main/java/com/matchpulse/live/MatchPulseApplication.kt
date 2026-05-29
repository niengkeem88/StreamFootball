package com.matchpulse.live

import android.app.Application
import com.matchpulse.live.core.ads.AdMobManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MatchPulseApplication : Application() {
    @Inject lateinit var adMobManager: AdMobManager

    override fun onCreate() {
        super.onCreate()
        adMobManager.initialize(this)
    }
}
