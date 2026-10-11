package com.movieapp.data.series.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.movieapp.core.common.Constants
import com.movieapp.core.common.Result
import com.movieapp.core.common.dispatchers.AppDispatchers
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.common.safeCall
import com.movieapp.core.common.safeResultFlow
import com.movieapp.core.database.TransactionRunner
import com.movieapp.core.database.dao.RemoteKeysDao
import com.movieapp.core.database.dao.TvSeriesDao
import com.movieapp.core.database.entity.TvSeriesEntity
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.model.Credits
import com.movieapp.core.model.ExternalIds
import com.movieapp.core.model.Genre
import com.movieapp.core.model.ImageResponse
import com.movieapp.core.model.Keyword
import com.movieapp.core.model.Review
import com.movieapp.core.model.TvSeries
import com.movieapp.core.model.WatchProviders
import com.movieapp.core.network.TmdbApiService
import com.movieapp.data.series.mapper.TvSeriesMapper
import com.movieapp.data.series.mediator.SeriesCategory
import com.movieapp.data.series.mediator.SeriesRemoteMediator
import com.movieapp.data.series.paging.SeriesReviewPagingSource
import com.movieapp.data.series.paging.TvSeriesSearchPagingSource
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

@OptIn(ExperimentalPagingApi::class)
class SeriesRepositoryImpl(
    private val apiService: TmdbApiService,
    private val tvSeriesDao: TvSeriesDao,
    private val transactionRunner: TransactionRunner,
    private val remoteKeysDao: RemoteKeysDao,
    private val tvSeriesMapper: TvSeriesMapper,
    private val clock: Clock,
    private val preferencesRepository: PreferencesRepository,
    private val dispatchers: AppDispatchers,
) : SeriesRepository {

    private fun cacheCutoffMillis(): Long =
        clock.now().toEpochMilliseconds() - Constants.Database.CACHE_DURATION_HOURS * 60 * 60 * 1000

    private suspend fun tmdbLanguage(): String = preferencesRepository.tmdbLanguage.first()

    private suspend fun tmdbRegion(): String = preferencesRepository.watchRegion.first()

    override fun getTrendingSeries(): Flow<PagingData<TvSeries>> =
        Pager(
                config = PagingConfig(pageSize = Constants.Pagination.DEFAULT_PAGE_SIZE),
                remoteMediator =
                    SeriesRemoteMediator(
                        SeriesCategory.TRENDING,
                        apiService,
                        transactionRunner,
                        remoteKeysDao,
                        tvSeriesDao,
                        tvSeriesMapper,
                    ),
                pagingSourceFactory = {
                    tvSeriesDao.getTvSeriesByCategoryPagingSource(SeriesCategory.TRENDING.name)
                },
            )
            .flow
            .map { pagingData -> pagingData.map { tvSeriesMapper.mapToDomain(it) } }

    override fun getTrendingTvSeriesPage(): Flow<Result<List<TvSeries>>> =
        safeResultFlow(
                fetch = {
                    val response =
                        apiService.getTrendingTvSeries(
                            timeWindow = Constants.TimeWindow.DAY,
                            page = 1,
                            language = tmdbLanguage(),
                        )
                    val series = response.results
                    tvSeriesDao
                        .insertTvSeriesList(series.map { tvSeriesMapper.mapToEntity(it) })
                    series
                },
                fallback = {
                    val entities = tvSeriesDao.getCachedTvSeries(cacheCutoffMillis()).first()
                    if (entities.isNotEmpty()) tvSeriesMapper.mapToDomainList(entities) else null
                },
            )
            .flowOn(dispatchers.io)

    override fun getPopularSeries(): Flow<PagingData<TvSeries>> =
        Pager(
                config = PagingConfig(pageSize = Constants.Pagination.DEFAULT_PAGE_SIZE),
                remoteMediator =
                    SeriesRemoteMediator(
                        SeriesCategory.POPULAR,
                        apiService,
                        transactionRunner,
                        remoteKeysDao,
                        tvSeriesDao,
                        tvSeriesMapper,
                    ),
                pagingSourceFactory = {
                    tvSeriesDao.getTvSeriesByCategoryPagingSource(SeriesCategory.POPULAR.name)
                },
            )
            .flow
            .map { pagingData -> pagingData.map { tvSeriesMapper.mapToDomain(it) } }

    override fun getTopRatedSeries(): Flow<PagingData<TvSeries>> =
        Pager(
                config = PagingConfig(pageSize = Constants.Pagination.DEFAULT_PAGE_SIZE),
                remoteMediator =
                    SeriesRemoteMediator(
                        SeriesCategory.TOP_RATED,
                        apiService,
                        transactionRunner,
                        remoteKeysDao,
                        tvSeriesDao,
                        tvSeriesMapper,
                    ),
                pagingSourceFactory = {
                    tvSeriesDao.getTvSeriesByCategoryPagingSource(SeriesCategory.TOP_RATED.name)
                },
            )
            .flow
            .map { pagingData -> pagingData.map { tvSeriesMapper.mapToDomain(it) } }

    override fun getAiringToday(): Flow<PagingData<TvSeries>> =
        Pager(
                config = PagingConfig(pageSize = Constants.Pagination.DEFAULT_PAGE_SIZE),
                remoteMediator =
                    SeriesRemoteMediator(
                        SeriesCategory.AIRING_TODAY,
                        apiService,
                        transactionRunner,
                        remoteKeysDao,
                        tvSeriesDao,
                        tvSeriesMapper,
                    ),
                pagingSourceFactory = {
                    tvSeriesDao.getTvSeriesByCategoryPagingSource(SeriesCategory.AIRING_TODAY.name)
                },
            )
            .flow
            .map { pagingData -> pagingData.map { tvSeriesMapper.mapToDomain(it) } }

    override fun getOnTheAir(): Flow<PagingData<TvSeries>> =
        Pager(
                config = PagingConfig(pageSize = Constants.Pagination.DEFAULT_PAGE_SIZE),
                remoteMediator =
                    SeriesRemoteMediator(
                        SeriesCategory.ON_THE_AIR,
                        apiService,
                        transactionRunner,
                        remoteKeysDao,
                        tvSeriesDao,
                        tvSeriesMapper,
                    ),
                pagingSourceFactory = {
                    tvSeriesDao.getTvSeriesByCategoryPagingSource(SeriesCategory.ON_THE_AIR.name)
                },
            )
            .flow
            .map { pagingData -> pagingData.map { tvSeriesMapper.mapToDomain(it) } }

    override suspend fun getSeriesById(seriesId: Int): Result<TvSeries> =
        withContext(dispatchers.io) {
            val fetched = safeCall {
                val series =
                    apiService.getTvSeriesDetails(
                        seriesId,
                        language = tmdbLanguage(),
                        appendToResponse = "videos",
                    )
                tvSeriesDao.insertTvSeries(tvSeriesMapper.mapToEntity(series))
                series
            }
            if (fetched is Result.Error) {
                val cached = tvSeriesDao.getTvSeriesById(seriesId)
                if (cached != null) Result.Success(tvSeriesMapper.mapToDomain(cached)) else fetched
            } else {
                fetched
            }
        }

    override fun getSeriesByIdFlow(seriesId: Int): Flow<Result<TvSeries>> =
        tvSeriesDao
            .getTvSeriesByIdFlow(seriesId)
            .map<TvSeriesEntity?, Result<TvSeries>> { entity ->
                if (entity != null) {
                    Result.Success(tvSeriesMapper.mapToDomain(entity))
                } else {
                    getSeriesById(seriesId)
                }
            }
            .onStart { emit(Result.Loading) }
            .catch { exception -> emit(Result.Error(exception)) }
            .flowOn(dispatchers.io)

    override fun searchSeries(query: String, language: String): Flow<PagingData<TvSeries>> =
        Pager(
                config = PagingConfig(pageSize = Constants.Pagination.DEFAULT_PAGE_SIZE),
                pagingSourceFactory = { TvSeriesSearchPagingSource(apiService, query, language) },
            )
            .flow

    override fun searchSeriesResults(query: String): Flow<Result<List<TvSeries>>> =
        safeResultFlow(
                fetch = {
                    apiService
                        .searchTvSeries(query = query, page = 1, language = tmdbLanguage())
                        .results
                },
                fallback = {
                    val cached = tvSeriesDao.searchTvSeries(query).first()
                    if (cached.isNotEmpty()) tvSeriesMapper.mapToDomainList(cached) else null
                },
            )
            .flowOn(dispatchers.io)

    override fun discoverSeries(
        year: Int?,
        ratingGte: Double?,
        genreIds: List<Int>?,
        watchProviderIds: List<Int>?,
        watchRegion: String?,
    ): Flow<Result<List<TvSeries>>> =
        safeResultFlow(
                fetch = {
                    val response =
                        apiService.discoverTvSeries(
                            firstAirDateYear = year,
                            voteAverageGte = ratingGte,
                            withGenres = genreIds?.joinToString(","),
                            withWatchProviders = watchProviderIds?.joinToString("|"),
                            watchRegion = watchRegion ?: tmdbRegion(),
                            language = tmdbLanguage(),
                        )
                    val series = response.results
                    tvSeriesDao
                        .insertTvSeriesList(series.map { tvSeriesMapper.mapToEntity(it) })
                    series
                }
            )
            .flowOn(dispatchers.io)

    override fun getSimilarSeries(seriesId: Int, language: String): Flow<PagingData<TvSeries>> =
        Pager(
                config = PagingConfig(pageSize = Constants.Pagination.DEFAULT_PAGE_SIZE),
                pagingSourceFactory = {
                    com.movieapp.data.series.paging.TvSeriesRelatedPagingSource(
                        seriesId = seriesId,
                        language = language,
                        fetchPage = { id, page, lang ->
                            apiService.getSimilarTvSeries(id, page, lang)
                        },
                    )
                },
            )
            .flow

    override fun getRecommendedSeries(seriesId: Int, language: String): Flow<PagingData<TvSeries>> =
        Pager(
                config = PagingConfig(pageSize = Constants.Pagination.DEFAULT_PAGE_SIZE),
                pagingSourceFactory = {
                    com.movieapp.data.series.paging.TvSeriesRelatedPagingSource(
                        seriesId = seriesId,
                        language = language,
                        fetchPage = { id, page, lang ->
                            apiService.getTvSeriesRecommendations(id, page, lang)
                        },
                    )
                },
            )
            .flow

    override fun getSeriesByGenre(genreId: Int): Flow<PagingData<TvSeries>> =
        Pager(
                config = PagingConfig(pageSize = Constants.Pagination.DEFAULT_PAGE_SIZE),
                pagingSourceFactory = { tvSeriesDao.getTvSeriesByGenrePagingSource(genreId) },
            )
            .flow
            .map { pagingData -> pagingData.map { tvSeriesMapper.mapToDomain(it) } }

    override fun getWatchlistSeries(): Flow<Result<List<TvSeries>>> =
        tvSeriesDao
            .getWatchlistTvSeries()
            .map<List<TvSeriesEntity>, Result<List<TvSeries>>> { entities ->
                Result.Success(tvSeriesMapper.mapToDomainList(entities))
            }
            .onStart { emit(Result.Loading) }
            .catch { exception -> emit(Result.Error(exception)) }
            .flowOn(dispatchers.io)

    override fun getFavoriteSeries(): Flow<Result<List<TvSeries>>> =
        tvSeriesDao
            .getFavoriteTvSeries()
            .map<List<TvSeriesEntity>, Result<List<TvSeries>>> { entities ->
                Result.Success(tvSeriesMapper.mapToDomainList(entities))
            }
            .onStart { emit(Result.Loading) }
            .catch { exception -> emit(Result.Error(exception)) }
            .flowOn(dispatchers.io)

    override fun getSeriesInCollection(collectionId: String): Flow<Result<List<TvSeries>>> =
        tvSeriesDao
            .getTvSeriesInCollection(collectionId)
            .map<List<TvSeriesEntity>, Result<List<TvSeries>>> { entities ->
                Result.Success(tvSeriesMapper.mapToDomainList(entities))
            }
            .onStart { emit(Result.Loading) }
            .catch { exception -> emit(Result.Error(exception)) }
            .flowOn(dispatchers.io)

    override suspend fun refreshSeries() =
        withContext(dispatchers.io) {
            val lang = preferencesRepository.tmdbLanguage.first()
            SeriesCategory.entries.forEach { category ->
                try {
                    val response =
                        when (category) {
                            SeriesCategory.POPULAR ->
                                apiService.getPopularTvSeries(page = 1, language = lang)
                            SeriesCategory.TOP_RATED ->
                                apiService.getTopRatedTvSeries(page = 1, language = lang)
                            SeriesCategory.AIRING_TODAY ->
                                apiService.getAiringTodayTvSeries(page = 1, language = lang)
                            SeriesCategory.ON_THE_AIR ->
                                apiService.getOnTheAirTvSeries(page = 1, language = lang)
                            SeriesCategory.TRENDING ->
                                apiService.getTrendingTvSeries(
                                    timeWindow = Constants.TimeWindow.DAY,
                                    page = 1,
                                    language = lang,
                                )
                        }
                    tvSeriesDao.deleteByCategory(category.name)
                    val entities =
                        response.results.map { series ->
                            tvSeriesMapper.mapToEntity(series).copy(category = category.name)
                        }
                    tvSeriesDao.insertTvSeriesList(entities)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Ignore per-category failures; continue refreshing others
                }
            }
        }

    override fun getTvWatchProviders(seriesId: Int): Flow<Result<WatchProviders>> =
        safeResultFlow(fetch = { apiService.getTvSeriesWatchProviders(seriesId) })
            .flowOn(dispatchers.io)

    override suspend fun getTvSeriesCredits(seriesId: Int, language: String): Result<Credits> =
        withContext(dispatchers.io) {
            safeCall { apiService.getTvSeriesCredits(seriesId, language) }
        }

    override fun getTvSeriesReviewsPaged(
        seriesId: Int,
        language: String,
    ): Flow<PagingData<Review>> =
        Pager(
                config = PagingConfig(pageSize = Constants.Pagination.DEFAULT_PAGE_SIZE),
                pagingSourceFactory = { SeriesReviewPagingSource(apiService, seriesId, language) },
            )
            .flow

    override suspend fun getTvSeriesKeywords(seriesId: Int): Result<List<Keyword>> =
        withContext(dispatchers.io) {
            safeCall { apiService.getTvSeriesKeywords(seriesId).keywords }
        }

    override suspend fun getTvSeriesExternalIds(seriesId: Int): Result<ExternalIds> =
        withContext(dispatchers.io) { safeCall { apiService.getTvSeriesExternalIds(seriesId) } }

    override suspend fun getTvSeriesImages(seriesId: Int, language: String): Result<ImageResponse> =
        withContext(dispatchers.io) { safeCall { apiService.getTvSeriesImages(seriesId, language) } }

    override fun getTvGenresList(language: String): Flow<Result<List<Genre>>> =
        safeResultFlow(fetch = { apiService.getTvGenres(language).genres }).flowOn(dispatchers.io)
}
