package com.matchpulse.live.core.network

sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class Failure(val message: String, val code: Int? = null) : NetworkResult<Nothing>
}
