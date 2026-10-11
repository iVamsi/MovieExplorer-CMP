package com.movieapp.feature.movies.screens.browseall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.movieapp.core.domain.repository.MovieRepository
import com.movieapp.core.domain.repository.SeriesRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class BrowseAllViewModel(
    category: String,
    private val movieRepository: MovieRepository,
    private val seriesRepository: SeriesRepository,
) : ViewModel() {

    private val category: ContentCategory =
        ContentCategory.valueOf(category)

    private val _uiState = MutableStateFlow(BrowseAllUiState(category = this.category))
    val uiState: StateFlow<BrowseAllUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<BrowseAllSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    val items: Flow<PagingData<BrowseItem>> =
        when (this.category) {
            ContentCategory.NOW_PLAYING ->
                movieRepository.getNowPlayingMoviesPaged().map { pagingData ->
                    pagingData.map { movie -> BrowseItem.MovieItem(movie) as BrowseItem }
                }

            ContentCategory.POPULAR_MOVIES ->
                movieRepository.getPopularMoviesPaged().map { pagingData ->
                    pagingData.map { movie -> BrowseItem.MovieItem(movie) as BrowseItem }
                }

            ContentCategory.TOP_RATED_MOVIES ->
                movieRepository.getTopRatedMoviesPaged().map { pagingData ->
                    pagingData.map { movie -> BrowseItem.MovieItem(movie) as BrowseItem }
                }

            ContentCategory.TRENDING_SERIES ->
                seriesRepository.getTrendingSeries().map { pagingData ->
                    pagingData.map { series -> BrowseItem.SeriesItem(series) as BrowseItem }
                }

            ContentCategory.POPULAR_SERIES ->
                seriesRepository.getPopularSeries().map { pagingData ->
                    pagingData.map { series -> BrowseItem.SeriesItem(series) as BrowseItem }
                }

            ContentCategory.TOP_RATED_SERIES ->
                seriesRepository.getTopRatedSeries().map { pagingData ->
                    pagingData.map { series -> BrowseItem.SeriesItem(series) as BrowseItem }
                }
        }.cachedIn(viewModelScope)

    fun onEvent(event: BrowseAllUiEvent) {
        when (event) {
            is BrowseAllUiEvent.MovieClicked ->
                viewModelScope.launch {
                    _sideEffect.send(BrowseAllSideEffect.NavigateToMovieDetail(event.movieId))
                }

            is BrowseAllUiEvent.SeriesClicked ->
                viewModelScope.launch {
                    _sideEffect.send(BrowseAllSideEffect.NavigateToSeriesDetail(event.seriesId))
                }

            BrowseAllUiEvent.BackClicked ->
                viewModelScope.launch { _sideEffect.send(BrowseAllSideEffect.NavigateBack) }
        }
    }
}
