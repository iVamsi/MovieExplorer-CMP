package com.movieapp.feature.movies.screens.watchlist

import com.movieapp.feature.movies.resources.Res
import com.movieapp.feature.movies.resources.sort_alphabetical_az
import com.movieapp.feature.movies.resources.sort_alphabetical_za
import com.movieapp.feature.movies.resources.sort_recently_added
import org.jetbrains.compose.resources.StringResource
import com.movieapp.core.model.Movie
import com.movieapp.core.model.TvSeries

enum class WatchlistSortOption(val titleRes: StringResource) {
    DEFAULT(Res.string.sort_recently_added),
    ALPHABETICAL(Res.string.sort_alphabetical_az),
    REVERSE_ALPHABETICAL(Res.string.sort_alphabetical_za),
}

data class WatchlistUiState(
    val watchlistMovies: List<Movie> = emptyList(),
    val watchlistTvSeries: List<TvSeries> = emptyList(),
    val currentSortOption: WatchlistSortOption = WatchlistSortOption.DEFAULT,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface WatchlistUiEvent {
    data class SortOptionSelected(val option: WatchlistSortOption) : WatchlistUiEvent

    data class MovieClicked(val id: Int) : WatchlistUiEvent

    data class SeriesClicked(val id: Int) : WatchlistUiEvent

    object DiscoverClicked : WatchlistUiEvent

    object SeeAllMoviesClicked : WatchlistUiEvent

    object SeeAllSeriesClicked : WatchlistUiEvent
}

sealed interface WatchlistSideEffect {
    data class NavigateToMovieDetail(val id: Int) : WatchlistSideEffect

    data class NavigateToSeriesDetail(val id: Int) : WatchlistSideEffect

    object NavigateToDiscover : WatchlistSideEffect

    object NavigateToSeeAllMovies : WatchlistSideEffect

    object NavigateToSeeAllSeries : WatchlistSideEffect
}
