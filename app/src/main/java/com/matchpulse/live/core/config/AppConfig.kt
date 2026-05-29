package com.matchpulse.live.core.config

import com.matchpulse.live.BuildConfig

data class AppConfig(
    val isDebugBuild: Boolean,
    val appEnv: String,
    val apiBaseUrl: String,
    val footballApiMode: String,
    val enableAdsFlag: Boolean,
    val admobAndroidAppId: String,
    val admobAndroidBannerId: String,
    val admobAndroidInterstitialId: String,
    val admobAndroidNativeId: String,
    val admobAndroidRewardedId: String,
    val enableTeamLogos: Boolean,
    val enableLegalProviderLinks: Boolean,
    val enableExperimentalPlayer: Boolean,
    val admobTestDeviceIds: String,
) {
    val isProduction: Boolean = appEnv.equals("production", ignoreCase = true)
    val isDebugLike: Boolean = isDebugBuild || !isProduction
    val isMockMode: Boolean = footballApiMode.equals("mock", ignoreCase = true)
    val isBackendMode: Boolean = footballApiMode.equals("backend", ignoreCase = true)

    val shouldEnableAds: Boolean =
        enableAdsFlag && if (isDebugBuild) true else hasProductionAdIds()

    val bannerId: String = if (isDebugBuild) TEST_BANNER_ID else admobAndroidBannerId
    val interstitialId: String = if (isDebugBuild) TEST_INTERSTITIAL_ID else admobAndroidInterstitialId
    val nativeId: String = if (isDebugBuild) TEST_NATIVE_ID else admobAndroidNativeId
    val rewardedId: String = if (isDebugBuild) TEST_REWARDED_ID else admobAndroidRewardedId

    fun hasProductionAdIds(): Boolean =
        admobAndroidAppId.startsWith("ca-app-pub-") &&
            admobAndroidBannerId.startsWith("ca-app-pub-") &&
            admobAndroidInterstitialId.startsWith("ca-app-pub-") &&
            admobAndroidNativeId.startsWith("ca-app-pub-")

    companion object {
        const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
        const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
        const val TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"
        const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

        fun current(): AppConfig = AppConfig(
            isDebugBuild = BuildConfig.DEBUG,
            appEnv = BuildConfig.APP_ENV,
            apiBaseUrl = BuildConfig.API_BASE_URL,
            footballApiMode = BuildConfig.FOOTBALL_API_MODE,
            enableAdsFlag = BuildConfig.ENABLE_ADS,
            admobAndroidAppId = BuildConfig.ADMOB_ANDROID_APP_ID,
            admobAndroidBannerId = BuildConfig.ADMOB_ANDROID_BANNER_ID,
            admobAndroidInterstitialId = BuildConfig.ADMOB_ANDROID_INTERSTITIAL_ID,
            admobAndroidNativeId = BuildConfig.ADMOB_ANDROID_NATIVE_ID,
            admobAndroidRewardedId = BuildConfig.ADMOB_ANDROID_REWARDED_ID,
            enableTeamLogos = BuildConfig.ENABLE_TEAM_LOGOS,
            enableLegalProviderLinks = BuildConfig.ENABLE_LEGAL_PROVIDER_LINKS,
            enableExperimentalPlayer = BuildConfig.ENABLE_EXPERIMENTAL_PLAYER,
            admobTestDeviceIds = BuildConfig.ADMOB_TEST_DEVICE_IDS,
        )
    }
}
