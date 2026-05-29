package com.matchpulse.live.ui

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
// ...existing imports...
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.matchpulse.live.BuildConfig
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import androidx.compose.runtime.mutableStateOf

/**
 * Minimal, reusable WebView composable optimized for loading the local API-Football widget HTML.
 * Preserves WebView instance across recompositions to avoid unnecessary reloads and preserves state.
 */
@Composable
fun WidgetWebView(
    assetUrl: String = "file:///android_asset/api_football_widget.html",
    modifier: Modifier = Modifier,
    height: Dp = 520.dp,
) {
    val context = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val density = LocalDensity.current
    var contentHeight by remember { mutableStateOf(height) }
    val jsInterfaceAdded = remember { mutableStateOf(false) }

    // Remember a single WebView instance to preserve state and avoid repeated reloads
    val webView = remember {
        WebView(context).apply {
            // Settings tuned for the widget
            settings?.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadsImagesAutomatically = true
                mediaPlaybackRequiresUserGesture = false
                useWideViewPort = true
                loadWithOverviewMode = true
                // Prevent file access from web content for security
                allowFileAccess = false
                allowContentAccess = false
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }
            isHorizontalScrollBarEnabled = false
            // Match app dark background; avoid white flashes
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }
    }

    DisposableEffect(webView) {
        onDispose {
            try {
                webView.stopLoading()
                webView.loadUrl("about:blank")
                webView.removeAllViews()
                webView.destroy()
            } catch (_: Throwable) {}
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { webView },
            modifier = Modifier
                .fillMaxSize()
                .height(contentHeight)
        ) { view ->
            // Bridge used by the HTML to report measured height in pixels
            class JSBridge(private val densityScale: Float, private val onHeightPx: (Float) -> Unit) {
                @JavascriptInterface
                fun postHeight(px: String?) {
                    val p = px?.toFloatOrNull() ?: return
                    Handler(Looper.getMainLooper()).post { onHeightPx(p) }
                }
            }

            view.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    loading = true
                    error = null
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    loading = false
                    // Request height reporting once the page is finished
                    try { view?.evaluateJavascript("reportWidgetHeight();", null) } catch (_: Throwable) {}
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, rerr: WebResourceError?) {
                    if (request?.isForMainFrame == true) {
                        loading = false
                        error = rerr?.description?.toString() ?: "Load error"
                    }
                }
            }

            // Add JS bridge once
            if (!jsInterfaceAdded.value) {
                try {
                    val bridge = JSBridge(density.density) { px ->
                        // convert px to dp and update contentHeight on main thread
                        val dp = (px / density.density)
                        val newDp = dp.dp
                        // coerce to reasonable bounds
                        val min = 180.dp
                        val max = 2000.dp
                        contentHeight = when {
                            newDp < min -> min
                            newDp > max -> max
                            else -> newDp
                        }
                    }
                    view.addJavascriptInterface(bridge, "AndroidWidgetBridge")
                    jsInterfaceAdded.value = true
                } catch (_: Throwable) {}
            }

            // Initial load only if blank to avoid reloading on recomposition
            if (view.url == null || view.url == "about:blank") {
                try {
                    // Read the local asset, inject API key at runtime from BuildConfig to avoid committing key in source
                    val raw = context.assets.open("api_football_widget.html").use { it.reader(Charsets.UTF_8).readText() }
                    val injected = raw.replace("%%FOOTBALL_API_KEY%%", BuildConfig.FOOTBALL_API_KEY ?: "")
                    view.loadDataWithBaseURL("file:///android_asset/", injected, "text/html", "utf-8", null)
                } catch (t: Throwable) {
                    loading = false
                    error = t.message ?: "Failed to load widget"
                }
            }
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        if (error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card {
                    androidx.compose.foundation.layout.Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Unable to load football widget", color = Color.White)
                        Text(error ?: "Unknown error", color = Color.Gray)
                        Button(onClick = { error = null; loading = true; try { webView.reload() } catch(_ : Throwable){} }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

