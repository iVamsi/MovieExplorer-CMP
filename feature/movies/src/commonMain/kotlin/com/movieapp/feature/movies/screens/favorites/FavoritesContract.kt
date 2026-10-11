package com.movieapp.feature.movies.screens.favorites

import com.movieapp.feature.movies.resources.Res
import com.movieapp.feature.movies.resources.sort_alphabetical_az
import com.movieapp.feature.movies.resources.sort_alphabetical_za
import com.movieapp.feature.movies.resources.sort_recently_added
import org.jetbrains.compose.resources.StringResource
import com.movieapp.core.model.Actor
import com.movieapp.core.model.Movie
import com.movieapp.core.model.TvSeries

enum class FavoritesSortOption(val titleRes: StringResource) {
    DEFAULT(Res.string.sort_recently_added),
    ALPHABETICAL(Res.string.sort_alphabetical_az),
    REVERSE_ALPHABETICAL(Res.string.sort_alphabetical_za),
}

data class FavoritesUiState(
    val favoriteMovies: List<Movie> = emptyList(),
    val favoriteTvSeries: List<TvSeries> = emptyList(),
    val favoriteActors: List<Actor> = emptyList(),
    val currentSortOption: FavoritesSortOption = FavoritesSortOption.DEFAULT,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface FavoritesUiEvent {
    data class SortOptionSelected(val option: FavoritesSortOption) : FavoritesUiEvent

    data class MovieClicked(val id: Int) : FavoritesUiEvent

    data class SeriesClicked(val id: Int) : FavoritesUiEvent

    data class ActorClicked(val id: Int) : FavoritesUiEvent

    data object DiscoverClicked : FavoritesUiEvent

    object SeeAllMoviesClicked : FavoritesUiEvent

    object SeeAllSeriesClicked : FavoritesUiEvent

    object SeeAllActorsClicked : FavoritesUiEvent
}

sealed interface FavoritesSideEffect {
    data class NavigateToMovieDetail(val id: Int) : FavoritesSideEffect

    data class NavigateToSeriesDetail(val id: Int) : FavoritesSideEffect

    data class NavigateToActorDetail(val id: Int) : FavoritesSideEffect

    data object NavigateToDiscover : FavoritesSideEffect

    object NavigateToSeeAllMovies : FavoritesSideEffect

    object NavigateToSeeAllSeries : FavoritesSideEffect

    object NavigateToSeeAllActors : FavoritesSideEffect
}
