package com.matchpulse.live.core.network

import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

object NetworkErrorMapper {
    fun userMessage(throwable: Throwable): String = when (throwable) {
        is SocketTimeoutException -> "The request timed out. Please try again."
        is IOException -> "No internet connection or backend is unreachable."
        is SerializationException -> "The server response could not be read."
        is HttpException -> when (throwable.code()) {
            400 -> "The request could not be processed."
            401, 403 -> "This football data source is not authorized."
            404 -> "The requested football data was not found."
            429 -> "Too many requests. Please wait a moment."
            in 500..599 -> "The football service is temporarily unavailable."
            else -> "Football data could not be loaded."
        }
        else -> "Something went wrong while loading football data."
    }
}
