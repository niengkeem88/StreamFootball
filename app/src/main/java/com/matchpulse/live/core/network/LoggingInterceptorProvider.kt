package com.matchpulse.live.core.network

import com.matchpulse.live.core.config.AppConfig
import okhttp3.logging.HttpLoggingInterceptor

object LoggingInterceptorProvider {
    fun create(config: AppConfig): HttpLoggingInterceptor =
        HttpLoggingInterceptor { message -> android.util.Log.d("[MatchPulse]", message) }
            .apply {
                level = if (config.isDebugLike) {
                    HttpLoggingInterceptor.Level.BASIC
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }
}
