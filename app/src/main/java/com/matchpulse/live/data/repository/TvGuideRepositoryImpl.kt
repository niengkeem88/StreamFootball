package com.matchpulse.live.data.repository

import com.matchpulse.live.core.config.AppConfig
import com.matchpulse.live.core.database.CachePolicy
import com.matchpulse.live.core.database.dao.CacheMetadataDao
import com.matchpulse.live.core.database.dao.TvGuideDao
import com.matchpulse.live.core.database.entity.CacheMetadataEntity
import com.matchpulse.live.core.network.ApiService
import com.matchpulse.live.core.network.NetworkErrorMapper
import com.matchpulse.live.data.mapper.toDomain
import com.matchpulse.live.data.mapper.toEntity
import com.matchpulse.live.data.remote.mock.MockTvGuideDataSource
import com.matchpulse.live.domain.model.DataState
import com.matchpulse.live.domain.model.TvGuideCompetition
import com.matchpulse.live.domain.model.TvScheduleItem
import com.matchpulse.live.domain.repository.TvGuideRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

@Singleton
class TvGuideRepositoryImpl @Inject constructor(
    private val appConfig: AppConfig,
    private val apiService: ApiService,
    private val mockSource: MockTvGuideDataSource,
    private val tvGuideDao: TvGuideDao,
    private val cacheMetadataDao: CacheMetadataDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TvGuideRepository {
    override fun getTvGuideCompetitions(region: String?, category: String?): Flow<DataState<List<TvGuideCompetition>>> = flow {
        emit(DataState.Loading)
        val now = System.currentTimeMillis()
        val cached = tvGuideDao.allCompetitions().map { it.toDomain() }.filterBy(region, category)
        val meta = cacheMetadataDao.get("tv-guide:competitions")
        if (cached.isNotEmpty() && CachePolicy.isFresh(meta?.updatedAtMillis, now, CachePolicy.TV_GUIDE_MS)) {
            emit(DataState.Success(cached))
            return@flow
        }
        if (cached.isNotEmpty()) emit(DataState.Success(cached, stale = true))
        try {
            val fresh = if (appConfig.isBackendMode) apiService.tvGuideCompetitions().map { it.toDomain() } else mockSource.competitions
            tvGuideDao.upsertCompetitions(fresh.map { it.toEntity(now) })
            cacheMetadataDao.upsert(CacheMetadataEntity("tv-guide:competitions", now))
            emit(DataState.Success(fresh.filterBy(region, category)))
        } catch (throwable: Throwable) {
            emit(DataState.Error(NetworkErrorMapper.userMessage(throwable), cached.takeIf { it.isNotEmpty() }, cached.isNotEmpty()))
        }
    }.flowOn(ioDispatcher)

    override fun getTvSchedule(competitionId: String, forceRefresh: Boolean): Flow<DataState<List<TvScheduleItem>>> = flow {
        emit(DataState.Loading)
        val now = System.currentTimeMillis()
        val key = "tv-guide:schedule:$competitionId"
        val cached = tvGuideDao.schedulesForCompetition(competitionId).map { it.toDomain(stale = true) }
        val meta = cacheMetadataDao.get(key)
        if (!forceRefresh && cached.isNotEmpty() && CachePolicy.isFresh(meta?.updatedAtMillis, now, CachePolicy.TV_GUIDE_MS)) {
            emit(DataState.Success(cached))
            return@flow
        }
        if (cached.isNotEmpty()) emit(DataState.Success(cached, stale = true))
        try {
            val fresh = if (appConfig.isBackendMode) apiService.tvSchedule(competitionId).map { it.toDomain() } else mockSource.schedule(competitionId)
            tvGuideDao.upsertSchedules(fresh.map { it.toEntity(now) })
            cacheMetadataDao.upsert(CacheMetadataEntity(key, now))
            emit(DataState.Success(fresh))
        } catch (throwable: Throwable) {
            emit(DataState.Error(NetworkErrorMapper.userMessage(throwable), cached.takeIf { it.isNotEmpty() }, cached.isNotEmpty()))
        }
    }.flowOn(ioDispatcher)

    private fun List<TvGuideCompetition>.filterBy(region: String?, category: String?): List<TvGuideCompetition> = filter {
        (region.isNullOrBlank() || it.region.equals(region, ignoreCase = true)) &&
            (category.isNullOrBlank() || category.equals("All", ignoreCase = true) || it.category.equals(category, ignoreCase = true))
    }
}
