package com.matchpulse.live.data.remote.api

import com.matchpulse.live.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .apply {
                // If using the direct API-Sports domain, use x-apisports-key
                // If using the RapidAPI gateway, use x-rapidapi-key and x-rapidapi-host
                addHeader("x-apisports-key", BuildConfig.FOOTBALL_API_KEY)
            }
            .build()
        return chain.proceed(request)
    }
}
