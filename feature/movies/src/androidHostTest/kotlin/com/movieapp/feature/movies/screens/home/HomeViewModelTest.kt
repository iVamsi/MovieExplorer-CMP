package com.movieapp.feature.movies.screens.home

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.testing.FakeData
import com.movieapp.feature.movies.screens.browseall.ContentCategory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val movieRepository = mockk<MovieRepository>()
    private val seriesRepository = mockk<SeriesRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { movieRepository.getNowPlayingMoviesPaged() } returns
            flowOf(PagingData.from(FakeData.fakeMovies))
        every { movieRepository.getPopularMoviesPaged() } returns
            flowOf(PagingData.from(FakeData.fakeMovies))
        every { movieRepository.getTopRatedMoviesPaged() } returns
            flowOf(PagingData.from(FakeData.fakeMovies))
        every { seriesRepository.getTrendingSeries() } returns
            flowOf(PagingData.from(FakeData.fakeTvSeries))
        every { seriesRepository.getPopularSeries() } returns
            flowOf(PagingData.from(FakeData.fakeTvSeries))
        every { seriesRepository.getTopRatedSeries() } returns
            flowOf(PagingData.from(FakeData.fakeTvSeries))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        HomeViewModel(movieRepository, seriesRepository, SavedStateHandle())

    @Test
    fun initialstateisLoadingisfalseandnoerror() = runTest {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun categoryselectedupdatesstateandremainsselected() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(HomeUiEvent.CategorySelected(HomeCategory.TV))

        assertThat(viewModel.uiState.value.category).isEqualTo(HomeCategory.TV)
    }

    @Test
    fun onEventMovieClickedsendsNavigateToMovieDetailsideeffect() = runTest {
        val viewModel = createViewModel()
        val testMovie = FakeData.fakeMovies.first()

        viewModel.sideEffect.test {
            viewModel.onEvent(HomeUiEvent.MovieClicked(testMovie.id))

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(HomeSideEffect.NavigateToMovieDetail::class.java)
            assertThat((effect as HomeSideEffect.NavigateToMovieDetail).movieId)
                .isEqualTo(testMovie.id)
        }
    }

    @Test
    fun onEventSeriesClickedsendsNavigateToSeriesDetailsideeffect() = runTest {
        val viewModel = createViewModel()
        val testSeries = FakeData.fakeTvSeries.first()

        viewModel.sideEffect.test {
            viewModel.onEvent(HomeUiEvent.SeriesClicked(testSeries.id))

            val effect = awaitItem()
            assertThat(effect).isInstanceOf(HomeSideEffect.NavigateToSeriesDetail::class.java)
            assertThat((effect as HomeSideEffect.NavigateToSeriesDetail).seriesId)
                .isEqualTo(testSeries.id)
        }
    }

    @Test
    fun onEventRefreshdoesnotemitasideeffect() = runTest {
        val viewModel = createViewModel()

        viewModel.sideEffect.test {
            viewModel.onEvent(HomeUiEvent.Refresh)
            expectNoEvents()
        }
    }

    @Test
    fun moviepagingflowsarenotnull() = runTest {
        val viewModel = createViewModel()
        assertThat(viewModel.nowPlayingMovies).isNotNull()
        assertThat(viewModel.popularMovies).isNotNull()
        assertThat(viewModel.topRatedMovies).isNotNull()
    }

    @Test
    fun seriespagingflowsarenotnull() = runTest {
        val viewModel = createViewModel()
        assertThat(viewModel.trendingSeries).isNotNull()
        assertThat(viewModel.popularSeries).isNotNull()
        assertThat(viewModel.topRatedSeries).isNotNull()
    }

    @Test
    fun onEventSeeAllClickedsendsNavigateToSeeAllsideeffectwithcorrectcategory() =
        runTest {
            val viewModel = createViewModel()

            viewModel.sideEffect.test {
                viewModel.onEvent(HomeUiEvent.SeeAllClicked(ContentCategory.POPULAR_MOVIES))

                val effect = awaitItem()
                assertThat(effect).isInstanceOf(HomeSideEffect.NavigateToSeeAll::class.java)
                assertThat((effect as HomeSideEffect.NavigateToSeeAll).category)
                    .isEqualTo(ContentCategory.POPULAR_MOVIES)
            }
        }
}
