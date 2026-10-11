package com.movieapp.data.user.repository

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import com.movieapp.core.common.Constants
import com.movieapp.core.common.Result
import com.movieapp.core.common.dispatchers.AppDispatchers
import com.movieapp.core.database.dao.MovieDao
import com.movieapp.core.database.dao.OfflineContentDao
import com.movieapp.core.database.dao.TvSeriesDao
import com.movieapp.core.database.entity.OfflineContentEntity
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.OfflineContentRepository
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.Movie
import com.movieapp.core.model.OfflineContent
import com.movieapp.core.model.TvSeries
import kotlin.time.Clock
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import co.touchlab.kermit.Logger

class OfflineContentRepositoryImpl(
    private val offlineContentDao: OfflineContentDao,
    private val movieDao: MovieDao,
    private val tvSeriesDao: TvSeriesDao,
    private val movieRepository: MovieRepository,
    private val seriesRepository: SeriesRepository,
    private val imageLoader: ImageLoader,
    private val platformContext: PlatformContext,
    private val clock: Clock,
    private val dispatchers: AppDispatchers,
) : OfflineContentRepository {

    override fun prefetchContent(
        contentId: Int,
        contentType: ContentType,
    ): Flow<Result<OfflineContent>> = flow {
        emit(Result.Loading)
        val source = fetchSource(contentId = contentId, contentType = contentType)
        val entity =
            OfflineContentEntity(
                contentId = contentId,
                contentType = contentType.name,
                prefetchedAt = clock.now().toEpochMilliseconds(),
                metadataVersion = null,
                posterCached = source.posterPath.prefetchImage(Constants.ImageSize.POSTER_MEDIUM),
                backdropCached =
                    source.backdropPath.prefetchImage(Constants.ImageSize.BACKDROP_MEDIUM),
            )
        offlineContentDao.upsertOfflineContent(entity)
        emit(Result.Success(entity.toOfflineContent(source.title, source.posterPath)))
    }
        .catch { throwable ->
            if (throwable is CancellationException) throw throwable
            Logger.e(throwable) { "Unable to prefetch content for offline use" }
            emit(Result.Error(throwable))
        }
        .flowOn(dispatchers.io)

    override fun getOfflineContent(): Flow<List<OfflineContent>> =
        offlineContentDao
            .getOfflineContent()
            .map { entities -> entities.mapNotNull { it.toOfflineContent() } }
            .catch { throwable ->
                if (throwable is CancellationException) throw throwable
                Logger.e(throwable) { "Unable to read offline content" }
                emit(emptyList())
            }
            .flowOn(dispatchers.io)

    override fun observeOfflineContent(
        contentId: Int,
        contentType: ContentType,
    ): Flow<OfflineContent?> =
        offlineContentDao
            .observeOfflineContent(contentId = contentId, contentType = contentType.name)
            .map { it?.toOfflineContent() }
            .catch { throwable ->
                if (throwable is CancellationException) throw throwable
                Logger.e(throwable) { "Unable to observe offline content" }
                emit(null)
            }
            .flowOn(dispatchers.io)

    override suspend fun deleteOfflineContent(
        contentId: Int,
        contentType: ContentType,
    ): Result<Unit> =
        try {
            offlineContentDao.deleteOfflineContent(
                contentId = contentId,
                contentType = contentType.name,
            )
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.e(e) { "Unable to delete offline content" }
            Result.Error(e)
        }

    override suspend fun clearOfflineContent(): Result<Unit> =
        try {
            offlineContentDao.clearOfflineContent()
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.e(e) { "Unable to clear offline content" }
            Result.Error(e)
        }

    private suspend fun fetchSource(contentId: Int, contentType: ContentType): OfflineSource =
        when (contentType) {
            ContentType.MOVIE ->
                when (
                    val result =
                        movieRepository
                            .getMovieDetails(contentId)
                            .filter { it !is Result.Loading }
                            .first()
                ) {
                    is Result.Success -> result.data.toOfflineSource()
                    is Result.Error -> throw result.exception
                    Result.Loading -> error("Movie details did not finish loading")
                }

            ContentType.TV_SERIES ->
                when (
                    val result =
                        seriesRepository
                            .getSeriesByIdFlow(contentId)
                            .filter { it !is Result.Loading }
                            .first()
                ) {
                    is Result.Success -> result.data.toOfflineSource()
                    is Result.Error -> throw result.exception
                    Result.Loading -> error("Series details did not finish loading")
                }

            ContentType.ACTOR -> error("Actors cannot be prefetched for offline use")
        }

    private suspend fun OfflineContentEntity.toOfflineContent(): OfflineContent? =
        when (contentType.toContentTypeOrNull()) {
            ContentType.MOVIE -> {
                val movie = movieDao.getMovieById(contentId)
                toOfflineContent(title = movie?.title.orEmpty(), posterPath = movie?.posterPath)
            }

            ContentType.TV_SERIES -> {
                val series = tvSeriesDao.getTvSeriesById(contentId)
                toOfflineContent(title = series?.name.orEmpty(), posterPath = series?.posterPath)
            }

            else -> null
        }

    private fun OfflineContentEntity.toOfflineContent(
        title: String,
        posterPath: String?,
    ): OfflineContent =
        OfflineContent(
            contentId = contentId,
            contentType = contentType.toContentTypeOrNull() ?: ContentType.MOVIE,
            title = title,
            posterPath = posterPath,
            prefetchedAt = prefetchedAt,
            posterCached = posterCached,
            backdropCached = backdropCached,
        )

    private suspend fun String?.prefetchImage(size: String): Boolean {
        if (isNullOrBlank()) return false
        val url = "${Constants.Api.IMAGE_BASE_URL}$size$this"
        return try {
            val request =
                ImageRequest.Builder(platformContext)
                    .data(url)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build()
            imageLoader.execute(request) !is ErrorResult
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Logger.w(e) { "Unable to enqueue offline image prefetch" }
            false
        }
    }

    private fun Movie.toOfflineSource(): OfflineSource =
        OfflineSource(title = title, posterPath = posterPath, backdropPath = backdropPath)

    private fun TvSeries.toOfflineSource(): OfflineSource =
        OfflineSource(title = name, posterPath = posterPath, backdropPath = backdropPath)

    private fun String?.toContentTypeOrNull(): ContentType? =
        when (this) {
            ContentType.MOVIE.name,
            "movie" -> ContentType.MOVIE

            ContentType.TV_SERIES.name,
            "tv_series" -> ContentType.TV_SERIES

            else -> null
        }

    private data class OfflineSource(
        val title: String,
        val posterPath: String?,
        val backdropPath: String?,
    )
}
