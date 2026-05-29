package com.matchpulse.live.core.ads

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerAd(
    adUnitId: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val adView = remember {
        AdView(context).apply {
            adUnitId = adUnitId
            adSize = AdSize.SMART_BANNER
        }
    }

    DisposableEffect(adView) {
        adView.loadAd(AdRequest.Builder().build())
        onDispose {
            adView.destroy()
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        AndroidView(
            factory = { adView },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
