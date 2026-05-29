package com.matchpulse.live.core.ads

import com.matchpulse.live.core.config.AppConfig

const val ADS_LOG_TAG = "[MatchPulseAds]"

object AdConfig {
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    fun from(appConfig: AppConfig): AdRuntimeConfig {
        val debugAds = appConfig.isDebugBuild
        val hasProductionIds = appConfig.hasProductionAdIds()
        val enabled = appConfig.enableAdsFlag && if (debugAds) true else hasProductionIds
        val disabledReason = when {
            !appConfig.enableAdsFlag -> "ENABLE_ADS=false"
            !debugAds && !hasProductionIds -> "Production AdMob IDs are missing"
            else -> null
        }
        return AdRuntimeConfig(
            enabled = enabled,
            debugAds = debugAds,
            appId = if (debugAds) TEST_APP_ID else appConfig.admobAndroidAppId,
            bannerId = if (debugAds) TEST_BANNER_ID else appConfig.admobAndroidBannerId,
            interstitialId = if (debugAds) TEST_INTERSTITIAL_ID else appConfig.admobAndroidInterstitialId,
            nativeId = if (debugAds) TEST_NATIVE_ID else appConfig.admobAndroidNativeId,
            rewardedId = if (debugAds) TEST_REWARDED_ID else appConfig.admobAndroidRewardedId,
            testDeviceIds = appConfig.admobTestDeviceIds.split(',').map { it.trim() }.filter { it.isNotBlank() },
            disabledReason = disabledReason,
        )
    }

    fun mask(adUnitId: String): String {
        if (adUnitId.isBlank()) return "(not configured)"
        val slash = adUnitId.indexOf('/')
        val appSeparator = adUnitId.indexOf('~')
        val splitAt = listOf(slash, appSeparator).filter { it > 0 }.minOrNull()
        if (splitAt == null) return adUnitId.take(10) + "..." + adUnitId.takeLast(4)
        return adUnitId.take(18) + "..." + adUnitId.substring(splitAt)
    }
}

data class AdRuntimeConfig(
    val enabled: Boolean,
    val debugAds: Boolean,
    val appId: String,
    val bannerId: String,
    val interstitialId: String,
    val nativeId: String,
    val rewardedId: String,
    val testDeviceIds: List<String>,
    val disabledReason: String?,
)
