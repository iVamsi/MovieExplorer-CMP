package com.movieapp.feature.movies.screens.watchlist

import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.testing.FakeData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class WatchlistViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val movieRepository = mockk<MovieRepository>()
    private val seriesRepository = mockk<SeriesRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = WatchlistViewModel(movieRepository, seriesRepository)

    @Test
    fun loadWatchlistwhenmoviessucceedstateshowswatchlistmovies() = runTest {
        every { movieRepository.getWatchlistMovies() } returns
            flowOf(Result.Success(FakeData.fakeMovies))
        every { seriesRepository.getWatchlistSeries() } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        val job = backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        val state = viewModel.uiState.value
        job.cancel()
        assertThat(state.watchlistMovies).isEqualTo(FakeData.fakeMovies)
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun loadWatchlistwhenseriessucceedstateshowswatchlistseries() = runTest {
        every { movieRepository.getWatchlistMovies() } returns flowOf(Result.Success(emptyList()))
        every { seriesRepository.getWatchlistSeries() } returns
            flowOf(Result.Success(FakeData.fakeTvSeries))

        val viewModel = createViewModel()
        val job = backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        val state = viewModel.uiState.value
        job.cancel()
        assertThat(state.watchlistTvSeries).isEqualTo(FakeData.fakeTvSeries)
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun loadWatchlistwhenbothsucceedstateshowsmoviesandseries() = runTest {
        every { movieRepository.getWatchlistMovies() } returns
            flowOf(Result.Success(FakeData.fakeMovies))
        every { seriesRepository.getWatchlistSeries() } returns
            flowOf(Result.Success(FakeData.fakeTvSeries))

        val viewModel = createViewModel()
        val job = backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        val state = viewModel.uiState.value
        job.cancel()
        assertThat(state.watchlistMovies).isEqualTo(FakeData.fakeMovies)
        assertThat(state.watchlistTvSeries).isEqualTo(FakeData.fakeTvSeries)
    }

    @Test
    fun loadWatchlistwhenmovieserrorstateshowserrormessage() = runTest {
        val exception = Exception("Failed to load watchlist")
        every { movieRepository.getWatchlistMovies() } returns flowOf(Result.Error(exception))
        every { seriesRepository.getWatchlistSeries() } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        val job = backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        val state = viewModel.uiState.value
        job.cancel()
        assertThat(state.error).isEqualTo("Failed to load watchlist")
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun loadWatchlistwhenserieserrorstateshowserrormessage() = runTest {
        val exception = Exception("Series error")
        every { movieRepository.getWatchlistMovies() } returns flowOf(Result.Success(emptyList()))
        every { seriesRepository.getWatchlistSeries() } returns flowOf(Result.Error(exception))

        val viewModel = createViewModel()
        val job = backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        val state = viewModel.uiState.value
        job.cancel()
        assertThat(state.error).isEqualTo("Series error")
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun initialstatealllistsareempty() = runTest {
        every { movieRepository.getWatchlistMovies() } returns flowOf(Result.Loading)
        every { seriesRepository.getWatchlistSeries() } returns flowOf(Result.Loading)

        val viewModel = createViewModel()
        val job = backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        val state = viewModel.uiState.value
        job.cancel()
        assertThat(state.watchlistMovies).isEmpty()
        assertThat(state.watchlistTvSeries).isEmpty()
    }
}
