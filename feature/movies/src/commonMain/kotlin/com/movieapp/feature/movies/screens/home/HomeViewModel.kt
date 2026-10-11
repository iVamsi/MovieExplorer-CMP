package com.movieapp.feature.movies.screens.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.model.Movie
import com.movieapp.core.model.TvSeries
import com.movieapp.feature.movies.screens.browseall.ContentCategory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

enum class HomeCategory {
    ALL,
    MOVIES,
    TV,
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val category: HomeCategory = HomeCategory.ALL,
)

sealed interface HomeUiEvent {
    data object Refresh : HomeUiEvent

    data class MovieClicked(val movieId: Int) : HomeUiEvent

    data class SeriesClicked(val seriesId: Int) : HomeUiEvent

    data class SeeAllClicked(val category: ContentCategory) : HomeUiEvent

    data class CategorySelected(val category: HomeCategory) : HomeUiEvent
}

sealed interface HomeSideEffect {
    data class NavigateToMovieDetail(val movieId: Int) : HomeSideEffect

    data class NavigateToSeriesDetail(val seriesId: Int) : HomeSideEffect

    data class NavigateToSeeAll(val category: ContentCategory) : HomeSideEffect
}

class HomeViewModel(
    private val movieRepository: MovieRepository,
    private val seriesRepository: SeriesRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            HomeUiState(category = savedStateHandle[HOME_CATEGORY_KEY] ?: HomeCategory.ALL)
        )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<HomeSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    val nowPlayingMovies: Flow<PagingData<Movie>> =
        movieRepository.getNowPlayingMoviesPaged().cachedIn(viewModelScope)

    val popularMovies: Flow<PagingData<Movie>> =
        movieRepository.getPopularMoviesPaged().cachedIn(viewModelScope)

    val topRatedMovies: Flow<PagingData<Movie>> =
        movieRepository.getTopRatedMoviesPaged().cachedIn(viewModelScope)

    val trendingSeries: Flow<PagingData<TvSeries>> =
        seriesRepository.getTrendingSeries().cachedIn(viewModelScope)

    val popularSeries: Flow<PagingData<TvSeries>> =
        seriesRepository.getPopularSeries().cachedIn(viewModelScope)

    val topRatedSeries: Flow<PagingData<TvSeries>> =
        seriesRepository.getTopRatedSeries().cachedIn(viewModelScope)

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.Refresh -> {
                // UI will trigger PagingData.refresh() on each LazyPagingItems
            }

            is HomeUiEvent.MovieClicked -> {
                viewModelScope.launch {
                    _sideEffect.send(HomeSideEffect.NavigateToMovieDetail(event.movieId))
                }
            }

            is HomeUiEvent.SeriesClicked -> {
                viewModelScope.launch {
                    _sideEffect.send(HomeSideEffect.NavigateToSeriesDetail(event.seriesId))
                }
            }

            is HomeUiEvent.SeeAllClicked -> {
                viewModelScope.launch {
                    _sideEffect.send(HomeSideEffect.NavigateToSeeAll(event.category))
                }
            }

            is HomeUiEvent.CategorySelected -> {
                savedStateHandle[HOME_CATEGORY_KEY] = event.category
                _uiState.value = _uiState.value.copy(category = event.category)
            }
        }
    }

    private companion object {
        const val HOME_CATEGORY_KEY = "home_category"
    }
}
