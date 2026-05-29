package com.matchpulse.live.core.media

import com.matchpulse.live.core.config.AppConfig
import com.matchpulse.live.domain.model.TvProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LegalPlaybackGate @Inject constructor(
    private val appConfig: AppConfig,
) {
    fun canEnableInAppPlayback(provider: TvProvider): Boolean =
        appConfig.enableExperimentalPlayer && provider.isLicensedForInAppPlayback && provider.url?.startsWith("https://") == true

    fun disabledMessage(): String =
        "Licensed in-app playback can be enabled only with official streaming rights."
}
