package com.matchpulse.live.core.ads

import com.matchpulse.live.core.datastore.AdCounterSnapshot

object AdFrequencyPolicy {
    const val FIRST_SESSION_DELAY_MS = 90_000L
    const val MIN_GAP_MS = 3 * 60_000L
    const val MAX_PER_SESSION = 3
    const val MAX_PER_DAY = 6
    const val MAJOR_NAVIGATION_GAP = 4

    fun blockReason(counters: AdCounterSnapshot, nowMillis: Long, dayKey: String): String? {
        val dailyCount = if (counters.dailyKey == dayKey) counters.dailyCount else 0
        
        return when {
            counters.lastAppOpenTime <= 0 -> "App open time not recorded"
            nowMillis - counters.lastAppOpenTime < FIRST_SESSION_DELAY_MS -> {
                val remaining = (FIRST_SESSION_DELAY_MS - (nowMillis - counters.lastAppOpenTime)) / 1000
                "First session delay active: ${remaining}s remaining"
            }
            nowMillis - counters.lastInterstitialShownAt < MIN_GAP_MS -> {
                val remaining = (MIN_GAP_MS - (nowMillis - counters.lastInterstitialShownAt)) / 1000
                "Cooldown active: ${remaining}s remaining"
            }
            counters.sessionCount >= MAX_PER_SESSION -> "Session cap reached ($MAX_PER_SESSION)"
            dailyCount >= MAX_PER_DAY -> "Daily cap reached ($MAX_PER_DAY)"
            counters.majorNavigationCount < MAJOR_NAVIGATION_GAP -> 
                "Navigation gap active: ${counters.majorNavigationCount}/$MAJOR_NAVIGATION_GAP"
            else -> null
        }
    }

    fun canShow(counters: AdCounterSnapshot, nowMillis: Long, dayKey: String): Boolean =
        blockReason(counters, nowMillis, dayKey) == null
}
