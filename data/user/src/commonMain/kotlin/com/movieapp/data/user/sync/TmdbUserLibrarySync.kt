package com.movieapp.data.user.sync

import com.movieapp.core.database.dao.ActorDao
import com.movieapp.core.database.dao.MovieDao
import com.movieapp.core.database.dao.TvSeriesDao
import com.movieapp.core.database.dao.UserDao
import com.movieapp.core.database.entity.ActorEntity
import com.movieapp.core.database.entity.FavoriteItemEntity
import com.movieapp.core.database.entity.MovieEntity
import com.movieapp.core.database.entity.TvSeriesEntity
import com.movieapp.core.database.entity.UserRatingEntity
import com.movieapp.core.database.entity.WatchlistItemEntity
import com.movieapp.core.model.Actor
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.Movie
import com.movieapp.core.model.TvSeries
import com.movieapp.core.network.AccountFavoriteBody
import com.movieapp.core.network.AccountWatchlistBody
import com.movieapp.core.network.MovieTvRatingBody
import com.movieapp.core.network.TmdbApiService
import com.movieapp.data.user.auth.AuthSessionInvalidator
import com.movieapp.data.user.auth.TmdbSessionStorage
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import io.ktor.client.plugins.ResponseException
import co.touchlab.kermit.Logger

/**
 * Pushes local changes to TMDB when a session exists, and pulls remote lists after login. Notes on
 * ratings stay local-only; TMDB sync uses score only.
 */
