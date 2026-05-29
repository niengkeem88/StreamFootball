package com.matchpulse.live.core.legal

import com.matchpulse.live.domain.model.TvProvider
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderLinkValidator @Inject constructor() {
    fun canOpen(provider: TvProvider, providerLinksEnabled: Boolean): Boolean =
        validationMessage(provider, providerLinksEnabled) == null

    fun validationMessage(provider: TvProvider, providerLinksEnabled: Boolean): String? {
        val url = provider.url
        if (!providerLinksEnabled || !provider.isConfigured || url.isNullOrBlank()) {
            return "Provider links will be connected after legal source configuration."
        }
        val uri = runCatching { URI(url) }.getOrNull()
            ?: return "This provider link is not valid."
        if (uri.scheme?.equals("https", ignoreCase = true) != true) {
            return "Only secure official provider links can be opened."
        }
        if (uri.host.isNullOrBlank()) {
            return "This provider link is not valid."
        }
        val host = uri.host.orEmpty().lowercase()
        if (blockedTerms.any { host.contains(it) }) {
            return "This provider is not approved for legal TV guide links."
        }
        return null
    }

    private companion object {
        val blockedTerms = listOf("m3u", "iptv", "pirate", "free-stream", "crackstreams", "totalsportek")
    }
}
