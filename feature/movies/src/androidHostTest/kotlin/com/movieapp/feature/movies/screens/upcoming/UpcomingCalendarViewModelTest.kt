package com.movieapp.feature.movies.screens.upcoming

import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.model.Movie
import com.movieapp.core.testing.FixedClock
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Instant
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

class UpcomingCalendarViewModelTest {
    private val repository = mockk<MovieRepository>()
    private val clock = FixedClock(Instant.parse("2026-01-01T12:00:00Z"))

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initupcomingmoviesexposessortedvalidfuturedates() = runTest {
        val first = Movie(id = 1, title = "First", releaseDate = "2026-01-02")
        val last = Movie(id = 2, title = "Last", releaseDate = "2026-02-01")
        every { repository.getUpcomingMovies(page = 1) } returns
            flowOf(
                Result.Success(
                    listOf(
                        last,
                        Movie(id = 3, title = "Missing"),
                        Movie(id = 4, title = "Invalid", releaseDate = "2026-02-30"),
                        Movie(id = 5, title = "Past", releaseDate = "2025-12-01"),
                        first,
                    )
                )
            )
        val viewModel = UpcomingCalendarViewModel(repository, clock)
        assertThat(viewModel.uiState.value.movies).containsExactly(first, last).inOrder()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun refreshnetworkfailsretainsmoviesandexposeserror() = runTest {
        val movie = Movie(id = 1, title = "First", releaseDate = "2026-01-02")
        every { repository.getUpcomingMovies(page = 1) } returnsMany listOf(
            flowOf(Result.Success(listOf(movie))),
            flow {
                emit(Result.Loading)
                emit(Result.Error(IllegalStateException("Offline")))
            },
        )
        val viewModel = UpcomingCalendarViewModel(repository, clock)
        viewModel.onEvent(UpcomingCalendarUiEvent.Refresh)
        assertThat(viewModel.uiState.value.movies).containsExactly(movie)
        assertThat(viewModel.uiState.value.error).isEqualTo("Offline")
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }
}
