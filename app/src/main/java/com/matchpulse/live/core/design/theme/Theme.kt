package com.matchpulse.live.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = ElectricBlue,
    secondary = NeonGreen,
    tertiary = PulseCyan,
    background = Navy900,
    surface = DarkSurface,
    error = ErrorRed,
)

private val LightColors = lightColorScheme(
    primary = Navy800,
    secondary = ElectricBlue,
    tertiary = PitchGreen,
    background = LightBg,
    surface = LightSurface,
    error = ErrorRed,
)

@Composable
fun MatchPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MatchPulseTypography,
        content = content,
    )
}
