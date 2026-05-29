package com.matchpulse.live.core.ads

import com.matchpulse.live.core.datastore.AdCounterSnapshot
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdFrequencyPolicyTest {
    @Test
    fun blocksFirstNinetySecondsAndNavigationSpam() {
        val counters = AdCounterSnapshot(
            lastAppOpenTime = 1_000L,
            lastInterstitialShownAt = 0L,
            sessionCount = 0,
            dailyCount = 0,
            dailyKey = "2026-05-16",
            majorNavigationCount = 3,
        )

        // Still within first 90s (1000 + 90000 = 91000)
        assertFalse("Should block within first 90s", AdFrequencyPolicy.canShow(counters, 80_000L, "2026-05-16"))
        
        // After 90s but majorNavigationCount is only 3 (needs 4)
        assertFalse("Should block if navigation gap < 4", AdFrequencyPolicy.canShow(counters, 200_000L, "2026-05-16"))
    }

    @Test
    fun allowsAfterCooldownNavigationAndCaps() {
        val counters = AdCounterSnapshot(
            lastAppOpenTime = 1_000L,
            lastInterstitialShownAt = 1_000L,
            sessionCount = 1,
            dailyCount = 2,
            dailyKey = "2026-05-16",
            majorNavigationCount = 4,
        )

        // After 3 min (1000 + 180000 = 181000)
        assertTrue("Should allow after cooldown and 4 navigations", AdFrequencyPolicy.canShow(counters, 300_000L, "2026-05-16"))
    }
}
