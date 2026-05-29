package com.matchpulse.live.core.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

data class ApiLogEntry(
    val endpoint: String,
    val statusCode: Int,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class DiagnosticsSummary(
    val lastStatusCode: Int? = null,
    val lastEndpoint: String? = null,
    val isQuotaExceeded: Boolean = false,
    val requestCount: Int = 0,
    val logs: List<ApiLogEntry> = emptyList()
)

@Singleton
class ApiDiagnosticsManager @Inject constructor() {

    private val _summary = MutableStateFlow(DiagnosticsSummary())
    val summary = _summary.asStateFlow()

    fun recordRequest(endpoint: String, statusCode: Int, message: String) {
        _summary.update { current ->
            val newLog = ApiLogEntry(endpoint, statusCode, message)
            current.copy(
                lastStatusCode = statusCode,
                lastEndpoint = endpoint,
                isQuotaExceeded = statusCode == 403,
                requestCount = current.requestCount + 1,
                logs = (listOf(newLog) + current.logs).take(20)
            )
        }
    }
}
