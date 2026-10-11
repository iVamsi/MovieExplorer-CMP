package com.movieapp.data.movies.repository

import com.movieapp.core.testing.FixedClock
import com.movieapp.core.testing.testDispatchers
import app.cash.turbine.test
import com.movieapp.core.common.Result
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.database.TransactionRunner
import com.movieapp.core.database.dao.RemoteKeysDao
import com.movieapp.core.database.dao.MovieDao
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.model.Movie
import com.movieapp.core.model.PagedResponse
import com.movieapp.core.network.TmdbApiService
import com.movieapp.data.movies.mapper.MovieMapper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.io.IOException
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MovieRepositoryTest {

    private val apiService = mockk<TmdbApiService>()
    private val movieDao = mockk<MovieDao>(relaxed = true)
    private val transactionRunner =
        object : TransactionRunner {
            override suspend fun <R> run(block: suspend () -> R): R = block()
        }
    private val remoteKeysDao = mockk<RemoteKeysDao>(relaxed = true)
    private val clock = FixedClock(Instant.parse("2024-01-01T00:00:00Z"))
    private val mapper = MovieMapper(clock)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val preferencesRepository =
        mockk<PreferencesRepository>(relaxed = true).also {
            every { it.tmdbLanguage } returns flowOf("en-US")
            every { it.watchRegion } returns flowOf("US")
        }

    private lateinit var repository: MovieRepository

    @Before
    fun setup() {
        repository =
            MovieRepositoryImpl(
                apiService = apiService,
                movieDao = movieDao,
                transactionRunner = transactionRunner,
                remoteKeysDao = remoteKeysDao,
                mapper = mapper,
                clock = clock,
                preferencesRepository = preferencesRepository,
                dispatchers = testDispatchers(testDispatcher),
            )
    }

    @Test
    fun `getPopularMovies emits success from API and saves to DB`() = runTest {
        // Given
        val movie =
            Movie(
                id = 1,
                title = "Test Movie",
                overview = "Overview",
                posterPath = null,
                backdropPath = null,
                releaseDate = "2023-01-01",
                voteAverage = 8.0,
                voteCount = 100,
                popularity = 100.0,
                adult = false,
                originalLanguage = "en",
                originalTitle = "Test Movie",
                video = false,
                genreIds = emptyList(),
                genres = emptyList(), // added empty list for non-nullable list field
                runtime = null,
                status = null,
                tagline = null,
                budget = null,
                revenue = null,
                homepage = null,
                imdbId = null,
                productionCompanies = emptyList(),
                productionCountries = emptyList(),
                spokenLanguages = emptyList(),
                belongsToCollection = null,
                videos = null,
                images = null,
                credits = null,
                similar = null,
                recommendations = null,
                reviews = null,
                keywords = emptyList(),
                externalIds = null,
                watchProviders = null,
            )
        val movies = listOf(movie)
        val response = PagedResponse(page = 1, results = movies, totalPages = 1, totalResults = 1)

        coEvery { apiService.getPopularMovies(page = 1, language = "en-US", region = "US") } returns
            response
        coEvery { movieDao.insertMovies(any()) } returns Unit

        // When & Then
        repository.getPopularMovies(1).test {
            // First emission is Loading
            assertEquals(Result.Loading, awaitItem())

            // Second emission is Success
            val result = awaitItem()
            assertTrue(result is Result.Success)
            val successResult = result as Result.Success
            assertEquals(1, successResult.data.size)
            assertEquals("Test Movie", successResult.data[0].title)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify { movieDao.insertMovies(any()) }
    }

    @Test
    fun `getWatchlistMovies emits loading then mapped success from dao flow`() = runTest {
        val movie = testMovie(id = 7, title = "Saved Movie")
        every { movieDao.getWatchlistMovies() } returns flowOf(listOf(mapper.mapToEntity(movie)))

        repository.getWatchlistMovies().test {
            assertEquals(Result.Loading, awaitItem())

            val result = awaitItem()
            assertTrue(result is Result.Success)
            val success = result as Result.Success
            assertEquals(listOf("Saved Movie"), success.data.map { it.title })

            awaitComplete()
        }
    }

    @Test
    fun `getFavoriteMovies emits error when dao flow fails`() = runTest {
        val failure = IllegalStateException("database unavailable")
        every { movieDao.getFavoriteMovies() } returns flow { throw failure }

        repository.getFavoriteMovies().test {
            assertEquals(Result.Loading, awaitItem())

            val result = awaitItem()
            assertTrue(result is Result.Error)
            assertEquals(failure, (result as Result.Error).exception)

            awaitComplete()
        }
    }

    @Test
    fun `getMoviesInCollection delegates collection id and maps dao rows`() = runTest {
        val movie = testMovie(id = 11, title = "Collection Movie")
        every { movieDao.getMoviesInCollection("collection-1") } returns
            flowOf(listOf(mapper.mapToEntity(movie)))

        repository.getMoviesInCollection("collection-1").test {
            assertEquals(Result.Loading, awaitItem())

            val result = awaitItem()
            assertTrue(result is Result.Success)
            val success = result as Result.Success
            assertEquals(11, success.data.single().id)

            awaitComplete()
        }
    }

    @Test
    fun `discoverMovies - watch providers selected - sends pipe separated providers and watch region`() =
        runTest {
            val movie = testMovie(id = 19, title = "Provider Movie")
            coEvery {
                apiService.discoverMovies(
                    year = 2025,
                    voteAverageGte = 7.0,
                    withGenres = "28",
                    withWatchProviders = "8|337",
                    watchRegion = "US",
                    language = "en-US",
                )
            } returns
                PagedResponse(page = 1, results = listOf(movie), totalPages = 1, totalResults = 1)

            repository
                .discoverMovies(
                    year = 2025,
                    ratingGte = 7.0,
                    genreIds = listOf(28),
                    watchProviderIds = listOf(8, 337),
                    watchRegion = "US",
                )
                .test {
                    assertEquals(Result.Loading, awaitItem())
                    val result = awaitItem()
                    assertTrue(result is Result.Success)
                    assertEquals(listOf(19), (result as Result.Success).data.map { it.id })
                    awaitComplete()
                }
        }

    @Test
    fun `getPopularMovies - api failure with cached movies - emits cached Success`() = runTest {
        val cached = testMovie(id = 5, title = "Cached Movie")
        coEvery {
            apiService.getPopularMovies(page = 1, language = "en-US", region = "US")
        } throws IOException("offline")
        every { movieDao.getCachedMovies(any()) } returns flowOf(listOf(mapper.mapToEntity(cached)))

        repository.getPopularMovies(1).test {
            assertEquals(Result.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(listOf("Cached Movie"), (result as Result.Success).data.map { it.title })
            awaitComplete()
        }
    }

    @Test
    fun `getPopularMovies - api failure with empty cache - emits Error`() = runTest {
        val failure = IOException("offline")
        coEvery {
            apiService.getPopularMovies(page = 1, language = "en-US", region = "US")
        } throws failure
        every { movieDao.getCachedMovies(any()) } returns flowOf(emptyList())

        repository.getPopularMovies(1).test {
            assertEquals(Result.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is Result.Error)
            assertEquals(failure, (result as Result.Error).exception)
            awaitComplete()
        }
    }

    @Test
    fun `getTrendingMovies - api success - emits Loading then Success and caches`() = runTest {
        val movie = testMovie(id = 3, title = "Trending Movie")
        coEvery {
            apiService.getTrendingMovies(timeWindow = any(), language = "en-US")
        } returns PagedResponse(page = 1, results = listOf(movie), totalPages = 1, totalResults = 1)

        repository.getTrendingMovies().test {
            assertEquals(Result.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(listOf(3), (result as Result.Success).data.map { it.id })
            awaitComplete()
        }

        coVerify { movieDao.insertMovies(any()) }
    }

    @Test
    fun `getTrendingMovies - api failure with cached movies - emits cached Success`() = runTest {
        val cached = testMovie(id = 9, title = "Cached Trending")
        coEvery {
            apiService.getTrendingMovies(timeWindow = any(), language = "en-US")
        } throws IOException("offline")
        every { movieDao.getCachedMovies(any()) } returns flowOf(listOf(mapper.mapToEntity(cached)))

        repository.getTrendingMovies().test {
            assertEquals(Result.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(listOf(9), (result as Result.Success).data.map { it.id })
            awaitComplete()
        }
    }

    @Test
    fun `getTrendingMovies - api failure with empty cache - emits Error`() = runTest {
        val failure = IOException("offline")
        coEvery {
            apiService.getTrendingMovies(timeWindow = any(), language = "en-US")
        } throws failure
        every { movieDao.getCachedMovies(any()) } returns flowOf(emptyList())

        repository.getTrendingMovies().test {
            assertEquals(Result.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is Result.Error)
            assertEquals(failure, (result as Result.Error).exception)
            awaitComplete()
        }
    }

    @Test
    fun `getMovieDetails - api success - emits Loading then Success and caches`() = runTest {
        val movie = testMovie(id = 42, title = "Detail Movie")
        coEvery {
            apiService.getMovieDetails(42, language = "en-US", appendToResponse = "videos")
        } returns movie

        repository.getMovieDetails(42).test {
            assertEquals(Result.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(42, (result as Result.Success).data.id)
            awaitComplete()
        }

        coVerify { movieDao.insertMovie(any()) }
    }

    @Test
    fun `getMovieDetails - api failure with cached movie - emits cached Success`() = runTest {
        val cached = testMovie(id = 42, title = "Cached Detail")
        coEvery {
            apiService.getMovieDetails(42, language = "en-US", appendToResponse = "videos")
        } throws IOException("offline")
        coEvery { movieDao.getMovieById(42) } returns mapper.mapToEntity(cached)

        repository.getMovieDetails(42).test {
            assertEquals(Result.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals("Cached Detail", (result as Result.Success).data.title)
            awaitComplete()
        }
    }

    @Test
    fun `getMovieDetails - api failure with no cache - emits Error`() = runTest {
        val failure = IOException("offline")
        coEvery {
            apiService.getMovieDetails(99, language = "en-US", appendToResponse = "videos")
        } throws failure
        coEvery { movieDao.getMovieById(99) } returns null

        repository.getMovieDetails(99).test {
            assertEquals(Result.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is Result.Error)
            assertEquals(failure, (result as Result.Error).exception)
            awaitComplete()
        }
    }

    @Test
    fun `searchMovies - api success - emits Loading then Success and caches`() = runTest {
        val movie = testMovie(id = 7, title = "Search Hit")
        coEvery {
            apiService.searchMovies(query = "batman", language = "en-US", region = "US")
        } returns PagedResponse(page = 1, results = listOf(movie), totalPages = 1, totalResults = 1)

        repository.searchMovies("batman").test {
            assertEquals(Result.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(listOf("Search Hit"), (result as Result.Success).data.map { it.title })
            awaitComplete()
        }

        coVerify { movieDao.insertMovies(any()) }
    }

    @Test
    fun `searchMovies - api failure with cached results - emits cached Success`() = runTest {
        val cached = testMovie(id = 8, title = "Cached Search")
        coEvery {
            apiService.searchMovies(query = "batman", language = "en-US", region = "US")
        } throws IOException("offline")
        every { movieDao.searchMovies("batman") } returns flowOf(listOf(mapper.mapToEntity(cached)))

        repository.searchMovies("batman").test {
            assertEquals(Result.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(listOf(8), (result as Result.Success).data.map { it.id })
            awaitComplete()
        }
    }

    @Test
    fun `searchMovies - api failure with empty cache - emits Error`() = runTest {
        val failure = IOException("offline")
        coEvery {
            apiService.searchMovies(query = "batman", language = "en-US", region = "US")
        } throws failure
        every { movieDao.searchMovies("batman") } returns flowOf(emptyList())

        repository.searchMovies("batman").test {
            assertEquals(Result.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is Result.Error)
            assertEquals(failure, (result as Result.Error).exception)
            awaitComplete()
        }
    }

    @Test
    fun `getMoviesByGenre - api failure with cached genre movies - emits cached Success`() =
        runTest {
            val cached = testMovie(id = 12, title = "Genre Movie")
            coEvery {
                apiService.discoverMovies(withGenres = "28", language = "en-US")
            } throws IOException("offline")
            every { movieDao.getMoviesByGenre(28) } returns
                flowOf(listOf(mapper.mapToEntity(cached)))

            repository.getMoviesByGenre(28).test {
                assertEquals(Result.Loading, awaitItem())
                val result = awaitItem()
                assertTrue(result is Result.Success)
                assertEquals(listOf(12), (result as Result.Success).data.map { it.id })
                awaitComplete()
            }
        }

    @Test
    fun `refreshMovies - request fails - keeps cached movies`() = runTest {
        coEvery {
            apiService.getPopularMovies(page = 1, language = "en-US", region = "US")
        } throws IOException("offline")

        repository.refreshMovies()

        coVerify(exactly = 0) { movieDao.deleteOldMovies(any()) }
    }

    private fun testMovie(id: Int, title: String): Movie =
        Movie(
            id = id,
            title = title,
            overview = "Overview",
            posterPath = null,
            backdropPath = null,
            releaseDate = "2023-01-01",
            voteAverage = 8.0,
            voteCount = 100,
            popularity = 100.0,
            adult = false,
            originalLanguage = "en",
            originalTitle = title,
            video = false,
            genreIds = emptyList(),
            genres = emptyList(),
            runtime = null,
            status = null,
            tagline = null,
            budget = null,
            revenue = null,
            homepage = null,
            imdbId = null,
            productionCompanies = emptyList(),
            productionCountries = emptyList(),
            spokenLanguages = emptyList(),
            belongsToCollection = null,
            videos = null,
            images = null,
            credits = null,
            similar = null,
            recommendations = null,
            reviews = null,
            keywords = emptyList(),
            externalIds = null,
            watchProviders = null,
        )
}
