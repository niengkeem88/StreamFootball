package com.matchpulse.live.core.ads

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterstitialAdManager @Inject constructor(
    private val adMobManager: AdMobManager,
    private val capManager: AdFrequencyCapManager,
) {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var isShowing = false
    private var lastEvent = "Not preloaded"

    fun preload(activity: Activity) {
        val config = adMobManager.adConfig()
        if (!config.enabled) {
            lastEvent = "Preload skipped: ${config.disabledReason ?: "ads disabled"}"
            Log.d(ADS_LOG_TAG, lastEvent)
            return
        }
        if (isLoading || interstitialAd != null) {
            Log.d(ADS_LOG_TAG, "Interstitial preload skipped: loaded=$isLoaded loading=$isLoading")
            return
        }
        isLoading = true
        lastEvent = "Loading interstitial"
        Log.d(ADS_LOG_TAG, "Requesting interstitial ${AdConfig.mask(config.interstitialId)}")
        InterstitialAd.load(
            activity,
            config.interstitialId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                    lastEvent = "Interstitial loaded"
                    Log.d(ADS_LOG_TAG, lastEvent)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                    lastEvent = "Interstitial failed: ${error.message}"
                    Log.w(ADS_LOG_TAG, lastEvent)
                }
            }
        )
    }

    suspend fun canShow(): Boolean =
        adMobManager.adConfig().enabled && interstitialAd != null && !isShowing && capManager.canShowInterstitial()

    suspend fun showIfAllowed(
        activity: Activity,
        onContinue: () -> Unit,
    ) {
        showIfAvailable(activity, AdPlacement.TV_SCHEDULE_DETAIL, onContinue)
    }

    suspend fun showIfAvailable(
        activity: Activity,
        placement: AdPlacement,
        onContinue: () -> Unit,
    ) {
        val config = adMobManager.adConfig()
        if (!config.enabled) {
            lastEvent = "Interstitial skipped at $placement: ${config.disabledReason ?: "ads disabled"}"
            Log.d(ADS_LOG_TAG, lastEvent)
            onContinue()
            return
        }
        if (isShowing) {
            lastEvent = "Interstitial already showing"
            Log.d(ADS_LOG_TAG, lastEvent)
            onContinue()
            return
        }
        val ad = interstitialAd
        if (ad == null) {
            lastEvent = "Interstitial unavailable at $placement; navigation continues"
            Log.d(ADS_LOG_TAG, lastEvent)
            onContinue()
            preload(activity)
            return
        }
        val frequencyStatus = capManager.frequencyStatus()
        if (!frequencyStatus.canShow) {
            lastEvent = "Interstitial blocked at $placement: ${frequencyStatus.blockReason}"
            Log.d(ADS_LOG_TAG, lastEvent)
            onContinue()
            return
        }

        interstitialAd = null
        isShowing = true
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                lastEvent = "Interstitial shown at $placement"
                Log.d(ADS_LOG_TAG, lastEvent)
            }

            override fun onAdClicked() {
                Log.d(ADS_LOG_TAG, "Interstitial clicked at $placement")
            }

            override fun onAdDismissedFullScreenContent() {
                isShowing = false
                lastEvent = "Interstitial dismissed at $placement"
                Log.d(ADS_LOG_TAG, lastEvent)
                onContinue()
                preload(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                isShowing = false
                lastEvent = "Interstitial failed to show at $placement: ${adError.message}"
                Log.w(ADS_LOG_TAG, lastEvent)
                onContinue()
                preload(activity)
            }
        }
        capManager.recordInterstitialShown()
        ad.show(activity)
    }

    fun clear() {
        interstitialAd = null
        isLoading = false
        isShowing = false
        lastEvent = "Interstitial cleared"
        Log.d(ADS_LOG_TAG, lastEvent)
    }

    fun status(): InterstitialStatus = InterstitialStatus(
        loaded = interstitialAd != null,
        loading = isLoading,
        showing = isShowing,
        lastEvent = lastEvent,
    )

    private val isLoaded: Boolean
        get() = interstitialAd != null
}

data class InterstitialStatus(
    val loaded: Boolean,
    val loading: Boolean,
    val showing: Boolean,
    val lastEvent: String,
)
