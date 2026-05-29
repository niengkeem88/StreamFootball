package com.matchpulse.live.data.mapper

import com.matchpulse.live.core.database.entity.CompetitionEntity
import com.matchpulse.live.core.database.entity.MatchEntity
import com.matchpulse.live.core.database.entity.RemoteConfigEntity
import com.matchpulse.live.core.database.entity.TvGuideCompetitionEntity
import com.matchpulse.live.core.database.entity.TvScheduleEntity
import com.matchpulse.live.data.remote.dto.AdPlacementConfigDto
import com.matchpulse.live.data.remote.dto.AppRemoteConfigDto
import com.matchpulse.live.data.remote.dto.CompetitionDto
import com.matchpulse.live.data.remote.dto.FootballMatchDto
import com.matchpulse.live.data.remote.dto.MatchEventDto
import com.matchpulse.live.data.remote.dto.MatchStatsDto
import com.matchpulse.live.data.remote.dto.TeamDto
import com.matchpulse.live.data.remote.dto.TvGuideCompetitionDto
import com.matchpulse.live.data.remote.dto.TvProviderDto
import com.matchpulse.live.data.remote.dto.TvScheduleDto
import com.matchpulse.live.domain.model.AdPlacementConfig
import com.matchpulse.live.domain.model.AppRemoteConfig
import com.matchpulse.live.domain.model.Competition
import com.matchpulse.live.domain.model.FootballMatch
import com.matchpulse.live.domain.model.MatchEvent
import com.matchpulse.live.domain.model.MatchEventType
import com.matchpulse.live.domain.model.MatchStats
import com.matchpulse.live.domain.model.MatchStatus
import com.matchpulse.live.domain.model.Team
import com.matchpulse.live.domain.model.TvGuideCompetition
import com.matchpulse.live.domain.model.TvProvider
import com.matchpulse.live.domain.model.TvScheduleItem
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

fun TeamDto.toDomain() = Team(id, name, shortName ?: name.take(3).uppercase(), logoUrl)
fun CompetitionDto.toDomain() = Competition(id, name, region, country, category, description)
fun TvGuideCompetitionDto.toDomain() = TvGuideCompetition(id, name, region, country, category, description)
fun TvProviderDto.toDomain() = TvProvider(id, name, type, url, isConfigured, disclaimer, isLicensedForInAppPlayback)

fun MatchEventDto.toDomain() = MatchEvent(
    id = id,
    minute = minute,
    type = when (type.uppercase()) {
        "GOAL" -> MatchEventType.GOAL
        "YELLOW_CARD" -> MatchEventType.YELLOW_CARD
        "SUBSTITUTION" -> MatchEventType.SUBSTITUTION
        "HALF_TIME" -> MatchEventType.HALF_TIME
        "FULL_TIME" -> MatchEventType.FULL_TIME
        else -> MatchEventType.SUBSTITUTION
    },
    teamName = teamName,
    playerName = playerName,
    detail = detail,
)

fun MatchStatsDto.toDomain() = MatchStats(
    homePossession,
    awayPossession,
    homeShots,
    awayShots,
    homeShotsOnTarget,
    awayShotsOnTarget,
    homeCorners,
    awayCorners,
    homeFouls,
    awayFouls,
)

fun FootballMatchDto.toDomain() = FootballMatch(
    id = id,
    competition = competition.toDomain(),
    homeTeam = homeTeam.toDomain(),
    awayTeam = awayTeam.toDomain(),
    status = MatchStatus.fromWire(status),
    kickoffTimeMillis = kickoffTimeMillis,
    minute = minute,
    homeScore = homeScore,
    awayScore = awayScore,
    venue = venue,
    events = events.map { it.toDomain() },
    stats = stats?.toDomain(),
)

fun TvScheduleDto.toDomain() = TvScheduleItem(
    id = id,
    competitionId = competitionId,
    matchTitle = matchTitle,
    kickoffTimeMillis = kickoffTimeMillis,
    providers = providers.map { it.toDomain() },
)

