package com.matchpulse.live.core.ads

import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAdComposable(
    placement: AdPlacement,
    adMobManager: AdMobManager,
    modifier: Modifier = Modifier,
    adSize: AdSize? = null,
    onAdLoaded: () -> Unit = {},
    onAdFailedToLoad: (LoadAdError) -> Unit = {},
    onAdOpened: () -> Unit = {},
    onAdClicked: () -> Unit = {},
    onAdClosed: () -> Unit = {},
) {
    val config = adMobManager.adConfig()
    if (!config.enabled) {
        Log.d(ADS_LOG_TAG, "Banner skipped at $placement: ${config.disabledReason ?: "ads disabled"}")
        return
    }

    var visible by remember(placement, config.bannerId) { mutableStateOf(true) }
    if (!visible) return

    val context = LocalContext.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val effectiveAdSize = remember(screenWidthDp, adSize) {
        adSize ?: AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, screenWidthDp)
    }
    val adView = remember(placement, config.bannerId, effectiveAdSize) {
        AdView(context).apply {
            setAdSize(effectiveAdSize)
            adUnitId = config.bannerId
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            contentDescription = "Ad placement $placement"
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    visible = true
                    Log.d(ADS_LOG_TAG, "Banner loaded at $placement")
                    onAdLoaded()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    visible = false
                    Log.w(ADS_LOG_TAG, "Banner failed at $placement: ${error.message}")
                    onAdFailedToLoad(error)
                }

                override fun onAdOpened() {
                    Log.d(ADS_LOG_TAG, "Banner opened at $placement")
                    onAdOpened()
                }

                override fun onAdClicked() {
                    Log.d(ADS_LOG_TAG, "Banner clicked at $placement")
                    onAdClicked()
                }

                override fun onAdClosed() {
                    Log.d(ADS_LOG_TAG, "Banner closed at $placement")
                    onAdClosed()
                }
            }
            Log.d(ADS_LOG_TAG, "Requesting banner at $placement with ${AdConfig.mask(config.bannerId)}")
            loadAd(AdRequest.Builder().build())
        }
    }
    DisposableEffect(adView) {
        onDispose {
            adView.destroy()
            Log.d(ADS_LOG_TAG, "Banner destroyed at $placement")
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(effectiveAdSize.height.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { adView },
            update = { it.contentDescription = "Ad placement $placement" },
        )
    }
}

@Composable
fun NativeAdPlaceholderComposable(placement: AdPlacement, enabled: Boolean, modifier: Modifier = Modifier) {
    if (!enabled) return
    AssistChip(
        modifier = modifier,
        onClick = {},
        label = { Text("Sponsored") },
    )
}
