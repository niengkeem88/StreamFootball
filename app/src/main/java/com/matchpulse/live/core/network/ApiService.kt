package com.matchpulse.live.core.network

import com.matchpulse.live.data.remote.dto.AppRemoteConfigDto
import com.matchpulse.live.data.remote.dto.CompetitionDto
import com.matchpulse.live.data.remote.dto.FootballMatchDto
import com.matchpulse.live.data.remote.dto.HealthDto
import com.matchpulse.live.data.remote.dto.TvGuideCompetitionDto
import com.matchpulse.live.data.remote.dto.TvScheduleDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("health")
    suspend fun health(): HealthDto

    @GET("v1/football/provider/health")
    suspend fun providerHealth(): HealthDto

    @GET("v1/football/matches/live")
    suspend fun liveMatches(): List<FootballMatchDto>

    @GET("v1/football/matches/today")
    suspend fun todayMatches(): List<FootballMatchDto>

    @GET("v1/football/matches/upcoming")
    suspend fun upcomingMatches(): List<FootballMatchDto>

    @GET("v1/football/matches/finished")
    suspend fun finishedMatches(): List<FootballMatchDto>

    @GET("v1/football/matches/{matchId}")
    suspend fun matchDetail(@Path("matchId") matchId: String): FootballMatchDto

    @GET("v1/football/competitions")
    suspend fun competitions(): List<CompetitionDto>

    @GET("v1/football/competitions/{competitionId}/matches")
    suspend fun competitionMatches(@Path("competitionId") competitionId: String): List<FootballMatchDto>

    @GET("v1/tv-guide/competitions")
    suspend fun tvGuideCompetitions(): List<TvGuideCompetitionDto>

    @GET("v1/tv-guide/competitions/{competitionId}/schedule")
    suspend fun tvSchedule(@Path("competitionId") competitionId: String): List<TvScheduleDto>

    @GET("v1/app/config")
    suspend fun remoteConfig(): AppRemoteConfigDto
}