fun AppRemoteConfigDto.toDomain() = AppRemoteConfig(
    maintenanceMode = maintenanceMode,
    forceUpdate = forceUpdate,
    minSupportedVersion = minSupportedVersion,
    enableAds = enableAds,
    adPlacements = adPlacements.map { AdPlacementConfig(it.placement, it.enabled) },
    enableTvGuide = enableTvGuide,
    enableProviderLinks = enableProviderLinks,
    legalDisclaimerText = legalDisclaimerText,
    privacyPolicyUrl = privacyPolicyUrl,
    termsUrl = termsUrl,
)

fun Competition.toEntity(now: Long) = CompetitionEntity(id, name, region, country, category, description, now)
fun CompetitionEntity.toDomain() = Competition(id, name, region, country, category, description)
fun TvGuideCompetition.toEntity(now: Long) = TvGuideCompetitionEntity(id, name, region, country, category, description, now)
fun TvGuideCompetitionEntity.toDomain() = TvGuideCompetition(id, name, region, country, category, description)

fun FootballMatch.toEntity(bucket: String, now: Long) = MatchEntity(
    id = id,
    bucket = bucket,
    competitionId = competition.id,
    competitionName = competition.name,
    competitionRegion = competition.region,
    competitionCountry = competition.country,
    competitionCategory = competition.category,
    competitionDescription = competition.description,
    homeTeamId = homeTeam.id,
    homeTeamName = homeTeam.name,
    homeTeamShortName = homeTeam.shortName,
    awayTeamId = awayTeam.id,
    awayTeamName = awayTeam.name,
    awayTeamShortName = awayTeam.shortName,
    status = status.name,
    kickoffTimeMillis = kickoffTimeMillis,
    minute = minute,
    homeScore = homeScore,
    awayScore = awayScore,
    venue = venue,
    eventsJson = json.encodeToString(ListSerializer(MatchEvent.serializer()), events),
    statsJson = stats?.let { json.encodeToString(MatchStats.serializer(), it) },
    updatedAtMillis = now,
)

fun MatchEntity.toDomain(stale: Boolean = false) = FootballMatch(
    id = id,
    competition = Competition(competitionId, competitionName, competitionRegion, competitionCountry, competitionCategory, competitionDescription),
    homeTeam = Team(homeTeamId, homeTeamName, homeTeamShortName),
    awayTeam = Team(awayTeamId, awayTeamName, awayTeamShortName),
    status = MatchStatus.fromWire(status),
    kickoffTimeMillis = kickoffTimeMillis,
    minute = minute,
    homeScore = homeScore,
    awayScore = awayScore,
    venue = venue,
    events = json.decodeFromString(ListSerializer(MatchEvent.serializer()), eventsJson),
    stats = statsJson?.let { json.decodeFromString(MatchStats.serializer(), it) },
    isStale = stale,
)

fun TvScheduleItem.toEntity(now: Long) = TvScheduleEntity(
    id = id,
    competitionId = competitionId,
    matchTitle = matchTitle,
    kickoffTimeMillis = kickoffTimeMillis,
    providersJson = json.encodeToString(ListSerializer(TvProvider.serializer()), providers),
    updatedAtMillis = now,
)

fun TvScheduleEntity.toDomain(stale: Boolean = false) = TvScheduleItem(
    id = id,
    competitionId = competitionId,
    matchTitle = matchTitle,
    kickoffTimeMillis = kickoffTimeMillis,
    providers = json.decodeFromString(ListSerializer(TvProvider.serializer()), providersJson),
    isStale = stale,
)

fun AppRemoteConfig.toEntity(now: Long) = RemoteConfigEntity(
    payloadJson = json.encodeToString(AppRemoteConfig.serializer(), this),
    updatedAtMillis = now,
)

fun RemoteConfigEntity.toDomain() = json.decodeFromString(AppRemoteConfig.serializer(), payloadJson)

fun Competition.toDto() = CompetitionDto(id, name, region, country, category, description)
fun Team.toDto() = TeamDto(id, name, shortName, logoUrl)
fun AdPlacementConfig.toDto() = AdPlacementConfigDto(placement, enabled)
