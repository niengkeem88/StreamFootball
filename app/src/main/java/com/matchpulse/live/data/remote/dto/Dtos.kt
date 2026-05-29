package com.matchpulse.live.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class HealthDto(
    val status: String = "unknown",
    val message: String? = null,
    val checkedAt: String? = null,
)

@Serializable
data class TeamDto(
    val id: String,
    val name: String,
    val shortName: String? = null,
    val logoUrl: String? = null,
)

@Serializable
data class CompetitionDto(
    val id: String,
    val name: String,
    val region: String = "International",
    val country: String = "Global",
    val category: String = "International",
    val description: String = "",
)

@Serializable
data class FootballMatchDto(
    val id: String,
    val competition: CompetitionDto,
    val homeTeam: TeamDto,
    val awayTeam: TeamDto,
    val status: String,
    val kickoffTimeMillis: Long,
    val minute: Int? = null,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val venue: String = "TBC",
    val events: List<MatchEventDto> = emptyList(),
    val stats: MatchStatsDto? = null,
)

@Serializable
data class MatchEventDto(
    val id: String,
    val minute: Int,
    val type: String,
    val teamName: String,
    val playerName: String,
    val detail: String,
)

@Serializable
data class MatchStatsDto(
    val homePossession: Int = 50,
    val awayPossession: Int = 50,
    val homeShots: Int = 0,
    val awayShots: Int = 0,
    val homeShotsOnTarget: Int = 0,
    val awayShotsOnTarget: Int = 0,
    val homeCorners: Int = 0,
    val awayCorners: Int = 0,
    val homeFouls: Int = 0,
    val awayFouls: Int = 0,
)

@Serializable
data class TvGuideCompetitionDto(
    val id: String,
    val name: String,
    val region: String,
    val country: String,
    val category: String,
    val description: String,
)

@Serializable
data class TvScheduleDto(
    val id: String,
    val competitionId: String,
    val matchTitle: String,
    val kickoffTimeMillis: Long,
    val providers: List<TvProviderDto> = emptyList(),
)

@Serializable
data class TvProviderDto(
    val id: String,
    val name: String,
    val type: String,
    val url: String? = null,
    val isConfigured: Boolean = false,
    val disclaimer: String = "Official provider links must be legally configured by the backend.",
    val isLicensedForInAppPlayback: Boolean = false,
)

@Serializable
data class AppRemoteConfigDto(
    val maintenanceMode: Boolean = false,
    val forceUpdate: Boolean = false,
    val minSupportedVersion: Int = 1,
    val enableAds: Boolean = false,
    val adPlacements: List<AdPlacementConfigDto> = emptyList(),
    val enableTvGuide: Boolean = true,
    val enableProviderLinks: Boolean = true,
    val legalDisclaimerText: String = "MatchPulse Live does not host, distribute, or promote unauthorized live streams.",
    val privacyPolicyUrl: String? = null,
    val termsUrl: String? = null,
)

@Serializable
data class AdPlacementConfigDto(
    val placement: String,
    val enabled: Boolean,
)
