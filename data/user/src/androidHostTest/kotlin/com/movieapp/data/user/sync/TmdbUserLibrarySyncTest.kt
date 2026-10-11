package com.movieapp.data.user.sync

import com.movieapp.core.database.dao.ActorDao
import com.movieapp.core.database.dao.MovieDao
import com.movieapp.core.database.dao.TvSeriesDao
import com.movieapp.core.database.dao.UserDao
import com.movieapp.core.database.entity.MovieEntity
import com.movieapp.core.model.Actor
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.Movie
import com.movieapp.core.model.PagedResponse
import com.movieapp.core.model.TvSeries
import com.movieapp.core.network.TmdbApiService
import com.movieapp.core.testing.FixedClock
import com.movieapp.data.user.auth.AuthSessionInvalidator
import com.movieapp.data.user.auth.TmdbSessionStorage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Test

@Suppress("FunctionName")
class TmdbUserLibrarySyncTest {

    private val api = mockk<TmdbApiService>()
    private val sessionStorage = mockk<TmdbSessionStorage>(relaxed = true)
    private val userDao = mockk<UserDao>(relaxed = true)
    private val movieDao = mockk<MovieDao>(relaxed = true)
    private val tvSeriesDao = mockk<TvSeriesDao>(relaxed = true)
    private val actorDao = mockk<ActorDao>(relaxed = true)
    private val sessionInvalidator = lazy { mockk<AuthSessionInvalidator>(relaxed = true) }
    private val clock = FixedClock(Instant.fromEpochMilliseconds(CACHED_AT))

    @Test
    fun `pullAllAndMerge - fresh database - caches metadata with library links`() = runTest {
        val movie = Movie(id = 1, title = "Movie", posterPath = "/movie.jpg")
        val tvSeries = TvSeries(id = 2, name = "Series", posterPath = "/series.jpg")
        val actor = Actor(id = 3, name = "Actor", profilePath = "/actor.jpg")
        stubRemoteLists(
            favoriteMovies = listOf(movie),
            favoriteTv = listOf(tvSeries),
            favoritePeople = listOf(actor),
        )
        coEvery { movieDao.getMovieById(movie.id) } returns null
        coEvery { tvSeriesDao.getTvSeriesById(tvSeries.id) } returns null
        coEvery { actorDao.getActorById(actor.id) } returns null
        val sync = createSync()

        sync.pullAllAndMerge(ACCOUNT_ID, SESSION_ID)

        coVerify {
            movieDao.insertMovie(
                match {
                    it.id == movie.id &&
                        it.title == movie.title &&
                        it.posterPath == movie.posterPath &&
                        it.cachedAt == CACHED_AT
                }
            )
            tvSeriesDao.insertTvSeries(
                match {
                    it.id == tvSeries.id &&
                        it.name == tvSeries.name &&
                        it.posterPath == tvSeries.posterPath &&
                        it.cachedAt == CACHED_AT
                }
            )
            actorDao.insertActor(
                match {
                    it.id == actor.id &&
                        it.name == actor.name &&
                        it.profilePath == actor.profilePath &&
                        it.cachedAt == CACHED_AT
                }
            )
            userDao.insertFavoriteItem(
                match { it.contentId == movie.id && it.contentType == ContentType.MOVIE.name }
            )
            userDao.insertFavoriteItem(
                match { it.contentId == tvSeries.id && it.contentType == ContentType.TV_SERIES.name }
            )
            userDao.insertFavoriteItem(
                match { it.contentId == actor.id && it.contentType == ContentType.ACTOR.name }
            )
        }
    }

    @Test
    fun `pullAllAndMerge - metadata already cached - preserves richer row`() = runTest {
        val movie = Movie(id = 1, title = "List title")
        val cached = mockk<MovieEntity>()
        stubRemoteLists(favoriteMovies = listOf(movie))
        coEvery { movieDao.getMovieById(movie.id) } returns cached
        val sync = createSync()

        sync.pullAllAndMerge(ACCOUNT_ID, SESSION_ID)

        coVerify(exactly = 0) { movieDao.insertMovie(any()) }
    }

    private fun createSync() =
        TmdbUserLibrarySync(
            api = api,
            sessionStorage = sessionStorage,
            userDao = userDao,
            movieDao = movieDao,
            tvSeriesDao = tvSeriesDao,
            actorDao = actorDao,
            clock = clock,
            sessionInvalidator = sessionInvalidator,
        )

    private fun stubRemoteLists(
        favoriteMovies: List<Movie> = emptyList(),
        favoriteTv: List<TvSeries> = emptyList(),
        favoritePeople: List<Actor> = emptyList(),
    ) {
        coEvery { userDao.getFavoriteItemByContent(any(), any()) } returns null
        coEvery { api.getFavoriteMovies(ACCOUNT_ID, SESSION_ID, any(), any()) } returns
            page(favoriteMovies)
        coEvery { api.getFavoriteTv(ACCOUNT_ID, SESSION_ID, any(), any()) } returns page(favoriteTv)
        coEvery { api.getWatchlistMovies(ACCOUNT_ID, SESSION_ID, any(), any()) } returns
            page(emptyList<Movie>())
        coEvery { api.getWatchlistTv(ACCOUNT_ID, SESSION_ID, any(), any()) } returns
            page(emptyList<TvSeries>())
        coEvery { api.getFavoritePeople(ACCOUNT_ID, SESSION_ID, any(), any()) } returns
            page(favoritePeople)
        coEvery { api.getRatedMovies(ACCOUNT_ID, SESSION_ID, any(), any()) } returns
            page(emptyList<Movie>())
        coEvery { api.getRatedTv(ACCOUNT_ID, SESSION_ID, any(), any()) } returns
            page(emptyList<TvSeries>())
    }

    private fun <T> page(items: List<T>) =
        PagedResponse(page = 1, results = items, totalPages = 1, totalResults = items.size)

    private companion object {
        const val ACCOUNT_ID = 42
        const val SESSION_ID = "session"
        const val CACHED_AT = 1_700_000_000_000L
    }
}
