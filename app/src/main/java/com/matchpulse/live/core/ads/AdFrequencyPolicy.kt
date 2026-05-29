package com.matchpulse.live.core.ads

data class AdFrequencyPolicy(
    val firstSessionDelayMs: Long = 90_000L,
    val minGapBetweenMs: Long = 180_000L,
    val maxPerSession: Int = 3,
    val maxPerDay: Int = 6,
    val minNavigationsBetween: Int = 4,
)
