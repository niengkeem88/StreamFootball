package com.matchpulse.live.data.repository

import com.matchpulse.live.core.util.ApiDiagnosticsManager
import com.matchpulse.live.core.util.Resource
import com.matchpulse.live.core.util.safeApiCall
import com.matchpulse.live.data.mapper.toDomain
import com.matchpulse.live.data.remote.api.FootballApiService
import com.matchpulse.live.domain.model.Competition
import com.matchpulse.live.domain.model.DataState
import com.matchpulse.live.domain.model.FootballMatch
import com.matchpulse.live.domain.repository.FootballRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FootballRepositoryImpl @Inject constructor(
    private val apiService: FootballApiService,
    private val diagnostics: ApiDiagnosticsManager
) : FootballRepository {

    private var cachedLeagues: List<Competition>? = null
    private val fixtureCache = mutableMapOf<String, List<FootballMatch>>()
    private val lastFetchMap = mutableMapOf<String, Long>()
    
    private val mutex = Mutex()
    private val activeDeferreds = mutableMapOf<String, Deferred<Any?>>()

    private companion object {
        const val LIVE_THROTTLE_MS = 30_000L // 30 seconds
        const val GENERAL_THROTTLE_MS = 300_000L // 5 minutes
    }

    private fun isThrottled(key: String, throttleMs: Long): Boolean {
        val lastFetch = lastFetchMap[key] ?: return false
        return (System.currentTimeMillis() - lastFetch) < throttleMs
    }

    private fun markFetched(key: String) {
        lastFetchMap[key] = System.currentTimeMillis()
    }
    
    private suspend fun <T> deduplicate(key: String, block: suspend () -> T): T {
        val deferred = mutex.withLock {
            @Suppress("UNCHECKED_CAST")
            activeDeferreds[key] as? Deferred<T> ?: CoroutineScope(Dispatchers.IO).async {
                try {
                    block()
                } finally {
                    mutex.withLock { activeDeferreds.remove(key) }
                }
            }.also { activeDeferreds[key] = it as Deferred<Any?> }
        }
        return deferred.await()
    }

    private val prioritizedLeagueIds = setOf(
        "39",   // Premier League
        "2",    // UEFA Champions League
        "140",  // La Liga
        "78",   // Bundesliga
        "135",  // Serie A
        "61",   // Ligue 1
        "253",  // MLS
        "1",    // World Cup
        "3",    // UEFA Europa League
        "848",  // UEFA Conference League
        "4",    // Euro Championship
        "6",    // Africa Cup of Nations
        "9",    // Copa America
        "20",   // CAF Champions League
        "21",   // CAF Confederation Cup
    )

    override fun getLiveMatches(forceRefresh: Boolean): Flow<DataState<List<FootballMatch>>> = flow {
        val cached = fixtureCache["live"]
        if (cached != null && !forceRefresh && isThrottled("live", LIVE_THROTTLE_MS)) {
            emit(DataState.Success(cached))
            return@flow
        }
        
        emit(DataState.Loading)
        val result = deduplicate("live") { safeApiCall(diagnostics) { apiService.getLiveFixtures() } } ?: return@flow
        
        when (result) {
            is Resource.Success -> {
                val apiErrors = result.data?.errors
                if (!apiErrors.isNullOrEmpty()) {
                    val error = apiErrors.values.firstOrNull() ?: "API Error"
                    diagnostics.recordRequest("live", 200, error)
                    emit(DataState.Error(error, cachedData = cached))
                } else {
                    val matches = result.data?.response?.map { it.toDomain() } ?: emptyList()
                    markFetched("live")
                    fixtureCache["live"] = matches
                    
                    if (matches.isEmpty()) {
                        // Fallback: If no live matches, fetch today's scheduled matches
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val cachedToday = fixtureCache["today"]
                        if (!forceRefresh && isThrottled("today", GENERAL_THROTTLE_MS) && cachedToday != null) {
                            val upcomingToday = cachedToday.filter { 
                                it.status == com.matchpulse.live.domain.model.MatchStatus.SCHEDULED 
                            }
                            emit(DataState.Success(upcomingToday))
                            return@flow
                        }
                        
                        val todayResult = safeApiCall(diagnostics) { apiService.getFixturesByDate(date) }
                        if (todayResult is Resource.Success) {
                            val todayMatches = todayResult.data?.response?.map { it.toDomain() } ?: emptyList()
                            markFetched("today")
                            fixtureCache["today"] = todayMatches
                            // Filter for matches that haven't finished yet or are upcoming
                            val upcomingToday = todayMatches.filter { 
                                it.status == com.matchpulse.live.domain.model.MatchStatus.SCHEDULED 
                            }
                            emit(DataState.Success(upcomingToday))
                        } else {
                            emit(DataState.Success(emptyList()))
                        }
                    } else {
                        emit(DataState.Success(matches))
                    }
                }
            }
            is Resource.Error -> {
                emit(DataState.Error(result.message ?: "Unknown error", cachedData = cached))
            }
            is Resource.Loading -> emit(DataState.Loading)
        }
    }

    override fun getTodayMatches(forceRefresh: Boolean): Flow<DataState<List<FootballMatch>>> = flow {
        val cached = fixtureCache["today"]
        if (cached != null && !forceRefresh && isThrottled("today", GENERAL_THROTTLE_MS)) {
            emit(DataState.Success(cached))
            return@flow
        }
        
        emit(DataState.Loading)
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val result = deduplicate("today") { safeApiCall(diagnostics) { apiService.getFixturesByDate(date) } } ?: return@flow
        
        when (result) {
            is Resource.Success -> {
                val apiErrors = result.data?.errors
                if (!apiErrors.isNullOrEmpty()) {
                    val error = apiErrors.values.firstOrNull() ?: "API Error"
                    diagnostics.recordRequest("today", 200, error)
                    emit(DataState.Error(error, cachedData = cached))
                } else {
                    val matches = result.data?.response?.map { it.toDomain() } ?: emptyList()
                    markFetched("today")
                    fixtureCache["today"] = matches
                    emit(DataState.Success(matches))
                }
            }
            is Resource.Error -> {
                emit(DataState.Error(result.message ?: "Unknown error", cachedData = cached))
            }
            is Resource.Loading -> emit(DataState.Loading)
        }
    }

    override fun getUpcomingMatches(forceRefresh: Boolean): Flow<DataState<List<FootballMatch>>> = flow {
        // For simplicity, we fetch today's matches as "upcoming" placeholders if they haven't started
        // A better production app might fetch tomorrow's date or a range
        getTodayMatches(forceRefresh).collect { emit(it) }
    }

    override fun getFinishedMatches(forceRefresh: Boolean): Flow<DataState<List<FootballMatch>>> = flow {
        // Similar to upcoming, we could filter today's finished matches
        getTodayMatches(forceRefresh).collect { state ->
            if (state is DataState.Success) {
                emit(DataState.Success(state.data.filter { it.status == com.matchpulse.live.domain.model.MatchStatus.FINISHED }))
            } else {
                emit(state)
            }
        }
    }

    override fun getMatchDetail(matchId: String, forceRefresh: Boolean): Flow<DataState<FootballMatch>> = flow {
        emit(DataState.Loading)
        // API-Football v3 has a specific endpoint for single fixture detail if needed, 
        // but here we just return a stub or search in today's matches for now
        // To be fully production ready, we'd call /fixtures?id=matchId
        emit(DataState.Error("Detail endpoint not fully implemented for API-Football v3 yet"))
    }

    override fun getCompetitions(region: String?, category: String?): Flow<DataState<List<Competition>>> = flow {
        // Return cached leagues if available
        cachedLeagues?.let {
            emit(DataState.Success(it))
        }

        if (isThrottled("leagues", GENERAL_THROTTLE_MS)) return@flow

        emit(DataState.Loading)
        val result = safeApiCall(diagnostics) { apiService.getLeagues() }
        when (result) {
            is Resource.Success -> {
                val apiErrors = result.data?.errors
                if (!apiErrors.isNullOrEmpty()) {
                    val error = apiErrors.values.firstOrNull() ?: "API Error"
                    diagnostics.recordRequest("leagues", 200, error)
                    emit(DataState.Error(error))
                } else {
                    val leagues = result.data?.response?.map { it.toDomain() } ?: emptyList()
                    markFetched("leagues")
                    
                    // Sort by priority and then by name
                    val sortedLeagues = leagues.sortedWith(
                        compareByDescending<Competition> { prioritizedLeagueIds.contains(it.id) }
                            .thenBy { it.name }
                    )
                    
                    cachedLeagues = sortedLeagues
                    emit(DataState.Success(sortedLeagues))
                }
            }
            is Resource.Error -> {
                emit(DataState.Error(result.message ?: "Unknown error", cachedData = cachedLeagues))
            }
            is Resource.Loading -> emit(DataState.Loading)
        }
    }
}
