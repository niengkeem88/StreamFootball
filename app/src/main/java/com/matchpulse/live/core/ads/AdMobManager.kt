package com.matchpulse.live.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.matchpulse.live.core.config.AppConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdMobManager @Inject constructor(
    private val appConfig: AppConfig,
    private val testDeviceManager: TestDeviceManager,
    private val consentManager: ConsentManager,
) {
    private var initialized = false
    private var lastEvent = "Not initialized"

    fun gatherConsentAndInitialize(activity: Activity) {
        consentManager.gatherConsent(activity) { error ->
            if (error == null) {
                if (consentManager.canRequestAds) {
                    initialize(activity.applicationContext)
                } else {
                    lastEvent = "Consent gathered, but ads not allowed"
                    Log.d(LOG_TAG, lastEvent)
                }
            } else {
                lastEvent = "Consent gathering failed: ${error.message}"
                Log.e(LOG_TAG, lastEvent)
                if (consentManager.canRequestAds) {
                    initialize(activity.applicationContext)
                }
            }
        }
    }

    fun initialize(context: Context) {
        if (initialized) return
        val config = adConfig()
        testDeviceManager.configureForDebug()

        if (!config.enabled) {
            lastEvent = "Ads disabled: ${config.disabledReason}"
            Log.w(LOG_TAG, lastEvent)
            return
        }
        runCatching {
            MobileAds.initialize(context) {
                initialized = true
                lastEvent = "Mobile Ads initialized"
                Log.d(LOG_TAG, lastEvent)
            }
        }.onFailure { throwable ->
            initialized = false
            lastEvent = "Mobile Ads initialization failed: ${throwable.message}"
            Log.w(LOG_TAG, lastEvent, throwable)
        }
    }

    fun adConfig(): AdRuntimeConfig = AdConfig.from(appConfig)
    fun isPrivacyOptionsRequired(): Boolean = consentManager.isPrivacyOptionsRequired()
    fun showPrivacyOptions(activity: Activity) {
        consentManager.showPrivacyOptionsForm(activity) { error ->
            if (error != null) Log.e(LOG_TAG, "Privacy options form error: ${error.message}")
        }
    }

    fun status(): AdMobStatus {
        val config = adConfig()
        val testStatus = testDeviceManager.status()
        return AdMobStatus(
            adsEnabled = config.enabled,
            debugAds = config.debugAds,
            initialized = initialized,
            bannerIdMasked = AdConfig.mask(config.bannerId),
            interstitialIdMasked = AdConfig.mask(config.interstitialId),
            nativeIdMasked = AdConfig.mask(config.nativeId),
            rewardedIdMasked = AdConfig.mask(config.rewardedId),
            testDeviceActive = testStatus.active,
            testDeviceCount = testStatus.configuredIds.size,
            canRequestAds = consentManager.canRequestAds,
            disabledReason = config.disabledReason,
            lastEvent = lastEvent,
        )
    }

    companion object {
        private const val LOG_TAG = "AdMobManager"
    }
}

data class AdMobStatus(
    val adsEnabled: Boolean,
    val debugAds: Boolean,
    val initialized: Boolean,
    val bannerIdMasked: String,
    val interstitialIdMasked: String,
    val nativeIdMasked: String,
    val rewardedIdMasked: String,
    val testDeviceActive: Boolean,
    val testDeviceCount: Int,
    val canRequestAds: Boolean,
    val disabledReason: String?,
    val lastEvent: String,
)
