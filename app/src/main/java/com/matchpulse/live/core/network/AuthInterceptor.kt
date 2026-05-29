package com.matchpulse.live.core.network

import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Accept", "application/json")
            .header("X-MatchPulse-Client", "android")
            .build()
        return chain.proceed(request)
    }
}
