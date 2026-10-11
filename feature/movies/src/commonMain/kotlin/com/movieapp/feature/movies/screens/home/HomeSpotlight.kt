package com.movieapp.feature.movies.screens.home

import com.movieapp.core.model.Movie
import com.movieapp.core.model.TvSeries

internal sealed interface SpotlightItem {
    val key: SpotlightKey
    val backdropPath: String

    data class MovieItem(val movie: Movie) : SpotlightItem {
        override val key = SpotlightKey.Movie(movie.id)
        override val backdropPath = movie.backdropPath.orEmpty()
    }

    data class SeriesItem(val series: TvSeries) : SpotlightItem {
        override val key = SpotlightKey.Series(series.id)
        override val backdropPath = series.backdropPath.orEmpty()
    }
}

internal sealed interface SpotlightKey {
    data class Movie(val id: Int) : SpotlightKey

    data class Series(val id: Int) : SpotlightKey
}

internal fun selectSpotlight(
    category: HomeCategory,
    nowPlaying: List<Movie>,
    popularMovies: List<Movie>,
    trendingSeries: List<TvSeries>,
    existingKey: SpotlightKey? = null,
): SpotlightItem? {
    val movies = (nowPlaying + popularMovies).filter { it.backdropPath != null }
    val series = trendingSeries.filter { it.backdropPath != null }
    val existing =
        when (existingKey) {
            is SpotlightKey.Movie ->
                movies.firstOrNull { it.id == existingKey.id }?.let(SpotlightItem::MovieItem)
            is SpotlightKey.Series ->
                series.firstOrNull { it.id == existingKey.id }?.let(SpotlightItem::SeriesItem)
            null -> null
        }
    val existingMatchesCategory =
        when (category) {
            HomeCategory.ALL -> true
            HomeCategory.MOVIES -> existing is SpotlightItem.MovieItem
            HomeCategory.TV -> existing is SpotlightItem.SeriesItem
        }
    if (existing != null && existingMatchesCategory) {
        return existing
    }
    return when (category) {
        HomeCategory.ALL ->
            movies.firstOrNull()?.let(SpotlightItem::MovieItem)
                ?: series.firstOrNull()?.let(SpotlightItem::SeriesItem)
        HomeCategory.MOVIES -> movies.firstOrNull()?.let(SpotlightItem::MovieItem)
        HomeCategory.TV -> series.firstOrNull()?.let(SpotlightItem::SeriesItem)
    }
}
