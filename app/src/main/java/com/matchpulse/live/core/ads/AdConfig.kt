package com.matchpulse.live.core.ads

import com.matchpulse.live.core.config.AppConfig

data class AdRuntimeConfig(
    val enabled: Boolean,
    val debugAds: Boolean,
    val bannerId: String,
    val interstitialId: String,
    val nativeId: String,
    val rewardedId: String,
    val disabledReason: String? = null,
)

object AdConfig {
    fun from(appConfig: AppConfig): AdRuntimeConfig {
        val enabled = appConfig.shouldEnableAds
        return AdRuntimeConfig(
            enabled = enabled,
            debugAds = appConfig.isDebugLike,
            bannerId = appConfig.bannerId,
            interstitialId = appConfig.interstitialId,
            nativeId = appConfig.nativeId,
            rewardedId = appConfig.rewardedId,
            disabledReason = if (!enabled) {
                if (appConfig.isProduction) "Missing production AdMob IDs" else "Ads disabled by config"
            } else null,
        )
    }

    fun mask(id: String): String = when {
        id.length >= 8 -> "${id.take(3)}...${id.takeLast(3)}"
        id.isNotEmpty() -> "***"
        else -> "(empty)"
    }
}
