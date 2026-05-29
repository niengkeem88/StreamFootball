package com.matchpulse.live.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * FootballWidgetScreen: small wrapper to host the API-Football widget inside the Home screen.
 * Keeps sizing responsive and matches app padding.
 */
@Composable
fun FootballWidgetScreen(modifier: Modifier = Modifier) {
    // Responsive height: ~60% of screen width, min 360dp
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenWidth = with(LocalDensity.current) { screenWidthDp.dp }
    val widgetHeight = (screenWidth * 0.60f).coerceAtLeast(360.dp)

    Column(modifier = modifier.fillMaxWidth()) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)) {
            WidgetWebView(height = widgetHeight, modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

