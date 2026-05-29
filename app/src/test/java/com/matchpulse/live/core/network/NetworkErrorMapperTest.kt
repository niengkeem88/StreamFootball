package com.matchpulse.live.core.network

import java.net.SocketTimeoutException
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkErrorMapperTest {
    @Test
    fun mapsTimeoutToFriendlyMessage() {
        assertTrue(NetworkErrorMapper.userMessage(SocketTimeoutException()).contains("timed out"))
    }
}
