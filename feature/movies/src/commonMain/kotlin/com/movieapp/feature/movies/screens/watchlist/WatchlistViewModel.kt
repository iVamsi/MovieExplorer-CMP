package com.movieapp.feature.movies.screens.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.SeriesRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WatchlistViewModel(
    private val movieRepository: MovieRepository,
    private val seriesRepository: SeriesRepository,
) : ViewModel() {

    private val sortOption = MutableStateFlow(WatchlistSortOption.DEFAULT)

    private val _effectChannel = Channel<WatchlistSideEffect>()
    val sideEffect = _effectChannel.receiveAsFlow()

    val uiState: StateFlow<WatchlistUiState> =
        combine(
                movieRepository.getWatchlistMovies(),
                seriesRepository.getWatchlistSeries(),
                sortOption,
            ) { moviesResult, seriesResult, sort ->
                var isLoading = false
                var error: String? = null

                val rawMovies =
                    when (moviesResult) {
                        is Result.Success -> moviesResult.data
                        is Result.Error -> {
                            error = moviesResult.exception.message
                            emptyList()
                        }
                        is Result.Loading -> {
                            isLoading = true
                            emptyList()
                        }
                    }

                val rawSeries =
                    when (seriesResult) {
                        is Result.Success -> seriesResult.data
                        is Result.Error -> {
                            error = seriesResult.exception.message
                            emptyList()
                        }
                        is Result.Loading -> {
                            isLoading = true
                            emptyList()
                        }
                    }

                val sortedMovies =
                    when (sort) {
                        WatchlistSortOption.DEFAULT -> rawMovies
                        WatchlistSortOption.ALPHABETICAL -> rawMovies.sortedBy { it.title }
                        WatchlistSortOption.REVERSE_ALPHABETICAL ->
                            rawMovies.sortedByDescending { it.title }
                    }

                val sortedSeries =
                    when (sort) {
                        WatchlistSortOption.DEFAULT -> rawSeries
                        WatchlistSortOption.ALPHABETICAL -> rawSeries.sortedBy { it.name }
                        WatchlistSortOption.REVERSE_ALPHABETICAL ->
                            rawSeries.sortedByDescending { it.name }
                    }

                WatchlistUiState(
                    watchlistMovies = sortedMovies,
                    watchlistTvSeries = sortedSeries,
                    currentSortOption = sort,
                    isLoading = isLoading,
                    error = error,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = WatchlistUiState(isLoading = true),
            )

    fun onEvent(event: WatchlistUiEvent) {
        when (event) {
            is WatchlistUiEvent.SortOptionSelected -> sortOption.value = event.option
            is WatchlistUiEvent.MovieClicked ->
                emitEffect(WatchlistSideEffect.NavigateToMovieDetail(event.id))
            is WatchlistUiEvent.SeriesClicked ->
                emitEffect(WatchlistSideEffect.NavigateToSeriesDetail(event.id))
            WatchlistUiEvent.DiscoverClicked -> emitEffect(WatchlistSideEffect.NavigateToDiscover)
            WatchlistUiEvent.SeeAllMoviesClicked ->
                emitEffect(WatchlistSideEffect.NavigateToSeeAllMovies)
            WatchlistUiEvent.SeeAllSeriesClicked ->
                emitEffect(WatchlistSideEffect.NavigateToSeeAllSeries)
        }
    }

    private fun emitEffect(effect: WatchlistSideEffect) {
        viewModelScope.launch { _effectChannel.send(effect) }
    }
}
