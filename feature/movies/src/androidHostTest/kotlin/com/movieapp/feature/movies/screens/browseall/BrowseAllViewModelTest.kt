package com.movieapp.feature.movies.screens.browseall

import androidx.paging.PagingData
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.SeriesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BrowseAllViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val movieRepository = mockk<MovieRepository>(relaxed = true)
    private val seriesRepository = mockk<SeriesRepository>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { movieRepository.getPopularMoviesPaged() } returns flowOf(PagingData.empty())
        every { movieRepository.getNowPlayingMoviesPaged() } returns flowOf(PagingData.empty())
        every { movieRepository.getTopRatedMoviesPaged() } returns flowOf(PagingData.empty())
        every { seriesRepository.getTrendingSeries() } returns flowOf(PagingData.empty())
        every { seriesRepository.getPopularSeries() } returns flowOf(PagingData.empty())
        every { seriesRepository.getTopRatedSeries() } returns flowOf(PagingData.empty())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(category: ContentCategory) =
        BrowseAllViewModel(
            category = category.name,
            movieRepository = movieRepository,
            seriesRepository = seriesRepository,
        )

    @Test
    fun initseedsuiStatecategory() = runTest {
        val viewModel = createViewModel(ContentCategory.POPULAR_MOVIES)

        assertThat(viewModel.uiState.value.category).isEqualTo(ContentCategory.POPULAR_MOVIES)
        assertThat(viewModel.uiState.value.error).isNull()
    }

    @Test
    fun initinvalidcategorythrows() {
        val ex = runCatching {
            BrowseAllViewModel(
                category = "invalid",
                movieRepository = movieRepository,
                seriesRepository = seriesRepository,
            )
        }
            .exceptionOrNull()

        assertThat(ex).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun onEventMovieClickedemitsNavigateToMovieDetailsideeffect() = runTest {
        val viewModel = createViewModel(ContentCategory.POPULAR_MOVIES)

        viewModel.sideEffect.test {
            viewModel.onEvent(BrowseAllUiEvent.MovieClicked(movieId = 550))

            assertThat(awaitItem())
                .isEqualTo(BrowseAllSideEffect.NavigateToMovieDetail(movieId = 550))
        }
    }

    @Test
    fun onEventSeriesClickedemitsNavigateToSeriesDetailsideeffect() = runTest {
        val viewModel = createViewModel(ContentCategory.TRENDING_SERIES)

        viewModel.sideEffect.test {
            viewModel.onEvent(BrowseAllUiEvent.SeriesClicked(seriesId = 42))

            assertThat(awaitItem())
                .isEqualTo(BrowseAllSideEffect.NavigateToSeriesDetail(seriesId = 42))
        }
    }

    @Test
    fun onEventBackClickedemitsNavigateBacksideeffect() = runTest {
        val viewModel = createViewModel(ContentCategory.POPULAR_MOVIES)

        viewModel.sideEffect.test {
            viewModel.onEvent(BrowseAllUiEvent.BackClicked)

            assertThat(awaitItem()).isEqualTo(BrowseAllSideEffect.NavigateBack)
        }
    }
}
