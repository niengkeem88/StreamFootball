package com.matchpulse.live.core.ads

import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.matchpulse.live.core.config.AppConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestDeviceManager @Inject constructor(
    private val appConfig: AppConfig,
) {
    private var configuredIds: List<String> = emptyList()

    fun configureForDebug() {
        val runtimeConfig = AdConfig.from(appConfig)
        if (!runtimeConfig.debugAds) {
            configuredIds = emptyList()
            Log.d(ADS_LOG_TAG, "Release build detected; test device IDs not forced.")
            return
        }

        // Test ads are safe to click while developing. Do not repeatedly click production ads.
        configuredIds = (listOf(AdRequest.DEVICE_ID_EMULATOR) + runtimeConfig.testDeviceIds).distinct()
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(configuredIds)
                .build()
        )
        Log.d(ADS_LOG_TAG, "Configured debug test devices: ${configuredIds.joinToString()}")
    }

    fun status(): TestDeviceStatus = TestDeviceStatus(
        active = appConfig.isDebugBuild && configuredIds.isNotEmpty(),
        configuredIds = configuredIds,
    )
}

data class TestDeviceStatus(
    val active: Boolean,
    val configuredIds: List<String>,
)
