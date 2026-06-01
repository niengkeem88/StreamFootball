package com.matchpulse.live.core.ads

import android.webkit.JavascriptInterface

class WidgetBridge(private val callback: () -> Unit) {
    @JavascriptInterface
    fun onTabChanged() {
        callback()
    }
}
