package com.matchpulse.live.core.util

import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

suspend fun <T> safeApiCall(
    diagnostics: ApiDiagnosticsManager? = null,
    apiCall: suspend () -> Response<T>
): Resource<T> {
    try {
        val response = apiCall()
        val endpoint = response.raw().request.url.toString()
        
        if (response.isSuccessful) {
            val body = response.body()
            diagnostics?.recordRequest(endpoint, response.code(), "Success")
            if (body != null) {
                return Resource.Success(body)
            }
        }
        
        val errorMsg = when (response.code()) {
            401 -> "Invalid API Key - Please check configuration"
            403 -> "Quota exceeded or API access restricted"
            404 -> "Resource not found on server"
            429 -> "Too many requests - Rate limit reached"
            in 500..599 -> "Server error - Please try again later"
            else -> "API Error (${response.code()}): ${response.message()}"
        }
        
        diagnostics?.recordRequest(endpoint, response.code(), errorMsg)
        return Resource.Error(errorMsg)
        
    } catch (e: SocketTimeoutException) {
        return Resource.Error("Request timed out - Server is busy")
    } catch (e: IOException) {
        return Resource.Error("Network failure: check your internet connection")
    } catch (e: Exception) {
        return Resource.Error("Unexpected error: ${e.localizedMessage}")
    }
}