class TmdbUserLibrarySync(
    private val api: TmdbApiService,
    private val sessionStorage: TmdbSessionStorage,
    private val userDao: UserDao,
    private val movieDao: MovieDao,
    private val tvSeriesDao: TvSeriesDao,
    private val actorDao: ActorDao,
    private val clock: Clock,
    private val sessionInvalidator: Lazy<AuthSessionInvalidator>,
) {

    suspend fun pullAllAndMerge(accountId: Int, sessionId: String) {
        try {
            pullFavoritesMovies(accountId, sessionId)
            pullFavoritesTv(accountId, sessionId)
            pullWatchlistMovies(accountId, sessionId)
            pullWatchlistTv(accountId, sessionId)
            pullFavoritePeople(accountId, sessionId)
            pullRatedMovies(accountId, sessionId)
            pullRatedTv(accountId, sessionId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.withTag("TmdbUserLibrarySync").w(e) { "Initial TMDB library pull failed" }
        }
    }

    private suspend fun pullFavoritesMovies(accountId: Int, sessionId: String) {
        var page = 1
        while (true) {
            val res =
                guard401OrNull { api.getFavoriteMovies(accountId, sessionId, page = page) }
                    ?: return
            for (movie in res.results) {
                cacheMovie(movie)
                mergeFavorite(movie.id, ContentType.MOVIE)
            }
            if (page >= res.totalPages || res.results.isEmpty()) break
            page++
        }
    }

    private suspend fun pullFavoritesTv(accountId: Int, sessionId: String) {
        var page = 1
        while (true) {
            val res =
                guard401OrNull { api.getFavoriteTv(accountId, sessionId, page = page) } ?: return
            for (tv in res.results) {
                cacheTvSeries(tv)
                mergeFavorite(tv.id, ContentType.TV_SERIES)
            }
            if (page >= res.totalPages || res.results.isEmpty()) break
            page++
        }
    }

    private suspend fun pullWatchlistMovies(accountId: Int, sessionId: String) {
        var page = 1
        while (true) {
            val res =
                guard401OrNull { api.getWatchlistMovies(accountId, sessionId, page = page) }
                    ?: return
            for (movie in res.results) {
                cacheMovie(movie)
                mergeWatchlist(movie.id, ContentType.MOVIE)
            }
            if (page >= res.totalPages || res.results.isEmpty()) break
            page++
        }
    }

    private suspend fun pullWatchlistTv(accountId: Int, sessionId: String) {
        var page = 1
        while (true) {
            val res =
                guard401OrNull { api.getWatchlistTv(accountId, sessionId, page = page) } ?: return
            for (tv in res.results) {
                cacheTvSeries(tv)
                mergeWatchlist(tv.id, ContentType.TV_SERIES)
            }
            if (page >= res.totalPages || res.results.isEmpty()) break
            page++
        }
    }

    private suspend fun pullFavoritePeople(accountId: Int, sessionId: String) {
        var page = 1
        while (true) {
            val res =
                guard401OrNull { api.getFavoritePeople(accountId, sessionId, page = page) }
                    ?: return
            for (person in res.results) {
                cacheActor(person)
                mergeFavorite(person.id, ContentType.ACTOR)
            }
            if (page >= res.totalPages || res.results.isEmpty()) break
            page++
        }
    }

    private suspend fun pullRatedMovies(accountId: Int, sessionId: String) {
        var page = 1
        while (true) {
            val res =
                guard401OrNull { api.getRatedMovies(accountId, sessionId, page = page) } ?: return
            for (movie in res.results) {
                cacheMovie(movie)
                val r = movie.accountRating ?: continue
                mergeRating(movie.id, ContentType.MOVIE, r)
            }
            if (page >= res.totalPages || res.results.isEmpty()) break
            page++
        }
    }

    private suspend fun pullRatedTv(accountId: Int, sessionId: String) {
        var page = 1
        while (true) {
            val res = guard401OrNull { api.getRatedTv(accountId, sessionId, page = page) } ?: return
            for (tv in res.results) {
                cacheTvSeries(tv)
                val r = tv.accountRating ?: continue
                mergeRating(tv.id, ContentType.TV_SERIES, r)
            }
            if (page >= res.totalPages || res.results.isEmpty()) break
            page++
        }
    }

    private suspend fun cacheMovie(movie: Movie) {
        if (movieDao.getMovieById(movie.id) == null) {
            movieDao.insertMovie(movie.toLibraryEntity(clock.now().toEpochMilliseconds()))
        }
    }

    private suspend fun cacheTvSeries(tvSeries: TvSeries) {
        if (tvSeriesDao.getTvSeriesById(tvSeries.id) == null) {
            tvSeriesDao.insertTvSeries(tvSeries.toLibraryEntity(clock.now().toEpochMilliseconds()))
        }
    }

    private suspend fun cacheActor(actor: Actor) {
        if (actorDao.getActorById(actor.id) == null) {
            actorDao.insertActor(actor.toLibraryEntity(clock.now().toEpochMilliseconds()))
        }
    }

    private suspend fun mergeFavorite(contentId: Int, type: ContentType) {
        if (userDao.getFavoriteItemByContent(contentId, type.name) != null) return
        userDao.insertFavoriteItem(
            FavoriteItemEntity(
                id = Uuid.random().toString(),
                contentId = contentId,
                contentType = type.name,
                addedAt = clock.now().toEpochMilliseconds(),
            )
        )
    }

    private suspend fun mergeWatchlist(contentId: Int, type: ContentType) {
        if (userDao.getWatchlistItemByContent(contentId, type.name) == null) {
            userDao.insertWatchlistItem(
                WatchlistItemEntity(
                    id = Uuid.random().toString(),
                    contentId = contentId,
                    contentType = type.name,
                    addedAt = clock.now().toEpochMilliseconds(),
                    priority = 0,
                )
            )
        }
    }

    private suspend fun mergeRating(contentId: Int, type: ContentType, tmdbValue: Double) {
        val ratingInt = tmdbValue.roundToInt().coerceIn(1, 10)
        val existing = userDao.getUserRatingByContent(contentId, type.name)
        val now = clock.now().toEpochMilliseconds()
        if (existing != null) {
            userDao.updateUserRating(
                existing.copy(rating = ratingInt, ratedAt = now, notes = existing.notes)
            )
        } else {
            userDao.insertUserRating(
                UserRatingEntity(
                    id = Uuid.random().toString(),
                    contentId = contentId,
                    contentType = type.name,
                    rating = ratingInt,
                    ratedAt = now,
                    notes = null,
                )
            )
        }
    }

    suspend fun pushFavoriteState(contentId: Int, contentType: ContentType, favorite: Boolean) {
        val session = sessionStorage.awaitInitialized()
        val sessionId = session.sessionId ?: return
        val accountId = session.accountId ?: return
        val media =
            when (contentType) {
                ContentType.MOVIE -> "movie"
                ContentType.TV_SERIES -> "tv"
                ContentType.ACTOR -> "person"
            }
        guard401OrNull {
            api.postAccountFavorite(
                accountId,
                sessionId,
                AccountFavoriteBody(mediaType = media, mediaId = contentId, favorite = favorite),
            )
        }
    }

    suspend fun pushWatchlistState(contentId: Int, contentType: ContentType, inWatchlist: Boolean) {
        val session = sessionStorage.awaitInitialized()
        val sessionId = session.sessionId ?: return
        val accountId = session.accountId ?: return
        val media = if (contentType == ContentType.MOVIE) "movie" else "tv"
        guard401OrNull {
            api.postAccountWatchlist(
                accountId,
                sessionId,
                AccountWatchlistBody(
                    mediaType = media,
                    mediaId = contentId,
                    watchlist = inWatchlist,
                ),
            )
        }
    }

    /** [ratingOutOfTen] is 1–10 as stored locally. */
    suspend fun pushRatingState(contentId: Int, contentType: ContentType, ratingOutOfTen: Float?) {
        if (contentType == ContentType.ACTOR) return
        val sessionId = sessionStorage.awaitInitialized().sessionId ?: return
        guard401OrNull {
            when (contentType) {
                ContentType.MOVIE ->
                    if (ratingOutOfTen == null) {
                        api.deleteMovieRating(contentId, sessionId)
                    } else {
                        api.postMovieRating(
                            contentId,
                            sessionId,
                            MovieTvRatingBody(ratingOutOfTen.coerceIn(1f, 10f)),
                        )
                    }

                ContentType.TV_SERIES ->
                    if (ratingOutOfTen == null) {
                        api.deleteTvRating(contentId, sessionId)
                    } else {
                        api.postTvRating(
                            contentId,
                            sessionId,
                            MovieTvRatingBody(ratingOutOfTen.coerceIn(1f, 10f)),
                        )
                    }

                else -> Unit
            }
        }
    }

    private suspend fun <T> guard401OrNull(block: suspend () -> T): T? {
        return try {
            block()
        } catch (e: ResponseException) {
            if (e.response.status.value == 401) {
                sessionInvalidator.value.onSessionInvalidated()
                null
            } else {
                throw e
            }
        }
    }
}

private fun Movie.toLibraryEntity(cachedAt: Long) =
    MovieEntity(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        popularity = popularity,
        adult = adult,
        originalLanguage = originalLanguage,
        originalTitle = originalTitle,
        video = video,
        genreIds = genreIds,
        genres = genres,
        runtime = runtime,
        status = status,
        tagline = tagline,
        budget = budget,
        revenue = revenue,
        homepage = homepage,
        imdbId = imdbId,
        productionCompanies = null,
        productionCountries = null,
        spokenLanguages = null,
        belongsToCollection = null,
        videos = videos,
        images = null,
        credits = null,
        similar = null,
        recommendations = null,
        reviews = null,
        keywords = null,
        externalIds = null,
        watchProviders = null,
        cachedAt = cachedAt,
    )

private fun TvSeries.toLibraryEntity(cachedAt: Long) =
    TvSeriesEntity(
        id = id,
        name = name,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        firstAirDate = firstAirDate,
        lastAirDate = lastAirDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        popularity = popularity,
        adult = adult,
        originalLanguage = originalLanguage,
        originalName = originalName,
        originCountry = originCountry,
        genreIds = genreIds,
        genres = genres,
        episodeRunTime = episodeRunTime,
        numberOfEpisodes = numberOfEpisodes,
        numberOfSeasons = numberOfSeasons,
        status = status,
        tagline = tagline,
        type = type,
        homepage = homepage,
        inProduction = inProduction,
        languages = languages,
        networks = null,
        productionCompanies = null,
        productionCountries = null,
        spokenLanguages = null,
        seasons = null,
        videos = videos,
        images = null,
        credits = null,
        similar = null,
        recommendations = null,
        reviews = null,
        keywords = null,
        externalIds = null,
        watchProviders = null,
        createdBy = null,
        lastEpisodeToAir = null,
        nextEpisodeToAir = null,
        cachedAt = cachedAt,
    )

private fun Actor.toLibraryEntity(cachedAt: Long) =
    ActorEntity(
        id = id,
        name = name,
        biography = biography,
        birthday = birthday,
        deathday = deathday,
        placeOfBirth = placeOfBirth,
        profilePath = profilePath,
        adult = adult,
        gender = gender,
        knownForDepartment = knownForDepartment,
        originalName = originalName,
        popularity = popularity,
        alsoKnownAs = alsoKnownAs,
        images = null,
        movieCredits = null,
        tvCredits = null,
        externalIds = null,
        taggedImages = null,
        cachedAt = cachedAt,
    )
