package com.movieapp.feature.movies.screens.favorites

import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.ActorRepository
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

class FavoritesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val movieRepository = mockk<MovieRepository>()
    private val seriesRepository = mockk<SeriesRepository>()
    private val actorRepository = mockk<ActorRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { actorRepository.getFavoriteActors() } returns flowOf(Result.Success(emptyList()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        FavoritesViewModel(movieRepository, seriesRepository, actorRepository)

    @Test
    fun loadFavoriteswhenmoviessucceedstateshowsfavoritemovies() = runTest {
        every { movieRepository.getFavoriteMovies() } returns
            flowOf(Result.Success(FakeData.fakeMovies))
        every { seriesRepository.getFavoriteSeries() } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        val job = backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        val state = viewModel.uiState.value
        job.cancel()
        assertThat(state.favoriteMovies).isEqualTo(FakeData.fakeMovies)
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun loadFavoriteswhenseriessucceedstateshowsfavoriteseries() = runTest {
        every { movieRepository.getFavoriteMovies() } returns flowOf(Result.Success(emptyList()))
        every { seriesRepository.getFavoriteSeries() } returns
            flowOf(Result.Success(FakeData.fakeTvSeries))

        val viewModel = createViewModel()
        val job = backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        val state = viewModel.uiState.value
        job.cancel()
        assertThat(state.favoriteTvSeries).isEqualTo(FakeData.fakeTvSeries)
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun loadFavoriteswhenmoviesandseriesbothsucceedstateshowsboth() = runTest {
        every { movieRepository.getFavoriteMovies() } returns
            flowOf(Result.Success(FakeData.fakeMovies))
        every { seriesRepository.getFavoriteSeries() } returns
            flowOf(Result.Success(FakeData.fakeTvSeries))

        val viewModel = createViewModel()
        val job = backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        val state = viewModel.uiState.value
        job.cancel()
        assertThat(state.favoriteMovies).isEqualTo(FakeData.fakeMovies)
        assertThat(state.favoriteTvSeries).isEqualTo(FakeData.fakeTvSeries)
    }

    @Test
    fun loadFavoriteswhenmovieserrorstateshowserror() = runTest {
        val exception = Exception("Network error")
        every { movieRepository.getFavoriteMovies() } returns flowOf(Result.Error(exception))
        every { seriesRepository.getFavoriteSeries() } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        val job = backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        val state = viewModel.uiState.value
        job.cancel()
        assertThat(state.error).isEqualTo("Network error")
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun loadFavoritesfavoriteactorslistisalwaysempty() = runTest {
        every { movieRepository.getFavoriteMovies() } returns flowOf(Result.Success(emptyList()))
        every { seriesRepository.getFavoriteSeries() } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        val job = backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        val state = viewModel.uiState.value
        job.cancel()
        assertThat(state.favoriteActors).isEmpty()
    }

    @Test
    fun initialstatealllistsareemptyandnotloading() = runTest {
        every { movieRepository.getFavoriteMovies() } returns flowOf(Result.Loading)
        every { seriesRepository.getFavoriteSeries() } returns flowOf(Result.Loading)
        every { actorRepository.getFavoriteActors() } returns flowOf(Result.Loading)

        val viewModel = createViewModel()
        val job = backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        val state = viewModel.uiState.value
        job.cancel()
        assertThat(state.favoriteMovies).isEmpty()
        assertThat(state.favoriteTvSeries).isEmpty()
        assertThat(state.favoriteActors).isEmpty()
    }
}
