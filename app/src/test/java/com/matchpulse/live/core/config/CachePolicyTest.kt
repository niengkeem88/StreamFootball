package com.matchpulse.live.core.config

import com.matchpulse.live.core.database.CachePolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CachePolicyTest {
    @Test
    fun detectsFreshAndExpiredCache() {
        assertTrue(CachePolicy.isFresh(updatedAtMillis = 1_000L, nowMillis = 1_500L, ttlMillis = 1_000L))
        assertFalse(CachePolicy.isFresh(updatedAtMillis = 1_000L, nowMillis = 3_000L, ttlMillis = 1_000L))
    }
}
