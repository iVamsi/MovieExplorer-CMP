package com.movieapp.feature.collections.screens.collections

import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.model.CollectionItem
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.Movie
import com.movieapp.core.model.TvSeries
import com.movieapp.core.model.UserCollection
import com.movieapp.core.testing.FakeData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@Suppress("FunctionNaming")
class CollectionDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val userRepository = mockk<UserRepository>()
    private val movieRepository = mockk<MovieRepository>()
    private val seriesRepository = mockk<SeriesRepository>()

    private val testCollectionId = "col_1"
    private val testCollection =
        UserCollection(
            id = testCollectionId,
            name = "Action Picks",
            description = "Best action content",
            isPublic = false,
            itemCount = 3,
            createdAt = 1_000L,
            updatedAt = 2_000L,
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun movieItemsFor(movies: List<Movie>): List<CollectionItem> = movies.map { m ->
        CollectionItem(
            id = "item_${m.id}",
            collectionId = testCollectionId,
            contentId = m.id,
            contentType = ContentType.MOVIE,
            addedAt = m.id.toLong(),
        )
    }

    private fun seriesItemsFor(series: List<TvSeries>): List<CollectionItem> = series.map { s ->
        CollectionItem(
            id = "item_s_${s.id}",
            collectionId = testCollectionId,
            contentId = s.id,
            contentType = ContentType.TV_SERIES,
            addedAt = s.id.toLong(),
        )
    }

    private fun createViewModel(
        collectionId: String = testCollectionId
    ): CollectionDetailViewModel {
        return CollectionDetailViewModel(
            collectionId = collectionId,
            userRepository = userRepository,
            movieRepository = movieRepository,
            seriesRepository = seriesRepository,
        )
    }

    @Test
    fun `loadCollectionDetail - when all succeed - state shows collection metadata`() = runTest {
        every { userRepository.getCollectionById(testCollectionId) } returns
            flowOf(Result.Success(testCollection))
        every { userRepository.getCollectionItems(testCollectionId) } returns flowOf(emptyList())
        every { movieRepository.getMoviesInCollection(testCollectionId) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getSeriesInCollection(testCollectionId) } returns
            flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertThat(state.collection?.name).isEqualTo("Action Picks")
        assertThat(state.collection?.id).isEqualTo(testCollectionId)
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun `loadCollectionDetail - when movies loaded - state shows movie rows`() = runTest {
        every { userRepository.getCollectionById(testCollectionId) } returns
            flowOf(Result.Success(testCollection))
        every { userRepository.getCollectionItems(testCollectionId) } returns
            flowOf(movieItemsFor(FakeData.fakeMovies))
        every { movieRepository.getMoviesInCollection(testCollectionId) } returns
            flowOf(Result.Success(FakeData.fakeMovies))
        every { seriesRepository.getSeriesInCollection(testCollectionId) } returns
            flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertThat(state.movieRows.map { it.movie })
            .isEqualTo(FakeData.fakeMovies.sortedByDescending { it.id })
    }

    @Test
    fun `loadCollectionDetail - when series loaded - state shows series rows`() = runTest {
        every { userRepository.getCollectionById(testCollectionId) } returns
            flowOf(Result.Success(testCollection))
        every { userRepository.getCollectionItems(testCollectionId) } returns
            flowOf(seriesItemsFor(FakeData.fakeTvSeries))
        every { movieRepository.getMoviesInCollection(testCollectionId) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getSeriesInCollection(testCollectionId) } returns
            flowOf(Result.Success(FakeData.fakeTvSeries))

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertThat(state.seriesRows.map { it.series })
            .isEqualTo(FakeData.fakeTvSeries.sortedByDescending { it.id })
    }

    @Test
    fun `loadCollectionDetail - when collection error - state shows error`() = runTest {
        val exception = Exception("Collection not found")
        every { userRepository.getCollectionById(testCollectionId) } returns
            flowOf(Result.Error(exception))
        every { userRepository.getCollectionItems(testCollectionId) } returns flowOf(emptyList())
        every { movieRepository.getMoviesInCollection(testCollectionId) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getSeriesInCollection(testCollectionId) } returns
            flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.error).isEqualTo("Collection not found")
    }

    @Test
    fun `loadCollectionDetail - when collection error has no message - state still exposes error`() =
        runTest {
            every { userRepository.getCollectionById(testCollectionId) } returns
            flowOf(Result.Error(IllegalStateException("")))
            every { userRepository.getCollectionItems(testCollectionId) } returns
                flowOf(emptyList())
            every { movieRepository.getMoviesInCollection(testCollectionId) } returns
                flowOf(Result.Success(emptyList()))
            every { seriesRepository.getSeriesInCollection(testCollectionId) } returns
                flowOf(Result.Success(emptyList()))

            val viewModel = createViewModel()

            assertThat(viewModel.uiState.value.error).isNotNull()
        }

    @Test
    fun `loadCollectionDetail - when movie rows fail - state exposes partial content error`() =
        runTest {
            every { userRepository.getCollectionById(testCollectionId) } returns
                flowOf(Result.Success(testCollection))
            every { userRepository.getCollectionItems(testCollectionId) } returns
                flowOf(emptyList())
            every { movieRepository.getMoviesInCollection(testCollectionId) } returns
                flowOf(Result.Error(IllegalStateException("movies unavailable")))
            every { seriesRepository.getSeriesInCollection(testCollectionId) } returns
                flowOf(Result.Success(FakeData.fakeTvSeries))

            val viewModel = createViewModel()

            assertThat(viewModel.uiState.value.collection).isEqualTo(testCollection)
            assertThat(viewModel.uiState.value.contentError).isEqualTo("movies unavailable")
            assertThat(viewModel.uiState.value.error).isNull()
        }

    @Test
    fun `loadCollectionDetail - when row failure has no message - state still exposes content error`() =
        runTest {
            every { userRepository.getCollectionById(testCollectionId) } returns
                flowOf(Result.Success(testCollection))
            every { userRepository.getCollectionItems(testCollectionId) } returns
                flowOf(emptyList())
            every { movieRepository.getMoviesInCollection(testCollectionId) } returns
            flowOf(Result.Error(IllegalStateException("")))
            every { seriesRepository.getSeriesInCollection(testCollectionId) } returns
                flowOf(Result.Success(emptyList()))

            val viewModel = createViewModel()

            assertThat(viewModel.uiState.value.contentError).isNotNull()
        }

    @Test
    fun `loadCollectionDetail - when movie rows are loading - state remains loading`() = runTest {
        every { userRepository.getCollectionById(testCollectionId) } returns
            flowOf(Result.Success(testCollection))
        every { userRepository.getCollectionItems(testCollectionId) } returns flowOf(emptyList())
        every { movieRepository.getMoviesInCollection(testCollectionId) } returns
            flowOf(Result.Loading)
        every { seriesRepository.getSeriesInCollection(testCollectionId) } returns
            flowOf(Result.Success(FakeData.fakeTvSeries))

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.isLoading).isTrue()
        assertThat(viewModel.uiState.value.contentError).isNull()
    }

    @Test
    fun `loadCollectionDetail - when collection metadata is loading - state remains loading`() =
        runTest {
            every { userRepository.getCollectionById(testCollectionId) } returns
                flowOf(Result.Loading)
            every { userRepository.getCollectionItems(testCollectionId) } returns
                flowOf(emptyList())
            every { movieRepository.getMoviesInCollection(testCollectionId) } returns
                flowOf(Result.Success(emptyList()))
            every { seriesRepository.getSeriesInCollection(testCollectionId) } returns
                flowOf(Result.Success(emptyList()))

            val viewModel = createViewModel()

            assertThat(viewModel.uiState.value.collection).isNull()
            assertThat(viewModel.uiState.value.isLoading).isTrue()
        }

    @Test
    fun `onEvent RemoveMovie - calls removeFromCollection with MOVIE type`() = runTest {
        every { userRepository.getCollectionById(testCollectionId) } returns
            flowOf(Result.Success(testCollection))
        every { userRepository.getCollectionItems(testCollectionId) } returns flowOf(emptyList())
        every { movieRepository.getMoviesInCollection(testCollectionId) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getSeriesInCollection(testCollectionId) } returns
            flowOf(Result.Success(emptyList()))
        coEvery { userRepository.removeFromCollection(any(), any(), any()) } returns
            Result.Success(Unit)

        val viewModel = createViewModel()
        viewModel.onEvent(CollectionDetailUiEvent.RemoveMovie(movieId = 1))

        coVerify { userRepository.removeFromCollection(testCollectionId, 1, ContentType.MOVIE) }
    }

    @Test
    fun `onEvent RemoveMovie - when repository fails - emits failed effect and clears progress`() =
        runTest {
            every { userRepository.getCollectionById(testCollectionId) } returns
                flowOf(Result.Success(testCollection))
            every { userRepository.getCollectionItems(testCollectionId) } returns
                flowOf(emptyList())
            every { movieRepository.getMoviesInCollection(testCollectionId) } returns
                flowOf(Result.Success(emptyList()))
            every { seriesRepository.getSeriesInCollection(testCollectionId) } returns
                flowOf(Result.Success(emptyList()))
            coEvery { userRepository.removeFromCollection(any(), any(), any()) } returns
                Result.Error(IllegalStateException("write failed"))
            val viewModel = createViewModel()
            val effect = async { viewModel.sideEffect.first() }

            viewModel.onEvent(CollectionDetailUiEvent.RemoveMovie(movieId = 1))

            assertThat(viewModel.uiState.value.pendingMutation).isNull()
            assertThat(effect.await())
                .isEqualTo(
                    CollectionDetailSideEffect.MutationFailed(CollectionDetailMutation.REMOVE_ITEM)
                )
        }

    @Test
    fun `onEvent RemoveSeries - calls removeFromCollection with TV_SERIES type`() = runTest {
        every { userRepository.getCollectionById(testCollectionId) } returns
            flowOf(Result.Success(testCollection))
        every { userRepository.getCollectionItems(testCollectionId) } returns flowOf(emptyList())
        every { movieRepository.getMoviesInCollection(testCollectionId) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getSeriesInCollection(testCollectionId) } returns
            flowOf(Result.Success(emptyList()))
        coEvery { userRepository.removeFromCollection(any(), any(), any()) } returns
            Result.Success(Unit)

        val viewModel = createViewModel()
        viewModel.onEvent(CollectionDetailUiEvent.RemoveSeries(seriesId = 1))

        coVerify { userRepository.removeFromCollection(testCollectionId, 1, ContentType.TV_SERIES) }
    }

    @Test
    fun `initial state - lists are empty before load completes`() = runTest {
        every { userRepository.getCollectionById(testCollectionId) } returns flowOf(Result.Loading)
        every { userRepository.getCollectionItems(testCollectionId) } returns flowOf(emptyList())
        every { movieRepository.getMoviesInCollection(testCollectionId) } returns
            flowOf(Result.Loading)
        every { seriesRepository.getSeriesInCollection(testCollectionId) } returns
            flowOf(Result.Loading)

        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.movieRows).isEmpty()
        assertThat(viewModel.uiState.value.seriesRows).isEmpty()
    }

    @Test
    fun `onEvent RenameCollection - updates only the collection name`() = runTest {
        every { userRepository.getCollectionById(testCollectionId) } returns
            flowOf(Result.Success(testCollection))
        every { userRepository.getCollectionItems(testCollectionId) } returns flowOf(emptyList())
        every { movieRepository.getMoviesInCollection(testCollectionId) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getSeriesInCollection(testCollectionId) } returns
            flowOf(Result.Success(emptyList()))
        coEvery { userRepository.updateCollection(any()) } returns Result.Success(Unit)

        val viewModel = createViewModel()
        viewModel.onEvent(CollectionDetailUiEvent.RenameCollection("Renamed"))

        coVerify { userRepository.updateCollection(testCollection.copy(name = "Renamed")) }
    }

    @Test
    fun `onEvent DeleteCollection - deletes the whole collection without removing an item`() =
        runTest {
            every { userRepository.getCollectionById(testCollectionId) } returns
                flowOf(Result.Success(testCollection))
            every { userRepository.getCollectionItems(testCollectionId) } returns
                flowOf(emptyList())
            every { movieRepository.getMoviesInCollection(testCollectionId) } returns
                flowOf(Result.Success(emptyList()))
            every { seriesRepository.getSeriesInCollection(testCollectionId) } returns
                flowOf(Result.Success(emptyList()))
            coEvery { userRepository.deleteCollection(testCollectionId) } returns
                Result.Success(Unit)

            val viewModel = createViewModel()
            viewModel.onEvent(CollectionDetailUiEvent.DeleteCollection)

            coVerify { userRepository.deleteCollection(testCollectionId) }
            coVerify(exactly = 0) { userRepository.removeFromCollection(any(), any(), any()) }
        }

    @Test
    fun `onEvent DeleteCollection - when repository succeeds - emits saved effect`() = runTest {
        every { userRepository.getCollectionById(testCollectionId) } returns
            flowOf(Result.Success(testCollection))
        every { userRepository.getCollectionItems(testCollectionId) } returns flowOf(emptyList())
        every { movieRepository.getMoviesInCollection(testCollectionId) } returns
            flowOf(Result.Success(emptyList()))
        every { seriesRepository.getSeriesInCollection(testCollectionId) } returns
            flowOf(Result.Success(emptyList()))
        coEvery { userRepository.deleteCollection(testCollectionId) } returns Result.Success(Unit)
        val viewModel = createViewModel()
        val effect = async { viewModel.sideEffect.first() }

        viewModel.onEvent(CollectionDetailUiEvent.DeleteCollection)

        assertThat(effect.await())
            .isEqualTo(CollectionDetailSideEffect.MutationSaved(CollectionDetailMutation.DELETE))
        assertThat(viewModel.uiState.value.pendingMutation).isNull()
    }
}
