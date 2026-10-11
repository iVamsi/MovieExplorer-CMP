package com.movieapp.data.movies.repository

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
import com.movieapp.core.database.dao.MovieDao
import com.movieapp.core.database.entity.MovieEntity
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.model.Credits
import com.movieapp.core.model.ExternalIds
import com.movieapp.core.model.Genre
import com.movieapp.core.model.ImageResponse
import com.movieapp.core.model.Keyword
import com.movieapp.core.model.Movie
import com.movieapp.core.model.MultiSearchListRow
import com.movieapp.core.model.Review
import com.movieapp.core.model.WatchProviders
import com.movieapp.core.network.TmdbApiService
import com.movieapp.data.movies.mapper.MovieMapper
import com.movieapp.data.movies.mediator.MovieCategory
import com.movieapp.data.movies.mediator.MovieRemoteMediator
import com.movieapp.data.movies.paging.MovieRelatedPagingSource
import com.movieapp.data.movies.paging.MovieReviewPagingSource
import com.movieapp.data.movies.paging.MovieSearchPagingSource
import com.movieapp.data.movies.paging.MultiSearchPagingSource
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import co.touchlab.kermit.Logger

@OptIn(ExperimentalPagingApi::class)
class MovieRepositoryImpl(
    private val apiService: TmdbApiService,
    private val movieDao: MovieDao,
    private val transactionRunner: TransactionRunner,
    private val remoteKeysDao: RemoteKeysDao,
    private val mapper: MovieMapper,
    private val clock: Clock,
    private val preferencesRepository: PreferencesRepository,
    private val dispatchers: AppDispatchers,
) : MovieRepository {

    private fun cacheCutoffMillis(): Long =
        clock.now().toEpochMilliseconds() - Constants.Database.CACHE_DURATION_HOURS * 60 * 60 * 1000

    private suspend fun tmdbLanguage(): String = preferencesRepository.tmdbLanguage.first()

    private suspend fun tmdbRegion(): String = preferencesRepository.watchRegion.first()

    /**
     * Shared cache fallback for non-paged home-screen list endpoints.
     *
     * Intentionally returns any fresh cached movies regardless of their original category (popular
     * / top-rated / upcoming / now-playing). These endpoints power home-screen rows where showing a
     * mix of cached content on network failure is preferable to an empty state. The paged
     * equivalents (getPopularMoviesPaged, etc.) use RemoteMediator with per-category Room queries
     * and are not affected by this cross-category behaviour.
     */
    private suspend fun cachedMoviesFallback(label: String, e: Throwable): List<Movie>? {
        Logger.withTag(TAG).e(e) { "Error in $label, falling back to cache" }
        val entities = movieDao.getCachedMovies(cacheCutoffMillis()).first()
        return if (entities.isNotEmpty()) {
            Logger.withTag(TAG).d { "Using cached data for $label, ${entities.size} movies" }
            mapper.mapToDomainList(entities)
        } else {
            Logger.withTag(TAG).w { "No cached data available for $label" }
            null
        }
    }

    override fun getPopularMovies(page: Int): Flow<Result<List<Movie>>> =
        safeResultFlow(
                fetch = {
                    Logger.withTag(TAG).d { "Making API call for popular movies, page: $page" }
                    val response =
                        apiService.getPopularMovies(
                            page = page,
                            language = tmdbLanguage(),
                            region = tmdbRegion(),
                        )
                    movieDao.insertMovies(mapper.mapToEntityList(response.results))
                    response.results
                },
                fallback = { e -> cachedMoviesFallback("getPopularMovies", e) },
            )
            .flowOn(dispatchers.io)

    override fun getPopularMoviesPaged(): Flow<PagingData<Movie>> {
        return Pager(
                config = PagingConfig(pageSize = 20),
                remoteMediator =
                    MovieRemoteMediator(
                        category = MovieCategory.POPULAR,
                        apiService = apiService,
                        transactionRunner = transactionRunner,
                        remoteKeysDao = remoteKeysDao,
                        movieDao = movieDao,
                        mapper = mapper,
                    ),
                pagingSourceFactory = { movieDao.getMoviesPagingSource() },
            )
            .flow
            .map { pagingData -> pagingData.map { mapper.mapToDomain(it) } }
    }

    override fun getTopRatedMovies(page: Int): Flow<Result<List<Movie>>> =
        safeResultFlow(
                fetch = {
                    val response =
                        apiService.getTopRatedMovies(
                            page = page,
                            language = tmdbLanguage(),
                            region = tmdbRegion(),
                        )
                    movieDao.insertMovies(mapper.mapToEntityList(response.results))
                    response.results
                },
                fallback = { e -> cachedMoviesFallback("getTopRatedMovies", e) },
            )
            .flowOn(dispatchers.io)

    override fun getTopRatedMoviesPaged(): Flow<PagingData<Movie>> {
        return Pager(
                config = PagingConfig(pageSize = 20),
                remoteMediator =
                    MovieRemoteMediator(
                        category = MovieCategory.TOP_RATED,
                        apiService = apiService,
                        transactionRunner = transactionRunner,
                        remoteKeysDao = remoteKeysDao,
                        movieDao = movieDao,
                        mapper = mapper,
                    ),
                pagingSourceFactory = { movieDao.getTopRatedMoviesPagingSource() },
            )
            .flow
            .map { pagingData -> pagingData.map { mapper.mapToDomain(it) } }
    }

    override fun getUpcomingMovies(page: Int): Flow<Result<List<Movie>>> =
        safeResultFlow(
                fetch = {
                    val response =
                        apiService.getUpcomingMovies(
                            page = page,
                            language = tmdbLanguage(),
                            region = tmdbRegion(),
                        )
                    movieDao.insertMovies(mapper.mapToEntityList(response.results))
                    response.results
                },
                fallback = { e -> cachedMoviesFallback("getUpcomingMovies", e) },
            )
            .flowOn(dispatchers.io)

    override fun getUpcomingMoviesPaged(): Flow<PagingData<Movie>> {
        return Pager(
                config = PagingConfig(pageSize = 20),
                remoteMediator =
                    MovieRemoteMediator(
                        category = MovieCategory.UPCOMING,
                        apiService = apiService,
                        transactionRunner = transactionRunner,
                        remoteKeysDao = remoteKeysDao,
                        movieDao = movieDao,
                        mapper = mapper,
                    ),
                pagingSourceFactory = { movieDao.getUpcomingMoviesPagingSource() },
            )
            .flow
            .map { pagingData -> pagingData.map { mapper.mapToDomain(it) } }
    }

    override fun getNowPlayingMovies(page: Int): Flow<Result<List<Movie>>> =
        safeResultFlow(
                fetch = {
                    val response =
                        apiService.getNowPlayingMovies(
                            page = page,
                            language = tmdbLanguage(),
                            region = tmdbRegion(),
                        )
                    movieDao.insertMovies(mapper.mapToEntityList(response.results))
                    response.results
                },
                fallback = { e -> cachedMoviesFallback("getNowPlayingMovies", e) },
            )
            .flowOn(dispatchers.io)

    override fun getNowPlayingMoviesPaged(): Flow<PagingData<Movie>> {
        return Pager(
                config = PagingConfig(pageSize = 20),
                remoteMediator =
                    MovieRemoteMediator(
                        category = MovieCategory.NOW_PLAYING,
                        apiService = apiService,
                        transactionRunner = transactionRunner,
                        remoteKeysDao = remoteKeysDao,
                        movieDao = movieDao,
                        mapper = mapper,
                    ),
                pagingSourceFactory = { movieDao.getNowPlayingMoviesPagingSource() },
            )
            .flow
            .map { pagingData -> pagingData.map { mapper.mapToDomain(it) } }
    }

    override fun getMovieDetails(movieId: Int): Flow<Result<Movie>> =
        safeResultFlow(
                fetch = {
                    val movie =
                        apiService.getMovieDetails(
                            movieId,
                            language = tmdbLanguage(),
                            appendToResponse = "videos",
                        )
                    movieDao.insertMovie(mapper.mapToEntity(movie))
                    movie
                },
                fallback = {
                    val cachedMovie = movieDao.getMovieById(movieId)
                    if (cachedMovie != null) mapper.mapToDomain(cachedMovie) else null
                },
            )
            .flowOn(dispatchers.io)

    override fun searchMovies(query: String): Flow<Result<List<Movie>>> =
        safeResultFlow(
                fetch = {
                    val response =
                        apiService.searchMovies(
                            query = query,
                            language = tmdbLanguage(),
                            region = tmdbRegion(),
                        )
                    movieDao.insertMovies(mapper.mapToEntityList(response.results))
                    response.results
                },
                fallback = {
                    val entities = movieDao.searchMovies(query).first()
                    if (entities.isNotEmpty()) mapper.mapToDomainList(entities) else null
                },
            )
            .flowOn(dispatchers.io)

    override fun discoverMovies(
        year: Int?,
        ratingGte: Double?,
        genreIds: List<Int>?,
        watchProviderIds: List<Int>?,
        watchRegion: String?,
    ): Flow<Result<List<Movie>>> =
        safeResultFlow(
                fetch = {
                    val response =
                        apiService.discoverMovies(
                            year = year,
                            voteAverageGte = ratingGte,
                            withGenres = genreIds?.joinToString(","),
                            withWatchProviders = watchProviderIds?.joinToString("|"),
                            watchRegion = watchRegion ?: tmdbRegion(),
                            language = tmdbLanguage(),
                        )
                    movieDao.insertMovies(mapper.mapToEntityList(response.results))
                    response.results
                }
            )
            .flowOn(dispatchers.io)

    override fun getMoviesByGenre(genreId: Int): Flow<Result<List<Movie>>> =
        safeResultFlow(
                fetch = {
                    val response =
                        apiService.discoverMovies(
                            withGenres = genreId.toString(),
                            language = tmdbLanguage(),
                        )
                    movieDao.insertMovies(mapper.mapToEntityList(response.results))
                    response.results
                },
                fallback = {
                    val entities = movieDao.getMoviesByGenre(genreId).first()
                    if (entities.isNotEmpty()) mapper.mapToDomainList(entities) else null
                },
            )
            .flowOn(dispatchers.io)

    override fun getTrendingMovies(): Flow<Result<List<Movie>>> =
        safeResultFlow(
                fetch = {
                    val response =
                        apiService.getTrendingMovies(
                            timeWindow = Constants.TimeWindow.DAY,
                            language = tmdbLanguage(),
                        )
                    movieDao.insertMovies(mapper.mapToEntityList(response.results))
                    response.results
                },
                fallback = { e -> cachedMoviesFallback("getTrendingMovies", e) },
            )
            .flowOn(dispatchers.io)

    override fun getWatchlistMovies(): Flow<Result<List<Movie>>> =
        movieDao
            .getWatchlistMovies()
            .map<List<MovieEntity>, Result<List<Movie>>> { entities ->
                Result.Success(mapper.mapToDomainList(entities))
            }
            .onStart { emit(Result.Loading) }
            .catch { exception -> emit(Result.Error(exception)) }
            .flowOn(dispatchers.io)

    override fun getFavoriteMovies(): Flow<Result<List<Movie>>> =
        movieDao
            .getFavoriteMovies()
            .map<List<MovieEntity>, Result<List<Movie>>> { entities ->
                Result.Success(mapper.mapToDomainList(entities))
            }
            .onStart { emit(Result.Loading) }
            .catch { exception -> emit(Result.Error(exception)) }
            .flowOn(dispatchers.io)

    override fun getMoviesInCollection(collectionId: String): Flow<Result<List<Movie>>> =
        movieDao
            .getMoviesInCollection(collectionId)
            .map<List<MovieEntity>, Result<List<Movie>>> { entities ->
                Result.Success(mapper.mapToDomainList(entities))
            }
            .onStart { emit(Result.Loading) }
            .catch { exception -> emit(Result.Error(exception)) }
            .flowOn(dispatchers.io)

    override fun getMovieById(movieId: Int): Flow<Result<Movie>> =
        movieDao
            .getMovieByIdFlow(movieId)
            .map<MovieEntity?, Result<Movie>> { entity ->
                if (entity != null) {
                    Result.Success(mapper.mapToDomain(entity))
                } else {
                    Result.Error(Exception("Movie not found"))
                }
            }
            .onStart { emit(Result.Loading) }
            .catch { exception -> emit(Result.Error(exception)) }
            .flowOn(dispatchers.io)

    override suspend fun refreshMovies() =
        withContext(dispatchers.io) {
            try {
                movieDao.deleteOldMovies(cacheCutoffMillis())
                val lang = preferencesRepository.tmdbLanguage.first()
                val region = preferencesRepository.watchRegion.first()
                val popularResponse =
                    apiService.getPopularMovies(page = 1, language = lang, region = region)
                val topRatedResponse =
                    apiService.getTopRatedMovies(page = 1, language = lang, region = region)
                val upcomingResponse =
                    apiService.getUpcomingMovies(page = 1, language = lang, region = region)
                val nowPlayingResponse =
                    apiService.getNowPlayingMovies(page = 1, language = lang, region = region)
                val allMovies =
                    popularResponse.results +
                        topRatedResponse.results +
                        upcomingResponse.results +
                        nowPlayingResponse.results
                movieDao.insertMovies(mapper.mapToEntityList(allMovies))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Refresh failure is non-critical; cached data remains valid
            }
        }

    override fun searchMoviesPaged(
        query: String,
        language: String,
        region: String?,
    ): Flow<PagingData<Movie>> =
        Pager(
                config = PagingConfig(pageSize = Constants.Pagination.DEFAULT_PAGE_SIZE),
                pagingSourceFactory = {
                    MovieSearchPagingSource(apiService, query, language, region)
                },
            )
            .flow

    override fun multiSearchPaged(
        query: String,
        language: String,
        region: String?,
    ): Flow<PagingData<MultiSearchListRow>> =
        Pager(
                config = PagingConfig(pageSize = Constants.Pagination.DEFAULT_PAGE_SIZE),
                pagingSourceFactory = {
                    MultiSearchPagingSource(apiService, query, language, region)
                },
            )
            .flow

    override fun getSimilarMoviesPaged(movieId: Int, language: String): Flow<PagingData<Movie>> =
        Pager(
                config = PagingConfig(pageSize = Constants.Pagination.DEFAULT_PAGE_SIZE),
                pagingSourceFactory = {
                    MovieRelatedPagingSource(
                        apiService = apiService,
                        movieId = movieId,
                        language = language,
                        fetchPage = { id, page, lang ->
                            apiService.getSimilarMovies(id, page, lang)
                        },
                    )
                },
            )
            .flow

    override fun getRecommendedMoviesPaged(
        movieId: Int,
        language: String,
    ): Flow<PagingData<Movie>> =
        Pager(
                config = PagingConfig(pageSize = Constants.Pagination.DEFAULT_PAGE_SIZE),
                pagingSourceFactory = {
                    MovieRelatedPagingSource(
                        apiService = apiService,
                        movieId = movieId,
                        language = language,
                        fetchPage = { id, page, lang ->
                            apiService.getMovieRecommendations(id, page, lang)
                        },
                    )
                },
            )
            .flow

    override fun getMovieWatchProviders(movieId: Int): Flow<Result<WatchProviders>> =
        safeResultFlow(fetch = { apiService.getMovieWatchProviders(movieId) }).flowOn(dispatchers.io)

    override suspend fun getMovieCredits(movieId: Int, language: String): Result<Credits> =
        withContext(dispatchers.io) { safeCall { apiService.getMovieCredits(movieId, language) } }

    override fun getMovieReviewsPaged(movieId: Int, language: String): Flow<PagingData<Review>> =
        Pager(
                config = PagingConfig(pageSize = Constants.Pagination.DEFAULT_PAGE_SIZE),
                pagingSourceFactory = { MovieReviewPagingSource(apiService, movieId, language) },
            )
            .flow

    override suspend fun getMovieKeywords(movieId: Int): Result<List<Keyword>> =
        withContext(dispatchers.io) { safeCall { apiService.getMovieKeywords(movieId).keywords } }

    override suspend fun getMovieExternalIds(movieId: Int): Result<ExternalIds> =
        withContext(dispatchers.io) { safeCall { apiService.getMovieExternalIds(movieId) } }

    override suspend fun getMovieImages(movieId: Int, language: String): Result<ImageResponse> =
        withContext(dispatchers.io) { safeCall { apiService.getMovieImages(movieId, language) } }

    override fun getMovieGenresList(language: String): Flow<Result<List<Genre>>> =
        safeResultFlow(fetch = { apiService.getMovieGenres(language).genres }).flowOn(dispatchers.io)

    private companion object {
        const val TAG = "MovieRepositoryImpl"
    }
}
