package com.matchpulse.live.core.ads

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

class WidgetBridge(private val callback: () -> Unit) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onTabChanged() {
        // @JavascriptInterface runs on a WebView internal thread
        // Dispatch to main thread to safely trigger Compose/AdMob code
        mainHandler.post {
            callback()
        }
    }
}
