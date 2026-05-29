package com.matchpulse.live.core.config

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppConfigTest {
    @Test
    fun productionAdsDisableWhenIdsAreMissing() {
        val config = AppConfig(
            isDebugBuild = false,
            appEnv = "production",
            apiBaseUrl = "https://backend.example.com",
            footballApiMode = "backend",
            enableAdsFlag = true,
            admobAndroidAppId = "",
            admobAndroidBannerId = "",
            admobAndroidInterstitialId = "",
            admobAndroidNativeId = "",
            admobAndroidRewardedId = "",
            enableTeamLogos = true,
            enableLegalProviderLinks = true,
            enableExperimentalPlayer = false,
        )

        assertFalse(config.shouldEnableAds)
        assertTrue(config.isProduction)
        assertTrue(config.isBackendMode)
    }

    @Test
    fun developmentMockModeCanUseTestAdsWhenEnabled() {
        val config = AppConfig(
            isDebugBuild = true,
            appEnv = "development",
            apiBaseUrl = "https://matchpulse.invalid",
            footballApiMode = "mock",
            enableAdsFlag = true,
            admobAndroidAppId = AppConfig.TEST_APP_ID,
            admobAndroidBannerId = "",
            admobAndroidInterstitialId = "",
            admobAndroidNativeId = "",
            admobAndroidRewardedId = "",
            enableTeamLogos = true,
            enableLegalProviderLinks = true,
            enableExperimentalPlayer = false,
        )

        assertTrue(config.shouldEnableAds)
        assertTrue(config.isMockMode)
        assertTrue(config.bannerId.contains("3940256099942544"))
    }
}
