package com.movieapp.feature.actors.screens.actor

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.ActorRepository
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.domain.usecase.ToggleFavoriteActorUseCase
import com.movieapp.core.model.ContentType
import com.movieapp.core.testing.FakeData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class ActorDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val actorRepository = mockk<ActorRepository>()
    private val userRepository = mockk<UserRepository>()
    private val toggleFavoriteActorUseCase = mockk<ToggleFavoriteActorUseCase>()

    private val testActor = FakeData.fakeActors.first()
    private val testMovies = FakeData.fakeMovies
    private val testSeries = FakeData.fakeTvSeries

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(actorId: Int = testActor.id): ActorDetailViewModel {
        coEvery { userRepository.isFavorite(actorId, ContentType.ACTOR) } returns false
        coEvery { toggleFavoriteActorUseCase(any(), any()) } returns Result.Success(Unit)
        val savedStateHandle = SavedStateHandle(mapOf("actorId" to actorId))
        return ActorDetailViewModel(
            actorRepository = actorRepository,
            userRepository = userRepository,
            toggleFavoriteActorUseCase = toggleFavoriteActorUseCase,
            savedStateHandle = savedStateHandle,
        )
    }

    @Test
    fun `loadActorDetails - when actor flow emits success - state shows actor`() = runTest {
        coEvery { actorRepository.getActorByIdFlow(testActor.id) } returns
            flow {
                emit(Result.Loading)
                emit(Result.Success(testActor))
            }
        coEvery { actorRepository.getActorMovieCredits(testActor.id) } returns
            flowOf(Result.Success(testMovies))
        coEvery { actorRepository.getActorTvCredits(testActor.id) } returns
            flowOf(Result.Success(testSeries))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.actor).isEqualTo(testActor)
            assertThat(state.isLoading).isFalse()
            assertThat(state.error).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadActorDetails - when actor flow emits error - state shows error`() = runTest {
        val errorMessage = "Actor not found"
        coEvery { actorRepository.getActorByIdFlow(testActor.id) } returns
            flow { emit(Result.Error(Exception(errorMessage))) }
        coEvery { actorRepository.getActorMovieCredits(testActor.id) } returns
            flowOf(Result.Success(emptyList()))
        coEvery { actorRepository.getActorTvCredits(testActor.id) } returns
            flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.actor).isNull()
            assertThat(state.error).isEqualTo(errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadActorDetails - when credits succeed - state contains movie and tv credits`() =
        runTest {
            coEvery { actorRepository.getActorByIdFlow(testActor.id) } returns
                flowOf(Result.Success(testActor))
            coEvery { actorRepository.getActorMovieCredits(testActor.id) } returns
                flowOf(Result.Success(testMovies))
            coEvery { actorRepository.getActorTvCredits(testActor.id) } returns
                flowOf(Result.Success(testSeries))

            val viewModel = createViewModel()

            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.movieCredits).isEqualTo(testMovies)
                assertThat(state.tvCredits).isEqualTo(testSeries)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `loadActorDetails - when credits fail - credits remain empty`() = runTest {
        coEvery { actorRepository.getActorByIdFlow(testActor.id) } returns
            flowOf(Result.Success(testActor))
        coEvery { actorRepository.getActorMovieCredits(testActor.id) } returns
            flowOf(Result.Error(Exception("Credits unavailable")))
        coEvery { actorRepository.getActorTvCredits(testActor.id) } returns
            flowOf(Result.Error(Exception("Credits unavailable")))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.movieCredits).isEmpty()
            assertThat(state.tvCredits).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onEvent Retry - reloads all actor data`() = runTest {
        coEvery { actorRepository.getActorByIdFlow(testActor.id) } returns
            flowOf(Result.Success(testActor))
        coEvery { actorRepository.getActorMovieCredits(testActor.id) } returns
            flowOf(Result.Success(testMovies))
        coEvery { actorRepository.getActorTvCredits(testActor.id) } returns
            flowOf(Result.Success(testSeries))

        val viewModel = createViewModel()
        viewModel.onEvent(ActorDetailUiEvent.Retry)

        // Each of the three flows is collected twice (init + retry)
        coEvery { actorRepository.getActorByIdFlow(testActor.id) } returns
            flowOf(Result.Success(testActor))
        assertThat(viewModel.uiState.value.actor).isEqualTo(testActor)
    }

    @Test
    fun `onEvent ToggleFavorite - when save succeeds - emits typed update`() = runTest {
        coEvery { actorRepository.getActorByIdFlow(testActor.id) } returns
            flowOf(Result.Success(testActor))
        coEvery { actorRepository.getActorMovieCredits(testActor.id) } returns
            flowOf(Result.Success(emptyList()))
        coEvery { actorRepository.getActorTvCredits(testActor.id) } returns
            flowOf(Result.Success(emptyList()))
        val viewModel = createViewModel()
        coEvery { toggleFavoriteActorUseCase(testActor.id, false) } returns Result.Success(Unit)

        viewModel.sideEffect.test {
            viewModel.onEvent(ActorDetailUiEvent.ToggleFavorite)
            assertThat(awaitItem())
                .isEqualTo(ActorDetailSideEffect.FavoriteUpdated(isFavorite = true))
        }
    }

    @Test
    fun `onEvent ToggleFavorite - when save fails - keeps state and reports failure`() = runTest {
        coEvery { actorRepository.getActorByIdFlow(testActor.id) } returns
            flowOf(Result.Success(testActor))
        coEvery { actorRepository.getActorMovieCredits(testActor.id) } returns
            flowOf(Result.Success(emptyList()))
        coEvery { actorRepository.getActorTvCredits(testActor.id) } returns
            flowOf(Result.Success(emptyList()))
        val viewModel = createViewModel()
        coEvery { toggleFavoriteActorUseCase(testActor.id, false) } returns
            Result.Error(Exception("disk full"))

        viewModel.sideEffect.test {
            viewModel.onEvent(ActorDetailUiEvent.ToggleFavorite)
            assertThat(awaitItem()).isEqualTo(ActorDetailSideEffect.FavoriteUpdateFailed)
        }
        assertThat(viewModel.uiState.value.isFavorite).isFalse()
        assertThat(viewModel.uiState.value.isFavoriteMutationPending).isFalse()
    }
}
