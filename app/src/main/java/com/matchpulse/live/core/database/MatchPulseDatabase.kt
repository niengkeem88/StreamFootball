package com.matchpulse.live.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.matchpulse.live.core.database.dao.CacheMetadataDao
import com.matchpulse.live.core.database.dao.CompetitionDao
import com.matchpulse.live.core.database.dao.MatchDao
import com.matchpulse.live.core.database.dao.RemoteConfigDao
import com.matchpulse.live.core.database.dao.TvGuideDao
import com.matchpulse.live.core.database.entity.CacheMetadataEntity
import com.matchpulse.live.core.database.entity.CompetitionEntity
import com.matchpulse.live.core.database.entity.MatchEntity
import com.matchpulse.live.core.database.entity.RemoteConfigEntity
import com.matchpulse.live.core.database.entity.TvGuideCompetitionEntity
import com.matchpulse.live.core.database.entity.TvScheduleEntity

@Database(
    entities = [
        MatchEntity::class,
        CompetitionEntity::class,
        TvGuideCompetitionEntity::class,
        TvScheduleEntity::class,
        CacheMetadataEntity::class,
        RemoteConfigEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class MatchPulseDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun competitionDao(): CompetitionDao
    abstract fun tvGuideDao(): TvGuideDao
    abstract fun cacheMetadataDao(): CacheMetadataDao
    abstract fun remoteConfigDao(): RemoteConfigDao
}
