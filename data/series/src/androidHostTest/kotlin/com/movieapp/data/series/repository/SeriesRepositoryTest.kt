package com.movieapp.data.series.repository

import com.movieapp.core.testing.FixedClock
import com.movieapp.core.testing.testDispatchers
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.database.TransactionRunner
import com.movieapp.core.database.dao.RemoteKeysDao
import com.movieapp.core.database.dao.TvSeriesDao
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.model.PagedResponse
import com.movieapp.core.model.TvSeries
import com.movieapp.core.network.TmdbApiService
import com.movieapp.data.series.mapper.TvSeriesMapper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.io.IOException
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SeriesRepositoryTest {

    private val apiService = mockk<TmdbApiService>()
    private val tvSeriesDao = mockk<TvSeriesDao>(relaxed = true)
    private val transactionRunner =
        object : TransactionRunner {
            override suspend fun <R> run(block: suspend () -> R): R = block()
        }
    private val remoteKeysDao = mockk<RemoteKeysDao>(relaxed = true)
    private val clock = FixedClock(Instant.fromEpochMilliseconds(1_700_000_000_000L))
    private val mapper = TvSeriesMapper(clock)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val preferencesRepository =
        mockk<PreferencesRepository>(relaxed = true).also {
            every { it.tmdbLanguage } returns flowOf("en-US")
            every { it.watchRegion } returns flowOf("US")
        }

    private lateinit var repository: SeriesRepository

    @Before
    fun setup() {
        repository =
            SeriesRepositoryImpl(
                apiService = apiService,
                tvSeriesDao = tvSeriesDao,
                transactionRunner = transactionRunner,
                remoteKeysDao = remoteKeysDao,
                tvSeriesMapper = mapper,
                clock = clock,
                preferencesRepository = preferencesRepository,
                dispatchers = testDispatchers(testDispatcher),
            )
    }

    @Test
    fun `getTrendingTvSeriesPage - api success - emits Loading then Success and caches`() =
        runTest {
            val series = listOf(TvSeries(id = 1, name = "Breaking Bad"))
            coEvery {
                apiService.getTrendingTvSeries(timeWindow = any(), page = 1, language = "en-US")
            } returns PagedResponse(page = 1, results = series, totalPages = 1, totalResults = 1)

            repository.getTrendingTvSeriesPage().test {
                assertThat(awaitItem()).isEqualTo(Result.Loading)
                val success = awaitItem()
                assertThat(success).isInstanceOf(Result.Success::class.java)
                assertThat((success as Result.Success).data).isEqualTo(series)
                awaitComplete()
            }

            coVerify { tvSeriesDao.insertTvSeriesList(any()) }
        }

    @Test
    fun `getTrendingTvSeriesPage - api failure with cache - emits cached Success`() = runTest {
        val cached = TvSeries(id = 7, name = "Cached Show")
        coEvery {
            apiService.getTrendingTvSeries(timeWindow = any(), page = 1, language = "en-US")
        } throws IOException("offline")
        every { tvSeriesDao.getCachedTvSeries(any()) } returns
            flowOf(listOf(mapper.mapToEntity(cached)))

        repository.getTrendingTvSeriesPage().test {
            assertThat(awaitItem()).isEqualTo(Result.Loading)
            val success = awaitItem()
            assertThat(success).isInstanceOf(Result.Success::class.java)
            assertThat((success as Result.Success).data.single().id).isEqualTo(7)
            awaitComplete()
        }
    }

    @Test
    fun `getTrendingTvSeriesPage - api failure with empty cache - emits Error`() = runTest {
        val failure = IOException("offline")
        coEvery {
            apiService.getTrendingTvSeries(timeWindow = any(), page = 1, language = "en-US")
        } throws failure
        every { tvSeriesDao.getCachedTvSeries(any()) } returns flowOf(emptyList())

        repository.getTrendingTvSeriesPage().test {
            assertThat(awaitItem()).isEqualTo(Result.Loading)
            val error = awaitItem()
            assertThat(error).isInstanceOf(Result.Error::class.java)
            assertThat((error as Result.Error).exception).isSameInstanceAs(failure)
            awaitComplete()
        }
    }

    @Test
    fun `getSeriesById - api success - inserts entity and returns Success`() = runTest {
        val series = TvSeries(id = 1396, name = "Breaking Bad")
        coEvery {
            apiService.getTvSeriesDetails(1396, language = "en-US", appendToResponse = "videos")
        } returns series

        val result = repository.getSeriesById(1396)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data).isEqualTo(series)
        coVerify { tvSeriesDao.insertTvSeries(any()) }
    }

    @Test
    fun `getSeriesById - api failure with cache - returns cached Success`() = runTest {
        val cached = TvSeries(id = 1396, name = "Cached")
        coEvery {
            apiService.getTvSeriesDetails(1396, language = "en-US", appendToResponse = "videos")
        } throws IOException("offline")
        coEvery { tvSeriesDao.getTvSeriesById(1396) } returns mapper.mapToEntity(cached)

        val result = repository.getSeriesById(1396)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data.name).isEqualTo("Cached")
    }

    @Test
    fun `getSeriesById - api failure with no cache - returns Error`() = runTest {
        val failure = IOException("offline")
        coEvery {
            apiService.getTvSeriesDetails(42, language = "en-US", appendToResponse = "videos")
        } throws failure
        coEvery { tvSeriesDao.getTvSeriesById(42) } returns null

        val result = repository.getSeriesById(42)

        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).exception).isSameInstanceAs(failure)
    }

    @Test
    fun `searchSeriesResults - api success - emits Loading then Success`() = runTest {
        val series = listOf(TvSeries(id = 5, name = "Search Hit"))
        coEvery {
            apiService.searchTvSeries(query = "breaking", page = 1, language = "en-US")
        } returns PagedResponse(page = 1, results = series, totalPages = 1, totalResults = 1)

        repository.searchSeriesResults("breaking").test {
            assertThat(awaitItem()).isEqualTo(Result.Loading)
            val success = awaitItem()
            assertThat(success).isInstanceOf(Result.Success::class.java)
            assertThat((success as Result.Success).data).isEqualTo(series)
            awaitComplete()
        }
    }

    @Test
    fun `searchSeriesResults - api failure with cached results - emits cached Success`() = runTest {
        val cached = TvSeries(id = 6, name = "Cached Series")
        coEvery {
            apiService.searchTvSeries(query = "breaking", page = 1, language = "en-US")
        } throws IOException("offline")
        every { tvSeriesDao.searchTvSeries("breaking") } returns
            flowOf(listOf(mapper.mapToEntity(cached)))

        repository.searchSeriesResults("breaking").test {
            assertThat(awaitItem()).isEqualTo(Result.Loading)
            val success = awaitItem()
            assertThat(success).isInstanceOf(Result.Success::class.java)
            assertThat((success as Result.Success).data.single().name).isEqualTo("Cached Series")
            awaitComplete()
        }
    }

    @Test
    fun `searchSeriesResults - api failure with empty cache - emits Error`() = runTest {
        val failure = IOException("offline")
        coEvery {
            apiService.searchTvSeries(query = "breaking", page = 1, language = "en-US")
        } throws failure
        every { tvSeriesDao.searchTvSeries("breaking") } returns flowOf(emptyList())

        repository.searchSeriesResults("breaking").test {
            assertThat(awaitItem()).isEqualTo(Result.Loading)
            val error = awaitItem()
            assertThat(error).isInstanceOf(Result.Error::class.java)
            assertThat((error as Result.Error).exception).isSameInstanceAs(failure)
            awaitComplete()
        }
    }

    @Test
    fun `getSeriesByIdFlow - entity in dao - emits Loading then Success without api call`() =
        runTest {
            val series = TvSeries(id = 77, name = "Cached Flow Series")
            every { tvSeriesDao.getTvSeriesByIdFlow(77) } returns flowOf(mapper.mapToEntity(series))

            repository.getSeriesByIdFlow(77).test {
                assertThat(awaitItem()).isEqualTo(Result.Loading)
                val success = awaitItem()
                assertThat(success).isInstanceOf(Result.Success::class.java)
                assertThat((success as Result.Success).data.name).isEqualTo("Cached Flow Series")
                awaitComplete()
            }
        }

    @Test
    fun `getSeriesByIdFlow - no entity in dao and api succeeds - emits Loading then Success`() =
        runTest {
            val series = TvSeries(id = 88, name = "Fresh Series")
            every { tvSeriesDao.getTvSeriesByIdFlow(88) } returns flowOf(null)
            coEvery {
                apiService.getTvSeriesDetails(88, language = "en-US", appendToResponse = "videos")
            } returns series

            repository.getSeriesByIdFlow(88).test {
                assertThat(awaitItem()).isEqualTo(Result.Loading)
                val success = awaitItem()
                assertThat(success).isInstanceOf(Result.Success::class.java)
                assertThat((success as Result.Success).data.name).isEqualTo("Fresh Series")
                awaitComplete()
            }
        }

    @Test
    fun `discoverSeries - watch providers selected - sends pipe separated providers and watch region`() =
        runTest {
            val series = TvSeries(id = 72, name = "Provider Series")
            coEvery {
                apiService.discoverTvSeries(
                    firstAirDateYear = 2025,
                    voteAverageGte = 7.0,
                    withGenres = "18",
                    withWatchProviders = "8|350",
                    watchRegion = "US",
                    language = "en-US",
                )
            } returns
                PagedResponse(page = 1, results = listOf(series), totalPages = 1, totalResults = 1)

            repository
                .discoverSeries(
                    year = 2025,
                    ratingGte = 7.0,
                    genreIds = listOf(18),
                    watchProviderIds = listOf(8, 350),
                    watchRegion = "US",
                )
                .test {
                    assertThat(awaitItem()).isEqualTo(Result.Loading)
                    val success = awaitItem()
                    assertThat(success).isInstanceOf(Result.Success::class.java)
                    assertThat((success as Result.Success).data.map { it.id }).containsExactly(72)
                    awaitComplete()
                }
        }
}
