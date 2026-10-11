package com.movieapp.feature.profile.screens.profile

import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.AuthRepository
import com.movieapp.core.domain.repository.AuthState
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.domain.usecase.GetUserWatchStatsUseCase
import com.movieapp.core.model.UserCollection
import com.movieapp.core.model.WatchStats
import com.movieapp.core.testing.FakeData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.job
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val movieRepository = mockk<MovieRepository>()
    private val seriesRepository = mockk<SeriesRepository>()
    private val userRepository = mockk<UserRepository>()
    private val getUserWatchStatsUseCase = mockk<GetUserWatchStatsUseCase>()
    private val authRepository = mockk<AuthRepository>()

    private val testCollection =
        UserCollection(
            id = "col_1",
            name = "My Favorites",
            description = "A test collection",
            isPublic = true,
            itemCount = 3,
            createdAt = 1000L,
            updatedAt = 2000L,
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { movieRepository.refreshMovies() } coAnswers {}
        every { authRepository.authState } returns flowOf(AuthState.SignedOut)
        every { getUserWatchStatsUseCase() } returns flowOf(Result.Success(WatchStats()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        ProfileViewModel(
            movieRepository,
            seriesRepository,
            userRepository,
            getUserWatchStatsUseCase,
            authRepository,
        )

    @Test
    fun `loadUserData - when collections loaded - state shows collections`() = runTest {
        every { userRepository.getAllCollections() } returns flowOf(listOf(testCollection))
        every { movieRepository.getWatchlistMovies() } returns flowOf(Result.Success(emptyList()))
        every { seriesRepository.getWatchlistSeries() } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertThat(state.userCollections).hasSize(1)
        assertThat(state.userCollections.first().name).isEqualTo("My Favorites")
    }

    @Test
    fun `loadUserData - when watchlist movies loaded - state shows movies`() = runTest {
        every { userRepository.getAllCollections() } returns flowOf(emptyList())
        every { movieRepository.getWatchlistMovies() } returns
            flowOf(Result.Success(FakeData.fakeMovies))
        every { seriesRepository.getWatchlistSeries() } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertThat(state.watchlistMovies).isEqualTo(FakeData.fakeMovies)
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun `loadUserData - when watchlist series loaded - state shows series`() = runTest {
        every { userRepository.getAllCollections() } returns flowOf(emptyList())
        every { movieRepository.getWatchlistMovies() } returns flowOf(Result.Success(emptyList()))
        every { seriesRepository.getWatchlistSeries() } returns
            flowOf(Result.Success(FakeData.fakeTvSeries))

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertThat(state.watchlistTvSeries).isEqualTo(FakeData.fakeTvSeries)
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun `loadUserData - when all succeed - state shows all data`() = runTest {
        every { userRepository.getAllCollections() } returns flowOf(listOf(testCollection))
        every { movieRepository.getWatchlistMovies() } returns
            flowOf(Result.Success(FakeData.fakeMovies))
        every { seriesRepository.getWatchlistSeries() } returns
            flowOf(Result.Success(FakeData.fakeTvSeries))

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertThat(state.userCollections).hasSize(1)
        assertThat(state.watchlistMovies).isEqualTo(FakeData.fakeMovies)
        assertThat(state.watchlistTvSeries).isEqualTo(FakeData.fakeTvSeries)
    }

    @Test
    fun `loadUserData - when movies error - state shows error`() = runTest {
        val exception = Exception("Movies unavailable")
        every { userRepository.getAllCollections() } returns flowOf(emptyList())
        every { movieRepository.getWatchlistMovies() } returns flowOf(Result.Error(exception))
        every { seriesRepository.getWatchlistSeries() } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertThat(state.error).isEqualTo("Movies unavailable")
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun `initial state - all lists are empty and not loading`() = runTest {
        every { userRepository.getAllCollections() } returns flowOf(emptyList())
        every { movieRepository.getWatchlistMovies() } returns flowOf(Result.Loading)
        every { seriesRepository.getWatchlistSeries() } returns flowOf(Result.Loading)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertThat(state.userCollections).isEmpty()
        assertThat(state.watchlistMovies).isEmpty()
        assertThat(state.watchlistTvSeries).isEmpty()
    }

    @Test
    fun `loadUserData - when stats loaded - state shows watch stats`() = runTest {
        every { userRepository.getAllCollections() } returns flowOf(emptyList())
        every { movieRepository.getWatchlistMovies() } returns flowOf(Result.Success(emptyList()))
        every { seriesRepository.getWatchlistSeries() } returns flowOf(Result.Success(emptyList()))
        every { getUserWatchStatsUseCase() } returns
            flowOf(Result.Success(WatchStats(totalWatchTimeMinutes = 152)))

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertThat(state.watchStats.totalWatchTimeMinutes).isEqualTo(152)
    }

    @Test
    fun `pull refresh - repository cancellation - refresh job remains canceled`() = runTest {
        val completionCause = CompletableDeferred<Throwable?>()
        every { userRepository.getAllCollections() } returns flowOf(emptyList())
        every { movieRepository.getWatchlistMovies() } returns flowOf(Result.Success(emptyList()))
        every { seriesRepository.getWatchlistSeries() } returns flowOf(Result.Success(emptyList()))
        coEvery { movieRepository.refreshMovies() } coAnswers
            {
                currentCoroutineContext().job.invokeOnCompletion { cause ->
                    completionCause.complete(cause)
                }
                throw CancellationException("cancelled")
            }

        val viewModel = createViewModel()
        viewModel.onEvent(ProfileUiEvent.PullRefresh)

        assertThat(completionCause.await()).isInstanceOf(CancellationException::class.java)
        assertThat(viewModel.isPullRefreshing.value).isFalse()
    }
}
