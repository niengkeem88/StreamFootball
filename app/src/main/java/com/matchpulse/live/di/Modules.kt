package com.matchpulse.live.di

import android.content.Context
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.matchpulse.live.core.config.AppConfig
import com.matchpulse.live.core.database.MatchPulseDatabase
import com.matchpulse.live.core.database.dao.CacheMetadataDao
import com.matchpulse.live.core.database.dao.CompetitionDao
import com.matchpulse.live.core.database.dao.MatchDao
import com.matchpulse.live.core.database.dao.RemoteConfigDao
import com.matchpulse.live.core.database.dao.TvGuideDao
import com.matchpulse.live.core.datastore.UserPreferencesRepository
import com.matchpulse.live.core.network.ApiService
import com.matchpulse.live.core.network.AuthInterceptor
import com.matchpulse.live.core.network.LoggingInterceptorProvider
import com.matchpulse.live.data.remote.api.FootballApiService
import com.matchpulse.live.data.repository.AppConfigRepositoryImpl
import com.matchpulse.live.data.repository.FootballRepositoryImpl
import com.matchpulse.live.data.repository.IoDispatcher
import com.matchpulse.live.data.repository.TvGuideRepositoryImpl
import com.matchpulse.live.domain.repository.AppConfigRepository
import com.matchpulse.live.domain.repository.FavoritesRepository
import com.matchpulse.live.domain.repository.FootballRepository
import com.matchpulse.live.domain.repository.SettingsRepository
import com.matchpulse.live.domain.repository.TvGuideRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {
    @Provides
    @Singleton
    fun provideAppConfig(): AppConfig = AppConfig.current()
}

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MatchPulseDatabase =
        Room.databaseBuilder(context, MatchPulseDatabase::class.java, "matchpulse.db")
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides fun provideMatchDao(db: MatchPulseDatabase): MatchDao = db.matchDao()
    @Provides fun provideCompetitionDao(db: MatchPulseDatabase): CompetitionDao = db.competitionDao()
    @Provides fun provideTvGuideDao(db: MatchPulseDatabase): TvGuideDao = db.tvGuideDao()
    @Provides fun provideCacheMetadataDao(db: MatchPulseDatabase): CacheMetadataDao = db.cacheMetadataDao()
    @Provides fun provideRemoteConfigDao(db: MatchPulseDatabase): RemoteConfigDao = db.remoteConfigDao()
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(appConfig: AppConfig, authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(LoggingInterceptorProvider.create(appConfig))
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(appConfig: AppConfig, okHttpClient: OkHttpClient, json: Json): Retrofit {
        val baseUrl = appConfig.apiBaseUrl.takeIf { it.startsWith("https://") } ?: "https://matchpulse.invalid/"
        return Retrofit.Builder()
            .baseUrl(baseUrl.ensureTrailingSlash())
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideFootballApiService(
        json: Json,
        authInterceptor: com.matchpulse.live.data.remote.api.AuthInterceptor,
        appConfig: AppConfig
    ): FootballApiService {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(LoggingInterceptorProvider.create(appConfig))
            .build()

        return Retrofit.Builder()
            .baseUrl(FootballApiService.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FootballApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFootballAuthInterceptor(): com.matchpulse.live.data.remote.api.AuthInterceptor = 
        com.matchpulse.live.data.remote.api.AuthInterceptor()

    private fun String.ensureTrailingSlash(): String = if (endsWith("/")) this else "$this/"
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindFootballRepository(impl: FootballRepositoryImpl): FootballRepository
    @Binds abstract fun bindTvGuideRepository(impl: TvGuideRepositoryImpl): TvGuideRepository
    @Binds abstract fun bindSettingsRepository(impl: UserPreferencesRepository): SettingsRepository
    @Binds abstract fun bindFavoritesRepository(impl: UserPreferencesRepository): FavoritesRepository
    @Binds abstract fun bindAppConfigRepository(impl: AppConfigRepositoryImpl): AppConfigRepository
}
