package com.matchpulse.live.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.matchpulse.live.core.database.entity.CacheMetadataEntity
import com.matchpulse.live.core.database.entity.CompetitionEntity
import com.matchpulse.live.core.database.entity.MatchEntity
import com.matchpulse.live.core.database.entity.RemoteConfigEntity
import com.matchpulse.live.core.database.entity.TvGuideCompetitionEntity
import com.matchpulse.live.core.database.entity.TvScheduleEntity

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches WHERE bucket = :bucket ORDER BY kickoffTimeMillis ASC")
    suspend fun matchesForBucket(bucket: String): List<MatchEntity>

    @Query("SELECT * FROM matches WHERE id = :matchId LIMIT 1")
    suspend fun matchById(matchId: String): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(matches: List<MatchEntity>)
}

@Dao
interface CompetitionDao {
    @Query("SELECT * FROM competitions ORDER BY name ASC")
    suspend fun allCompetitions(): List<CompetitionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(competitions: List<CompetitionEntity>)
}

@Dao
interface TvGuideDao {
    @Query("SELECT * FROM tv_guide_competitions ORDER BY name ASC")
    suspend fun allCompetitions(): List<TvGuideCompetitionEntity>

    @Query("SELECT * FROM tv_schedules WHERE competitionId = :competitionId ORDER BY kickoffTimeMillis ASC")
    suspend fun schedulesForCompetition(competitionId: String): List<TvScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCompetitions(competitions: List<TvGuideCompetitionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSchedules(schedules: List<TvScheduleEntity>)
}

@Dao
interface CacheMetadataDao {
    @Query("SELECT * FROM cache_metadata WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): CacheMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CacheMetadataEntity)
}

@Dao
interface RemoteConfigDao {
    @Query("SELECT * FROM remote_config WHERE id = 'remote_config' LIMIT 1")
    suspend fun get(): RemoteConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RemoteConfigEntity)
}
