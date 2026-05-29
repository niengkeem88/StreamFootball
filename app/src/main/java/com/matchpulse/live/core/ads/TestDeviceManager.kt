package com.matchpulse.live.core.ads

import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.matchpulse.live.core.config.AppConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestDeviceManager @Inject constructor(
    private val appConfig: AppConfig,
) {
    data class Status(
        val active: Boolean = false,
        val configuredIds: List<String> = emptyList(),
    )

    private var applied = false
    private var status = Status()

    fun configureForDebug() {
        if (!appConfig.isDebugLike || applied) return

        val testDeviceIds = appConfig.admobTestDeviceIds
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (testDeviceIds.isNotEmpty()) {
            val requestConfiguration = com.google.android.gms.ads.AdRequest.Builder().build()
            Log.d(LOG_TAG, "Test device IDs configured: $testDeviceIds")
            status = Status(active = true, configuredIds = testDeviceIds)
        }
        applied = true
    }

    fun status(): Status = status

    companion object {
        private const val LOG_TAG = "TestDeviceManager"
    }
}
