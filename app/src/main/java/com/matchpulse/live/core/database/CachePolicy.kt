package com.matchpulse.live.core.database

object CachePolicy {
    const val LIVE_MATCHES_MS = 60_000L
    const val TODAY_MATCHES_MS = 120_000L
    const val UPCOMING_MATCHES_MS = 15 * 60_000L
    const val FINISHED_MATCHES_MS = 5 * 60_000L
    const val COMPETITIONS_MS = 24 * 60 * 60_000L
    const val TV_GUIDE_MS = 6 * 60 * 60_000L
    const val REMOTE_CONFIG_MS = 60 * 60_000L

    fun isFresh(updatedAtMillis: Long?, nowMillis: Long, ttlMillis: Long): Boolean =
        updatedAtMillis != null && nowMillis - updatedAtMillis <= ttlMillis
}
