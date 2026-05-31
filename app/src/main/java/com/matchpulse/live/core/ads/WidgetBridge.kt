package com.matchpulse.live.core.ads

import android.webkit.JavascriptInterface

class WidgetBridge(private val onTabChanged: () -> Unit) {
    @JavascriptInterface
    fun onTabChanged() {
        onTabChanged()
    }
}
