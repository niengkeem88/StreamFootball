package com.matchpulse.live.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val bucket: String,
    val competitionId: String,
    val competitionName: String,
    val competitionRegion: String,
    val competitionCountry: String,
    val competitionCategory: String,
    val competitionDescription: String,
    val homeTeamId: String,
    val homeTeamName: String,
    val homeTeamShortName: String,
    val awayTeamId: String,
    val awayTeamName: String,
    val awayTeamShortName: String,
    val status: String,
    val kickoffTimeMillis: Long,
    val minute: Int?,
    val homeScore: Int?,
    val awayScore: Int?,
    val venue: String,
    val eventsJson: String,
    val statsJson: String?,
    val updatedAtMillis: Long,
)

@Entity(tableName = "competitions")
data class CompetitionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val region: String,
    val country: String,
    val category: String,
    val description: String,
    val updatedAtMillis: Long,
)

@Entity(tableName = "tv_guide_competitions")
data class TvGuideCompetitionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val region: String,
    val country: String,
    val category: String,
    val description: String,
    val updatedAtMillis: Long,
)

@Entity(tableName = "tv_schedules")
data class TvScheduleEntity(
    @PrimaryKey val id: String,
    val competitionId: String,
    val matchTitle: String,
    val kickoffTimeMillis: Long,
    val providersJson: String,
    val updatedAtMillis: Long,
)

@Entity(tableName = "cache_metadata")
data class CacheMetadataEntity(
    @PrimaryKey val key: String,
    val updatedAtMillis: Long,
)

@Entity(tableName = "remote_config")
data class RemoteConfigEntity(
    @PrimaryKey val id: String = "remote_config",
    val payloadJson: String,
    val updatedAtMillis: Long,
)
