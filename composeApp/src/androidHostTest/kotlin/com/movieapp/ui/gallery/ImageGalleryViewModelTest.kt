package com.movieapp.ui.gallery

import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.domain.repository.ActorRepository
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.model.Image
import com.movieapp.core.model.ImageResponse
import io.mockk.coEvery
import io.mockk.coVerify
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
class ImageGalleryViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val movies = mockk<MovieRepository>()
    private val series = mockk<SeriesRepository>()
    private val actors = mockk<ActorRepository>()
    private val preferences = mockk<PreferencesRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        every { preferences.tmdbLanguage } returns flowOf("en-US")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load - movie images - merges distinct paths sorted by vote`() = runTest {
        coEvery { movies.getMovieImages(7, "en-US") } returns Result.Success(
            ImageResponse(
                backdrops = listOf(
                    Image(filePath = "/low.jpg", voteAverage = 1.0),
                    Image(filePath = "/high.jpg", voteAverage = 9.0),
                ),
                posters = listOf(Image(filePath = "/high.jpg", voteAverage = 8.0)),
            ),
        )

        val state = viewModel("movie").uiState.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
        assertThat(state.images.map { it.filePath }).containsExactly("/high.jpg", "/low.jpg").inOrder()
    }

    @Test
    fun `load - tv images - reads series gallery`() = runTest {
        coEvery { series.getTvSeriesImages(7, "en-US") } returns Result.Success(
            ImageResponse(posters = listOf(Image(filePath = "/poster.jpg", voteAverage = 4.0))),
        )

        val state = viewModel("tv").uiState.value

        assertThat(state.images.map { it.filePath }).containsExactly("/poster.jpg")
        assertThat(state.error).isNull()
    }

    @Test
    fun `load - person images - reads actor gallery`() = runTest {
        coEvery { actors.getActorImages(7) } returns Result.Success(
            listOf(Image(filePath = "/face.jpg")),
        )

        val state = viewModel("person").uiState.value

        assertThat(state.images.map { it.filePath }).containsExactly("/face.jpg")
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun `load - movie failure - shows the error message`() = runTest {
        coEvery { movies.getMovieImages(7, "en-US") } returns Result.Error(IllegalStateException("offline"))

        val state = viewModel("movie").uiState.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.images).isEmpty()
        assertThat(state.error).isEqualTo("offline")
    }

    @Test
    fun `load - person failure without message - shows fallback`() = runTest {
        coEvery { actors.getActorImages(7) } returns Result.Error(IllegalStateException(null as String?))

        assertThat(viewModel("person").uiState.value.error).isEqualTo("Could not load images")
    }

    @Test
    fun `load - missing id - reports invalid content`() = runTest {
        val state = viewModel(kind = "movie", contentId = 0).uiState.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.images).isEmpty()
        assertThat(state.error).isEqualTo("Invalid content")
        coVerify(exactly = 0) { movies.getMovieImages(any(), any()) }
    }

    @Test
    fun `load - unknown kind - reports unknown media type`() = runTest {
        val state = viewModel("album").uiState.value

        assertThat(state.error).isEqualTo("Unknown media type")
        assertThat(state.images).isEmpty()
    }

    @Test
    fun `load - still loading - keeps the loading flag`() = runTest {
        coEvery { movies.getMovieImages(7, "en-US") } returns Result.Loading

        assertThat(viewModel("movie").uiState.value.isLoading).isTrue()
    }

    @Test
    fun `retry - after a failure - replaces the error with images`() = runTest {
        coEvery { movies.getMovieImages(7, "en-US") } returns
            Result.Error(IllegalStateException("offline")) andThen
            Result.Success(ImageResponse(posters = listOf(Image(filePath = "/retry.jpg"))))
        val viewModel = viewModel("movie")

        viewModel.onEvent(ImageGalleryUiEvent.Retry)

        assertThat(viewModel.uiState.value.error).isNull()
        assertThat(viewModel.uiState.value.images.map { it.filePath }).containsExactly("/retry.jpg")
    }

    private fun viewModel(kind: String, contentId: Int = 7) =
        ImageGalleryViewModel(
            kind = kind,
            contentId = contentId,
            movieRepository = movies,
            seriesRepository = series,
            actorRepository = actors,
            preferencesRepository = preferences,
        )
}
