package com.movieapp.feature.movies.screens.browseall

import com.movieapp.feature.movies.resources.Res
import com.movieapp.feature.movies.resources.home_section_now_playing
import com.movieapp.feature.movies.resources.home_section_popular_movies
import com.movieapp.feature.movies.resources.home_section_popular_series
import com.movieapp.feature.movies.resources.home_section_top_rated_movies
import com.movieapp.feature.movies.resources.home_section_top_rated_series
import com.movieapp.feature.movies.resources.home_section_trending_series
import org.jetbrains.compose.resources.StringResource
import com.movieapp.core.model.Movie
import com.movieapp.core.model.TvSeries

enum class ContentCategory(val displayTitle: StringResource, val isSeries: Boolean = false) {
    NOW_PLAYING(Res.string.home_section_now_playing),
    POPULAR_MOVIES(Res.string.home_section_popular_movies),
    TOP_RATED_MOVIES(Res.string.home_section_top_rated_movies),
    TRENDING_SERIES(Res.string.home_section_trending_series, isSeries = true),
    POPULAR_SERIES(Res.string.home_section_popular_series, isSeries = true),
    TOP_RATED_SERIES(Res.string.home_section_top_rated_series, isSeries = true),
}

sealed interface BrowseItem {
    data class MovieItem(val movie: Movie) : BrowseItem

    data class SeriesItem(val series: TvSeries) : BrowseItem
}

data class BrowseAllUiState(
    val category: ContentCategory = ContentCategory.NOW_PLAYING,
    val error: String? = null,
)

sealed interface BrowseAllUiEvent {
    data class MovieClicked(val movieId: Int) : BrowseAllUiEvent

    data class SeriesClicked(val seriesId: Int) : BrowseAllUiEvent

    data object BackClicked : BrowseAllUiEvent
}

sealed interface BrowseAllSideEffect {
    data class NavigateToMovieDetail(val movieId: Int) : BrowseAllSideEffect

    data class NavigateToSeriesDetail(val seriesId: Int) : BrowseAllSideEffect

    data object NavigateBack : BrowseAllSideEffect
}
