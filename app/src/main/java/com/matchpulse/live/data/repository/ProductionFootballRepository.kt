package com.matchpulse.live.data.repository

import com.matchpulse.live.core.util.Resource
import com.matchpulse.live.core.util.safeApiCall
import com.matchpulse.live.data.remote.api.FootballApiService
import com.matchpulse.live.data.remote.dto.FootballFixturesResponse
import com.matchpulse.live.data.remote.dto.FootballLeaguesResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository specifically for handling API-Football (v3.football.api-sports.io) data.
 * This is separate from the internal mock/backend repositories used by the UI components
 * to ensure a clean migration path.
 */
@Singleton
class ProductionFootballRepository @Inject constructor(
    private val apiService: FootballApiService
) {

    suspend fun getLiveFixtures(): Resource<FootballFixturesResponse> {
        return safeApiCall { apiService.getLiveFixtures() }
    }

    suspend fun getFixturesByDate(date: String): Resource<FootballFixturesResponse> {
        return safeApiCall { apiService.getFixturesByDate(date) }
    }

    suspend fun getLeagues(): Resource<FootballLeaguesResponse> {
        return safeApiCall { apiService.getLeagues() }
    }
}
