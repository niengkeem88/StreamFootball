package com.matchpulse.live.core.ads

import android.util.Log
import com.matchpulse.live.core.datastore.UserPreferencesDataSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdFrequencyCapManager @Inject constructor(
    private val dataSource: UserPreferencesDataSource,
) {
    suspend fun markAppOpened(nowMillis: Long = System.currentTimeMillis()) {
        dataSource.markAppOpened(nowMillis)
        Log.d(ADS_LOG_TAG, "App session marked for ad frequency caps.")
    }

    suspend fun recordMajorNavigation() {
        dataSource.recordMajorNavigation()
    }

    suspend fun canShowInterstitial(nowMillis: Long = System.currentTimeMillis()): Boolean {
        val status = frequencyStatus(nowMillis)
        status.blockReason?.let { Log.d(ADS_LOG_TAG, "Interstitial blocked: $it") }
        return status.canShow
    }

    suspend fun frequencyStatus(nowMillis: Long = System.currentTimeMillis()): AdFrequencyStatus {
        val counters = dataSource.adCounters()
        val dayKey = dayKey(nowMillis)
        val blockReason = AdFrequencyPolicy.blockReason(counters, nowMillis, dayKey)
        return AdFrequencyStatus(
            canShow = blockReason == null,
            blockReason = blockReason,
            sessionCount = counters.sessionCount,
            dailyCount = if (counters.dailyKey == dayKey) counters.dailyCount else 0,
            majorNavigationCount = counters.majorNavigationCount,
            lastInterstitialShownAt = counters.lastInterstitialShownAt,
        )
    }

    suspend fun recordInterstitialShown(nowMillis: Long = System.currentTimeMillis()) {
        dataSource.recordInterstitialShown(nowMillis, dayKey(nowMillis))
        Log.d(ADS_LOG_TAG, "Interstitial impression recorded for caps.")
    }

    suspend fun resetFrequencyCapsForTesting(nowMillis: Long = System.currentTimeMillis()) {
        dataSource.resetAdFrequencyCapsForTesting(nowMillis)
        Log.d(ADS_LOG_TAG, "Ad frequency caps reset for diagnostics testing.")
    }

    private fun dayKey(nowMillis: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(nowMillis))
}

data class AdFrequencyStatus(
    val canShow: Boolean,
    val blockReason: String?,
    val sessionCount: Int,
    val dailyCount: Int,
    val majorNavigationCount: Int,
    val lastInterstitialShownAt: Long,
)
