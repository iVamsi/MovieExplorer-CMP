package com.movieapp.feature.movies.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.ActorRepository
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

class FavoritesViewModel(
    private val movieRepository: MovieRepository,
    private val seriesRepository: SeriesRepository,
    private val actorRepository: ActorRepository,
) : ViewModel() {

    private val sortOption = MutableStateFlow(FavoritesSortOption.DEFAULT)

    private val _effectChannel = Channel<FavoritesSideEffect>()
    val sideEffect = _effectChannel.receiveAsFlow()

    val uiState: StateFlow<FavoritesUiState> =
        combine(
                movieRepository.getFavoriteMovies(),
                seriesRepository.getFavoriteSeries(),
                actorRepository.getFavoriteActors(),
                sortOption,
            ) { moviesResult, seriesResult, actorsResult, sort ->
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

                val rawActors =
                    when (actorsResult) {
                        is Result.Success -> actorsResult.data
                        is Result.Error -> {
                            error = actorsResult.exception.message
                            emptyList()
                        }
                        is Result.Loading -> {
                            isLoading = true
                            emptyList()
                        }
                    }

                val sortedMovies =
                    when (sort) {
                        FavoritesSortOption.DEFAULT -> rawMovies
                        FavoritesSortOption.ALPHABETICAL -> rawMovies.sortedBy { it.title }
                        FavoritesSortOption.REVERSE_ALPHABETICAL ->
                            rawMovies.sortedByDescending { it.title }
                    }

                val sortedSeries =
                    when (sort) {
                        FavoritesSortOption.DEFAULT -> rawSeries
                        FavoritesSortOption.ALPHABETICAL -> rawSeries.sortedBy { it.name }
                        FavoritesSortOption.REVERSE_ALPHABETICAL ->
                            rawSeries.sortedByDescending { it.name }
                    }

                val sortedActors =
                    when (sort) {
                        FavoritesSortOption.DEFAULT -> rawActors
                        FavoritesSortOption.ALPHABETICAL -> rawActors.sortedBy { it.name }
                        FavoritesSortOption.REVERSE_ALPHABETICAL ->
                            rawActors.sortedByDescending { it.name }
                    }

                FavoritesUiState(
                    favoriteMovies = sortedMovies,
                    favoriteTvSeries = sortedSeries,
                    favoriteActors = sortedActors,
                    currentSortOption = sort,
                    isLoading = isLoading,
                    error = error,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = FavoritesUiState(isLoading = true),
            )

    fun onEvent(event: FavoritesUiEvent) {
        when (event) {
            is FavoritesUiEvent.SortOptionSelected -> sortOption.value = event.option
            is FavoritesUiEvent.MovieClicked ->
                emitEffect(FavoritesSideEffect.NavigateToMovieDetail(event.id))
            is FavoritesUiEvent.SeriesClicked ->
                emitEffect(FavoritesSideEffect.NavigateToSeriesDetail(event.id))
            is FavoritesUiEvent.ActorClicked ->
                emitEffect(FavoritesSideEffect.NavigateToActorDetail(event.id))
            FavoritesUiEvent.DiscoverClicked -> emitEffect(FavoritesSideEffect.NavigateToDiscover)
            FavoritesUiEvent.SeeAllMoviesClicked ->
                emitEffect(FavoritesSideEffect.NavigateToSeeAllMovies)
            FavoritesUiEvent.SeeAllSeriesClicked ->
                emitEffect(FavoritesSideEffect.NavigateToSeeAllSeries)
            FavoritesUiEvent.SeeAllActorsClicked ->
                emitEffect(FavoritesSideEffect.NavigateToSeeAllActors)
        }
    }

    private fun emitEffect(effect: FavoritesSideEffect) {
        viewModelScope.launch { _effectChannel.send(effect) }
    }
}
