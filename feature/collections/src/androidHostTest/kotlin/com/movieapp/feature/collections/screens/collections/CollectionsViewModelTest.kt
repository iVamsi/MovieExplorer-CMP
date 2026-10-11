package com.movieapp.feature.collections.screens.collections

import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.model.UserCollection as DomainUserCollection
import com.movieapp.core.testing.FakeData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import com.movieapp.core.testing.FixedClock
import kotlin.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.job
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@Suppress("FunctionNaming")
class CollectionsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val userRepository = mockk<UserRepository>()
    private val movieRepository = mockk<MovieRepository>()
    private val seriesRepository = mockk<SeriesRepository>()
    private val fixedClock = FixedClock(Instant.fromEpochMilliseconds(99_000L))

    private val testDomainCollection =
        DomainUserCollection(
            id = "col_1",
            name = "Action Classics",
            description = "Best action films",
            isPublic = false,
            itemCount = 2,
            createdAt = 1_000L,
            updatedAt = 2_000L,
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { movieRepository.getMoviesInCollection(any()) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getSeriesInCollection(any()) } returns
            flowOf(Result.Success(emptyList()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        CollectionsViewModel(
            userRepository = userRepository,
            movieRepository = movieRepository,
            seriesRepository = seriesRepository,
            clock = fixedClock,
        )

    @Test
    fun `loadCollections - with locally cached members - exposes their mosaic artwork`() = runTest {
        every { userRepository.getAllCollections() } returns flowOf(listOf(testDomainCollection))
        every { movieRepository.getMoviesInCollection(testDomainCollection.id) } returns
            flowOf(Result.Success(listOf(FakeData.fakeMovies.first())))
        every { seriesRepository.getSeriesInCollection(testDomainCollection.id) } returns
            flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.collections.single().items)
            .containsExactly(CollectionItem.MovieItem(FakeData.fakeMovies.first()))
    }

    @Test
    fun `loadCollections - when repository emits collections - state shows them`() = runTest {
        every { userRepository.getAllCollections() } returns flowOf(listOf(testDomainCollection))

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertThat(state.collections).hasSize(1)
        assertThat(state.collections.first().name).isEqualTo("Action Classics")
        assertThat(state.collections.first().id).isEqualTo("col_1")
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun `loadCollections - when repository emits empty list - state has no collections`() =
        runTest {
            every { userRepository.getAllCollections() } returns flowOf(emptyList())

            val viewModel = createViewModel()

            assertThat(viewModel.uiState.value.collections).isEmpty()
            assertThat(viewModel.uiState.value.isLoading).isFalse()
        }

    @Test
    fun `loadCollections - when mosaic artwork fails - state keeps collection and exposes partial error`() =
        runTest {
            every { userRepository.getAllCollections() } returns
                flowOf(listOf(testDomainCollection))
            every { movieRepository.getMoviesInCollection(testDomainCollection.id) } returns
                flowOf(Result.Error(IllegalStateException("artwork unavailable")))
            every { seriesRepository.getSeriesInCollection(testDomainCollection.id) } returns
                flowOf(Result.Success(emptyList()))

            val viewModel = createViewModel()

            assertThat(viewModel.uiState.value.collections).hasSize(1)
            assertThat(viewModel.uiState.value.partialError).isEqualTo("artwork unavailable")
        }

    @Test
    fun `loadCollections - when mosaic failure has no message - state still exposes partial error`() =
        runTest {
            every { userRepository.getAllCollections() } returns
                flowOf(listOf(testDomainCollection))
            every { movieRepository.getMoviesInCollection(testDomainCollection.id) } returns
                flowOf(Result.Error(IllegalStateException("")))
            every { seriesRepository.getSeriesInCollection(testDomainCollection.id) } returns
                flowOf(Result.Success(emptyList()))

            val viewModel = createViewModel()

            assertThat(viewModel.uiState.value.partialError).isNotNull()
        }

    @Test
    fun `loadCollections - when mosaic artwork is loading - state remains loading`() = runTest {
        every { userRepository.getAllCollections() } returns flowOf(listOf(testDomainCollection))
        every { movieRepository.getMoviesInCollection(testDomainCollection.id) } returns
            flowOf(Result.Loading)
        every { seriesRepository.getSeriesInCollection(testDomainCollection.id) } returns
            flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.collections).hasSize(1)
        assertThat(viewModel.uiState.value.isLoading).isTrue()
        assertThat(viewModel.uiState.value.partialError).isNull()
    }

    @Test
    fun `loadCollections - repository cancellation - collector job remains canceled`() = runTest {
        val completionCause = CompletableDeferred<Throwable?>()
        every { userRepository.getAllCollections() } returns
            flow {
                currentCoroutineContext().job.invokeOnCompletion { cause ->
                    completionCause.complete(cause)
                }
                throw CancellationException("cancelled")
            }

        createViewModel()

        assertThat(completionCause.await() is CancellationException).isTrue()
    }

    @Test
    fun `onEvent Retry - after load failure - reloads collections`() = runTest {
        var loadAttempts = 0
        every { userRepository.getAllCollections() } answers
            {
                loadAttempts += 1
                if (loadAttempts == 1) {
                    flow { throw IllegalStateException("offline") }
                } else {
                    flowOf(emptyList())
                }
            }

        val viewModel = createViewModel()
        assertThat(viewModel.uiState.value.error).isEqualTo("offline")

        viewModel.onEvent(CollectionsUiEvent.Retry)

        assertThat(loadAttempts).isEqualTo(2)
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.error).isNull()
    }

    @Test
    fun `onEvent Retry - cancels previous collection collector`() = runTest {
        val firstLoad = MutableSharedFlow<List<DomainUserCollection>>()
        var loadAttempts = 0
        every { userRepository.getAllCollections() } answers
            {
                loadAttempts += 1
                if (loadAttempts == 1) firstLoad else flowOf(emptyList())
            }
        every { movieRepository.getMoviesInCollection(testDomainCollection.id) } returns
            flowOf(Result.Success(FakeData.fakeMovies.take(1)))
        every { seriesRepository.getSeriesInCollection(testDomainCollection.id) } returns
            flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        firstLoad.emit(listOf(testDomainCollection))
        assertThat(viewModel.uiState.value.collections).hasSize(1)

        viewModel.onEvent(CollectionsUiEvent.Retry)
        assertThat(viewModel.uiState.value.collections).isEmpty()

        firstLoad.emit(listOf(testDomainCollection.copy(name = "Stale collection")))
        assertThat(viewModel.uiState.value.collections).isEmpty()
    }

    @Test
    fun `loadCollections - when failure has no message - state still exposes error`() = runTest {
        every { userRepository.getAllCollections() } returns flow { throw IllegalStateException("") }

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.error).isNotNull()
    }

    @Test
    fun `createCollection - calls repository with correct params`() = runTest {
        every { userRepository.getAllCollections() } returns flowOf(emptyList())
        coEvery { userRepository.createCollection(any(), any(), any()) } returns
            Result.Success(testDomainCollection)

        val viewModel = createViewModel()
        viewModel.onEvent(
            CollectionsUiEvent.CreateCollection(
                name = "Sci-Fi Picks",
                description = "Best sci-fi movies",
                isPublic = true,
            )
        )

        coVerify { userRepository.createCollection("Sci-Fi Picks", "Best sci-fi movies", true) }
    }

    @Test
    fun `createCollection - while repository is pending - exposes progress then saved effect`() =
        runTest {
            every { userRepository.getAllCollections() } returns flowOf(emptyList())
            val repositoryResult = CompletableDeferred<Result<DomainUserCollection>>()
            coEvery { userRepository.createCollection(any(), any(), any()) } coAnswers
                {
                    repositoryResult.await()
                }
            val viewModel = createViewModel()
            val effect = async { viewModel.sideEffect.first() }

            viewModel.onEvent(
                CollectionsUiEvent.CreateCollection(
                    name = "Sci-Fi Picks",
                    description = "",
                    isPublic = false,
                )
            )

            assertThat(viewModel.uiState.value.pendingMutation)
                .isEqualTo(CollectionsMutation.CREATE)

            repositoryResult.complete(Result.Success(testDomainCollection))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.pendingMutation).isNull()
            assertThat(effect.await())
                .isEqualTo(CollectionsSideEffect.MutationSaved(CollectionsMutation.CREATE))
        }

    @Test
    fun `createCollection - when description is blank - passes null to repository`() = runTest {
        every { userRepository.getAllCollections() } returns flowOf(emptyList())
        coEvery { userRepository.createCollection(any(), any(), any()) } returns
            Result.Success(testDomainCollection)

        val viewModel = createViewModel()
        viewModel.onEvent(
            CollectionsUiEvent.CreateCollection(
                name = "Empty Desc",
                description = "  ",
                isPublic = false,
            )
        )

        coVerify { userRepository.createCollection("Empty Desc", null, false) }
    }

    @Test
    fun `deleteCollection - calls repository with correct id`() = runTest {
        every { userRepository.getAllCollections() } returns flowOf(listOf(testDomainCollection))
        coEvery { userRepository.deleteCollection(any()) } returns Result.Success(Unit)

        val viewModel = createViewModel()
        viewModel.onEvent(CollectionsUiEvent.DeleteCollection("col_1"))

        coVerify { userRepository.deleteCollection("col_1") }
    }

    @Test
    fun `deleteCollection - when repository fails - clears progress and emits failed effect`() =
        runTest {
            every { userRepository.getAllCollections() } returns
                flowOf(listOf(testDomainCollection))
            coEvery { userRepository.deleteCollection(any()) } returns
                Result.Error(IllegalStateException("disk full"))
            val viewModel = createViewModel()
            val effect = async { viewModel.sideEffect.first() }

            viewModel.onEvent(CollectionsUiEvent.DeleteCollection("col_1"))

            advanceUntilIdle()
            assertThat(viewModel.uiState.value.pendingMutation).isNull()
            assertThat(effect.await())
                .isEqualTo(CollectionsSideEffect.MutationFailed(CollectionsMutation.DELETE))
        }

    @Test
    fun `editCollection - calls updateCollection with clock timestamp`() = runTest {
        every { userRepository.getAllCollections() } returns flowOf(listOf(testDomainCollection))
        coEvery { userRepository.updateCollection(any()) } returns Result.Success(Unit)

        val viewModel = createViewModel()
        viewModel.onEvent(
            CollectionsUiEvent.EditCollection(
                collectionId = "col_1",
                name = "New Name",
                description = "New description",
                isPublic = true,
            )
        )

        coVerify {
            userRepository.updateCollection(
                match { updated ->
                    updated.id == "col_1" &&
                        updated.name == "New Name" &&
                        updated.description == "New description" &&
                        updated.isPublic &&
                        updated.updatedAt == 99_000L
                }
            )
        }
    }

    @Test
    fun `editCollection - when collection id not found - does not call repository`() = runTest {
        every { userRepository.getAllCollections() } returns flowOf(listOf(testDomainCollection))

        val viewModel = createViewModel()
        viewModel.onEvent(
            CollectionsUiEvent.EditCollection(
                collectionId = "nonexistent_id",
                name = "Name",
                description = "Desc",
                isPublic = false,
            )
        )

        coVerify(exactly = 0) { userRepository.updateCollection(any()) }
    }
}
