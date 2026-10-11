package com.movieapp.feature.movies.screens.upcoming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movieapp.core.common.Result
import com.movieapp.core.common.toLocalDateOrNull
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.model.Movie
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class UpcomingCalendarUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface UpcomingCalendarUiEvent {
    data object Refresh : UpcomingCalendarUiEvent
}

class UpcomingCalendarViewModel(
    private val movieRepository: MovieRepository,
    private val clock: Clock,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UpcomingCalendarUiState(isLoading = true))
    val uiState: StateFlow<UpcomingCalendarUiState> = _uiState.asStateFlow()

    init {
        loadUpcoming()
    }

    fun onEvent(event: UpcomingCalendarUiEvent) {
        when (event) {
            UpcomingCalendarUiEvent.Refresh -> loadUpcoming()
        }
    }

    private fun loadUpcoming() {
        viewModelScope.launch {
            movieRepository.getUpcomingMovies(page = 1).collect { result ->
                when (result) {
                    Result.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Result.Success -> {
                        val today = clock.todayIn(TimeZone.currentSystemDefault())
                        _uiState.update { state ->
                            state.copy(
                                movies = result.data
                                    .filter { movie ->
                                        movie.releaseDate?.toLocalDateOrNull()?.let { it >= today } == true
                                    }
                                    .sortedBy { it.releaseDate },
                                isLoading = false,
                                error = null,
                            )
                        }
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Could not load upcoming movies",
                        )
                    }
                }
            }
        }
    }
}
