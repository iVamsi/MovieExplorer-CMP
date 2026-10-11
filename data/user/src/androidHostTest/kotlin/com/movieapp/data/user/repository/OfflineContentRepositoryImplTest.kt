package com.movieapp.data.user.repository

import com.movieapp.core.testing.FixedClock
import com.movieapp.core.testing.testDispatchers
import android.content.Context
import app.cash.turbine.test
import coil3.ImageLoader
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.database.dao.MovieDao
import com.movieapp.core.database.dao.OfflineContentDao
import com.movieapp.core.database.dao.TvSeriesDao
import com.movieapp.core.database.entity.OfflineContentEntity
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.model.ContentType
import com.movieapp.core.testing.FakeData
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineContentRepositoryImplTest {

    private val offlineContentDao = mockk<OfflineContentDao>()
    private val movieDao = mockk<MovieDao>(relaxed = true)
    private val tvSeriesDao = mockk<TvSeriesDao>(relaxed = true)
    private val movieRepository = mockk<MovieRepository>()
    private val seriesRepository = mockk<SeriesRepository>()
    private val imageLoader = mockk<ImageLoader>()
    private val context =
        mockk<Context>(relaxed = true).also { every { it.applicationContext } returns it }
    private val clock = FixedClock(Instant.fromEpochMilliseconds(FIXED_TIME))

    @Test
    fun `prefetchContent - for movie - fetches details caches images and pins offline row`() =
        runTest {
            val repository =
                OfflineContentRepositoryImpl(
                    offlineContentDao = offlineContentDao,
                    movieDao = movieDao,
                    tvSeriesDao = tvSeriesDao,
                    movieRepository = movieRepository,
                    seriesRepository = seriesRepository,
                    imageLoader = imageLoader,
                    platformContext = context,
                    clock = clock,
                    dispatchers = testDispatchers(UnconfinedTestDispatcher(testScheduler)),
                )
            val entitySlot = slot<OfflineContentEntity>()
            every { movieRepository.getMovieDetails(1) } returns
                flowOf(Result.Success(FakeData.fakeMovies.first()))
            coJustRun { offlineContentDao.upsertOfflineContent(capture(entitySlot)) }

            repository.prefetchContent(1, ContentType.MOVIE).test {
                assertThat(awaitItem()).isEqualTo(Result.Loading)

                val item = awaitItem()
                assertThat(item).isInstanceOf(Result.Success::class.java)
                val offlineContent = (item as Result.Success).data
                assertThat(offlineContent.contentId).isEqualTo(1)
                assertThat(offlineContent.contentType).isEqualTo(ContentType.MOVIE)
                assertThat(offlineContent.prefetchedAt).isEqualTo(FIXED_TIME)
                assertThat(entitySlot.captured.contentId).isEqualTo(1)
                assertThat(entitySlot.captured.contentType).isEqualTo(ContentType.MOVIE.name)
                awaitComplete()
            }
        }

    private companion object {
        const val FIXED_TIME = 1_700_000_000_000L
    }
}
