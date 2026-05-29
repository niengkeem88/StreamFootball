package com.matchpulse.live.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Region(
    val id: String,
    val name: String,
    val subtitle: String,
    val icon: String,
)

@Immutable
@Serializable
data class Team(
    val id: String,
    val name: String,
    val shortName: String,
    val logoUrl: String? = null,
)

@Immutable
@Serializable
data class Competition(
    val id: String,
    val name: String,
    val region: String,
    val country: String,
    val category: String,
    val description: String,
    val logoUrl: String? = null,
)

@Immutable
@Serializable
data class FootballMatch(
    val id: String,
    val competition: Competition,
    val homeTeam: Team,
    val awayTeam: Team,
    val status: MatchStatus,
    val kickoffTimeMillis: Long,
    val minute: Int? = null,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val venue: String,
    val events: List<MatchEvent> = emptyList(),
    val stats: MatchStats? = null,
    val isStale: Boolean = false,
)

@Serializable
enum class MatchStatus {
    SCHEDULED,
    LIVE,
    HALFTIME,
    FINISHED,
    POSTPONED,
    CANCELLED;

    companion object {
        fun fromWire(value: String?): MatchStatus = when (value?.uppercase()) {
            "LIVE", "1H", "2H", "ET" -> LIVE
            "HT", "HALFTIME" -> HALFTIME
            "FT", "FINISHED" -> FINISHED
            "PST", "POSTPONED" -> POSTPONED
            "CANC", "CANCELLED" -> CANCELLED
            else -> SCHEDULED
        }
    }
}

@Immutable
@Serializable
data class MatchEvent(
    val id: String,
    val minute: Int,
    val type: MatchEventType,
    val teamName: String,
    val playerName: String,
    val detail: String,
)

@Serializable
enum class MatchEventType {
    GOAL,
    YELLOW_CARD,
    SUBSTITUTION,
    HALF_TIME,
    FULL_TIME,
}

@Immutable
@Serializable
data class MatchStats(
    val homePossession: Int,
    val awayPossession: Int,
    val homeShots: Int,
    val awayShots: Int,
    val homeShotsOnTarget: Int,
    val awayShotsOnTarget: Int,
    val homeCorners: Int,
    val awayCorners: Int,
    val homeFouls: Int,
    val awayFouls: Int,
)

@Immutable
@Serializable
data class TvGuideCompetition(
    val id: String,
    val name: String,
    val region: String,
    val country: String,
    val category: String,
    val description: String,
)

@Immutable
@Serializable
data class TvScheduleItem(
    val id: String,
    val competitionId: String,
    val matchTitle: String,
    val kickoffTimeMillis: Long,
    val providers: List<TvProvider>,
    val isStale: Boolean = false,
)

@Immutable
@Serializable
data class TvProvider(
    val id: String,
    val name: String,
    val type: String,
    val url: String?,
    val isConfigured: Boolean,
    val disclaimer: String,
    val isLicensedForInAppPlayback: Boolean = false,
)

@Serializable
data class UserSettings(
    val onboardingCompleted: Boolean = false,
    val selectedRegion: String? = null,
    val termsAccepted: Boolean = false,
    val darkModeEnabled: Boolean = true,
    val pushNotificationsEnabled: Boolean = false,
    val autoRefreshEnabled: Boolean = true,
    val dataSaverEnabled: Boolean = false,
    val favoriteMatchIds: Set<String> = emptySet(),
)

@Serializable
data class AppRemoteConfig(
    val maintenanceMode: Boolean = false,
    val forceUpdate: Boolean = false,
    val minSupportedVersion: Int = 1,
    val enableAds: Boolean = false,
    val adPlacements: List<AdPlacementConfig> = emptyList(),
    val enableTvGuide: Boolean = true,
    val enableProviderLinks: Boolean = true,
    val legalDisclaimerText: String = "MatchPulse Live does not host, distribute, or promote unauthorized live streams.",
    val privacyPolicyUrl: String? = null,
    val termsUrl: String? = null,
)

@Serializable
data class AdPlacementConfig(
    val placement: String,
    val enabled: Boolean,
)

@Immutable
sealed interface DataState<out T> {
    data object Loading : DataState<Nothing>
    @Immutable
    data class Success<T>(val data: T, val stale: Boolean = false) : DataState<T>
    @Immutable
    data class Error<T>(val message: String, val cachedData: T? = null, val stale: Boolean = false) : DataState<T>
}
