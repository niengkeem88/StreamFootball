package com.matchpulse.live.core.legal

import com.matchpulse.live.domain.model.TvProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderLinkValidatorTest {
    private val validator = ProviderLinkValidator()

    @Test
    fun blocksMissingConfiguration() {
        val provider = TvProvider("p", "Provider", "Broadcaster", null, false, "Legal only")
        assertFalse(validator.canOpen(provider, providerLinksEnabled = true))
    }

    @Test
    fun blocksNonHttpsLinks() {
        val provider = TvProvider("p", "Provider", "Broadcaster", "http://official.invalid", true, "Legal only")
        assertFalse(validator.canOpen(provider, providerLinksEnabled = true))
    }

    @Test
    fun allowsConfiguredHttpsProvider() {
        val provider = TvProvider("p", "Provider", "Broadcaster", "https://official.invalid", true, "Legal only")
        assertTrue(validator.canOpen(provider, providerLinksEnabled = true))
    }
}
