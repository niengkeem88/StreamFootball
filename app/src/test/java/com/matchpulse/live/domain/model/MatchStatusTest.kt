package com.matchpulse.live.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MatchStatusTest {
    @Test
    fun mapsBackendStatusCodesSafely() {
        assertEquals(MatchStatus.LIVE, MatchStatus.fromWire("1H"))
        assertEquals(MatchStatus.HALFTIME, MatchStatus.fromWire("HT"))
        assertEquals(MatchStatus.FINISHED, MatchStatus.fromWire("FT"))
        assertEquals(MatchStatus.POSTPONED, MatchStatus.fromWire("PST"))
        assertEquals(MatchStatus.CANCELLED, MatchStatus.fromWire("CANC"))
        assertEquals(MatchStatus.SCHEDULED, MatchStatus.fromWire("unknown"))
    }
}
