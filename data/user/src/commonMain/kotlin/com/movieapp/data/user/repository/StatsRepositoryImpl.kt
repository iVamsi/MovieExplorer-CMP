package com.movieapp.data.user.repository

import com.movieapp.core.common.Result
import com.movieapp.core.common.dispatchers.AppDispatchers
import com.movieapp.core.database.dao.MovieDao
import com.movieapp.core.database.dao.TvSeriesDao
import com.movieapp.core.database.dao.UserDao
import com.movieapp.core.database.entity.FavoriteItemEntity
import com.movieapp.core.database.entity.UserRatingEntity
import com.movieapp.core.domain.repository.StatsRepository
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.Credits
import com.movieapp.core.model.DecadeCount
import com.movieapp.core.model.Genre
import com.movieapp.core.model.GenreCount
import com.movieapp.core.model.PersonCount
import com.movieapp.core.model.WatchStats
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.Json
import co.touchlab.kermit.Logger

class StatsRepositoryImpl(
    private val userDao: UserDao,
    private val movieDao: MovieDao,
    private val tvSeriesDao: TvSeriesDao,
    private val dispatchers: AppDispatchers,
) : StatsRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getUserWatchStats(): Flow<Result<WatchStats>> =
        combine(
                userDao.getAllFavoriteItems(),
                userDao.getAllUserRatings(),
                userDao.getWatchedCollectionItems(),
            ) { favorites, ratings, watchedItems ->
                buildStats(favorites, ratings, watchedItems)
            }
            .map<WatchStats, Result<WatchStats>> { Result.Success(it) }
            .onStart { emit(Result.Loading) }
            .catch { throwable ->
                if (throwable is CancellationException) throw throwable
                Logger.e(throwable) { "Unable to load user watch stats" }
                emit(Result.Error(throwable))
            }
            .flowOn(dispatchers.io)

    private suspend fun buildStats(
        favorites: List<FavoriteItemEntity>,
        ratings: List<UserRatingEntity>,
        watchedItems: List<com.movieapp.core.database.entity.CollectionItemEntity>,
    ): WatchStats {
        val contentKeys = linkedSetOf<ContentKey>()
        favorites.mapNotNullTo(contentKeys) { it.toContentKey() }
        ratings.mapNotNullTo(contentKeys) { it.toContentKey() }
        watchedItems.mapNotNullTo(contentKeys) { it.toContentKey() }

        val content = contentKeys.mapNotNull { it.toStatsContent() }

        return WatchStats(
            genreBreakdown = content.toGenreBreakdown(),
            totalWatchTimeMinutes = content.sumOf { it.watchTimeMinutes },
            decadeBreakdown = content.toDecadeBreakdown(),
            topPeople = content.toTopPeople(),
            ratingHistogram = ratings.toRatingHistogram(),
        )
    }

    private suspend fun ContentKey.toStatsContent(): StatsContent? =
        when (type) {
            ContentType.MOVIE ->
                movieDao.getMovieById(contentId)?.let { movie ->
                    StatsContent(
                        genres = movie.genres.orEmpty(),
                        watchTimeMinutes = movie.runtime?.takeIf { it > 0 }?.toLong() ?: 0L,
                        releaseDate = movie.releaseDate,
                        credits = movie.credits.decodeCredits(),
                    )
                }

            ContentType.TV_SERIES ->
                tvSeriesDao.getTvSeriesById(contentId)?.let { series ->
                    StatsContent(
                        genres = series.genres.orEmpty(),
                        watchTimeMinutes = series.estimatedWatchTimeMinutes(),
                        releaseDate = series.firstAirDate,
                        credits = series.credits.decodeCredits(),
                    )
                }

            ContentType.ACTOR -> null
        }

    private fun com.movieapp.core.database.entity.TvSeriesEntity.estimatedWatchTimeMinutes(): Long {
        val averageEpisodeMinutes =
            episodeRunTime.filter { it > 0 }.takeIf { it.isNotEmpty() }?.average()?.roundToLong()
                ?: return 0L
        val episodes = numberOfEpisodes?.coerceAtLeast(1) ?: 1
        return averageEpisodeMinutes * episodes
    }

    private fun List<StatsContent>.toGenreBreakdown(): List<GenreCount> = flatMap {
        it.genres
    }
        .filter { it.name.isNotBlank() }
        .groupingBy { GenreKey(id = it.id, name = it.name) }
        .eachCount()
        .entries
        .sortedWith(
            compareByDescending<Map.Entry<GenreKey, Int>> { it.value }.thenBy { it.key.name }
        )
        .map { GenreCount(genreName = it.key.name, count = it.value) }

    private fun List<StatsContent>.toDecadeBreakdown(): List<DecadeCount> = mapNotNull {
        it.releaseDate.toDecade()
    }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedBy { it.key }
        .map { DecadeCount(decade = it.key, count = it.value) }

    private fun List<StatsContent>.toTopPeople(): List<PersonCount> = flatMap { content ->
        val credits = content.credits ?: return@flatMap emptyList()
        credits.crew
            .filter { it.job.equals(DIRECTOR_ROLE, ignoreCase = true) }
            .map {
                PersonHit(
                    id = it.id,
                    name = it.name,
                    role = DIRECTOR_ROLE,
                    profilePath = it.profilePath,
                )
            } +
            credits.cast.take(MAX_CAST_PEOPLE).map {
                PersonHit(
                    id = it.id,
                    name = it.name,
                    role = ACTOR_ROLE,
                    profilePath = it.profilePath,
                )
            }
    }
        .filter { it.id > 0 && it.name.isNotBlank() }
        .groupBy { PersonKey(id = it.id, role = it.role) }
        .map { (_, hits) ->
            val first = hits.first()
            PersonCount(
                id = first.id,
                name = first.name,
                role = first.role,
                profilePath = first.profilePath,
                count = hits.size,
            )
        }
        .sortedWith(
            compareByDescending<PersonCount> { it.count }
                .thenBy { if (it.role == DIRECTOR_ROLE) 0 else 1 }
                .thenBy { it.name }
        )
        .take(MAX_TOP_PEOPLE)

    private fun List<UserRatingEntity>.toRatingHistogram(): Map<Int, Int> =
        asSequence()
            .filter { it.contentType.toStatsContentType() != null }
            .map { (it.rating / 2.0).roundToInt().coerceIn(MIN_STARS, MAX_STARS) }
            .groupingBy { it }
            .eachCount()
            .entries.sortedBy { it.key }.associate { it.key to it.value }

    private fun FavoriteItemEntity.toContentKey(): ContentKey? =
        contentType.toStatsContentType()?.let { ContentKey(contentId = contentId, type = it) }

    private fun UserRatingEntity.toContentKey(): ContentKey? =
        contentType.toStatsContentType()?.let { ContentKey(contentId = contentId, type = it) }

    private fun com.movieapp.core.database.entity.CollectionItemEntity.toContentKey(): ContentKey? =
        contentType.toStatsContentType()?.let { ContentKey(contentId = contentId, type = it) }

    private fun String?.toStatsContentType(): ContentType? =
        when (this) {
            ContentType.MOVIE.name,
            "movie" -> ContentType.MOVIE

            ContentType.TV_SERIES.name,
            "tv_series" -> ContentType.TV_SERIES

            else -> null
        }

    private fun String?.decodeCredits(): Credits? {
        if (isNullOrBlank()) return null
        return try {
            json.decodeFromString<Credits>(this)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.w(e) { "Unable to parse credits while building watch stats" }
            null
        }
    }

    private fun String?.toDecade(): String? {
        val year = this?.take(4)?.toIntOrNull() ?: return null
        val decade = (year / 10) * 10
        return "${decade}s"
    }

    private data class ContentKey(val contentId: Int, val type: ContentType)

    private data class StatsContent(
        val genres: List<Genre>,
        val watchTimeMinutes: Long,
        val releaseDate: String?,
        val credits: Credits?,
    )

    private data class GenreKey(val id: Int, val name: String)

    private data class PersonKey(val id: Int, val role: String)

    private data class PersonHit(
        val id: Int,
        val name: String,
        val role: String,
        val profilePath: String?,
    )

    private companion object {
        const val DIRECTOR_ROLE = "Director"
        const val ACTOR_ROLE = "Actor"
        const val MAX_CAST_PEOPLE = 10
        const val MAX_TOP_PEOPLE = 10
        const val MIN_STARS = 1
        const val MAX_STARS = 5
    }
}
