package com.matchpulse.live.core.ads

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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
    val adRequest = remember { AdRequest.Builder().build() }
    val adView = remember {
        AdView(context).also { view ->
            view.adUnitId = adUnitId
            view.adSize = AdSize.SMART_BANNER
            view.loadAd(adRequest)
        }
    }

    DisposableEffect(adView) {
        onDispose { adView.destroy() }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        AndroidView(
            factory = { adView },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
