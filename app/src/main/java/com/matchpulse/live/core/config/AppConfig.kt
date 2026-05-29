package com.matchpulse.live.core.config

import com.matchpulse.live.BuildConfig

data class AppConfig(
    val isDebugBuild: Boolean,
    val enableAdsFlag: Boolean,
    val admobAndroidAppId: String,
    val admobAndroidBannerId: String,
    val admobAndroidInterstitialId: String,
    val admobAndroidNativeId: String,
    val admobAndroidRewardedId: String,

    val admobTestDeviceIds: String,
) {
    val isProduction: Boolean = false
    val isDebugLike: Boolean = isDebugBuild || !isProduction

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
            enableAdsFlag = BuildConfig.ENABLE_ADS,
            admobAndroidAppId = BuildConfig.ADMOB_ANDROID_APP_ID,
            admobAndroidBannerId = BuildConfig.ADMOB_ANDROID_BANNER_ID,
            admobAndroidInterstitialId = BuildConfig.ADMOB_ANDROID_INTERSTITIAL_ID,
            admobAndroidNativeId = BuildConfig.ADMOB_ANDROID_NATIVE_ID,
            admobAndroidRewardedId = BuildConfig.ADMOB_ANDROID_REWARDED_ID,

            admobTestDeviceIds = BuildConfig.ADMOB_TEST_DEVICE_IDS,
        )
    }
}
