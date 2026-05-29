package com.matchpulse.live.data.repository

import com.matchpulse.live.core.config.AppConfig
import com.matchpulse.live.core.database.CachePolicy
import com.matchpulse.live.core.database.dao.RemoteConfigDao
import com.matchpulse.live.core.network.ApiService
import com.matchpulse.live.core.network.NetworkErrorMapper
import com.matchpulse.live.data.mapper.toDomain
import com.matchpulse.live.data.mapper.toEntity
import com.matchpulse.live.domain.model.AppRemoteConfig
import com.matchpulse.live.domain.model.DataState
import com.matchpulse.live.domain.repository.AppConfigRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

@Singleton
class AppConfigRepositoryImpl @Inject constructor(
    private val appConfig: AppConfig,
    private val apiService: ApiService,
    private val remoteConfigDao: RemoteConfigDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AppConfigRepository {
    override fun getRemoteConfig(forceRefresh: Boolean): Flow<DataState<AppRemoteConfig>> = flow {
        val now = System.currentTimeMillis()
        val cachedEntity = remoteConfigDao.get()
        val cached = cachedEntity?.toDomain()
        if (!forceRefresh && cached != null && CachePolicy.isFresh(cachedEntity.updatedAtMillis, now, CachePolicy.REMOTE_CONFIG_MS)) {
            emit(DataState.Success(cached))
            return@flow
        }
        if (cached != null) emit(DataState.Success(cached, stale = true))
        if (!appConfig.isBackendMode) {
            emit(DataState.Success(cached ?: AppRemoteConfig()))
            return@flow
        }
        try {
            val fresh = apiService.remoteConfig().toDomain()
            remoteConfigDao.upsert(fresh.toEntity(now))
            emit(DataState.Success(fresh))
        } catch (throwable: Throwable) {
            emit(DataState.Error(NetworkErrorMapper.userMessage(throwable), cached ?: AppRemoteConfig(), cached != null))
        }
    }.flowOn(ioDispatcher)
}
