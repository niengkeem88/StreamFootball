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
class InterstitialAdManager @Inject constructor() {
    private var interstitialAd: InterstitialAd? = null
    private var isShowing = false

    fun loadAd(adUnitId: String) {
        if (adUnitId.isBlank() || interstitialAd != null) return
        Log.d(LOG_TAG, "Loading interstitial ad: $adUnitId")
        // Ad loading is done via Activity context; we'll load on show attempt
    }

    fun show(activity: Activity, adUnitId: String, onDismissed: () -> Unit = {}) {
        if (isShowing) {
            Log.d(LOG_TAG, "Already showing an interstitial")
            onDismissed()
            return
        }

        if (interstitialAd != null) {
            isShowing = true
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    isShowing = false
                    interstitialAd = null
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    isShowing = false
                    interstitialAd = null
                    Log.e(LOG_TAG, "Interstitial failed to show: ${adError.message}")
                    onDismissed()
                }
            }
            interstitialAd?.show(activity)
        } else {
            // Load and show
            val request = AdRequest.Builder().build()
            InterstitialAd.load(activity, adUnitId, request, object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isShowing = true
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            isShowing = false
                            interstitialAd = null
                            onDismissed()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            isShowing = false
                            interstitialAd = null
                            Log.e(LOG_TAG, "Interstitial failed after load: ${adError.message}")
                            onDismissed()
                        }
                    }
                    ad.show(activity)
                }

                override fun onAdFailedToLoad(loadError: LoadAdError) {
                    Log.e(LOG_TAG, "Interstitial failed to load: ${loadError.message}")
                    onDismissed()
                }
            })
        }
    }

    companion object {
        private const val LOG_TAG = "InterstitialAdManager"
    }
}
