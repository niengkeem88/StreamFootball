package com.matchpulse.live.data.remote.api

import com.matchpulse.live.data.remote.dto.FootballFixturesResponse
import com.matchpulse.live.data.remote.dto.FootballLeaguesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FootballApiService {

    @GET("fixtures")
    suspend fun getLiveFixtures(
        @Query("live") live: String = "all"
    ): Response<FootballFixturesResponse>

    @GET("fixtures")
    suspend fun getFixturesByDate(
        @Query("date") date: String
    ): Response<FootballFixturesResponse>

    @GET("leagues")
    suspend fun getLeagues(): Response<FootballLeaguesResponse>

    companion object {
        const val BASE_URL = "https://v3.football.api-sports.io/"
    }
}
