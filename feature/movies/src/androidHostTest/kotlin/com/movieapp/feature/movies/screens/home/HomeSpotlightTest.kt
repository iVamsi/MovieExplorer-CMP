package com.movieapp.feature.movies.screens.home

import com.google.common.truth.Truth.assertThat
import com.movieapp.core.model.Movie
import com.movieapp.core.model.TvSeries
import org.junit.Test

class HomeSpotlightTest {
    private val movie = Movie(id = 7, title = "Movie", backdropPath = "/movie.jpg")
    private val series = TvSeries(id = 7, name = "Series", backdropPath = "/series.jpg")

    @Test
    fun selectSpotlightAllfallbackusespopularmoviebeforeTV() {
        assertThat(selectSpotlight(HomeCategory.ALL, emptyList(), listOf(movie), listOf(series)))
            .isEqualTo(SpotlightItem.MovieItem(movie))
    }

    @Test
    fun selectSpotlightMovieswithoutmoviesexcludesTV() {
        assertThat(selectSpotlight(HomeCategory.MOVIES, emptyList(), emptyList(), listOf(series)))
            .isNull()
    }

    @Test
    fun selectSpotlightpagingorderchangespreservesexistingidentity() {
        val second = movie.copy(id = 8)
        assertThat(
                selectSpotlight(
                    HomeCategory.ALL,
                    listOf(second, movie),
                    emptyList(),
                    emptyList(),
                    SpotlightKey.Movie(7),
                )
            )
            .isEqualTo(SpotlightItem.MovieItem(movie))
    }

    @Test
    fun selectSpotlightduplicatemovieandTVidspreservesmediatype() {
        assertThat(
                selectSpotlight(
                    HomeCategory.ALL,
                    listOf(movie),
                    emptyList(),
                    listOf(series),
                    SpotlightKey.Series(7),
                )
            )
            .isEqualTo(SpotlightItem.SeriesItem(series))
    }

    @Test
    fun selectSpotlightclearedidentityafterrefreshreselectscurrentfirstitem() {
        val refreshed = movie.copy(id = 9)
        assertThat(
                selectSpotlight(
                    HomeCategory.MOVIES,
                    listOf(refreshed, movie),
                    emptyList(),
                    emptyList(),
                )
            )
            .isEqualTo(SpotlightItem.MovieItem(refreshed))
    }

    @Test
    fun selectSpotlightTVcategoryselectsonlyaseries() {
        assertThat(selectSpotlight(HomeCategory.TV, listOf(movie), emptyList(), listOf(series)))
            .isEqualTo(SpotlightItem.SeriesItem(series))
    }

    @Test
    fun selectSpotlightexistingmoviemissingafterrefreshchoosesrefreshedmovie() {
        val refreshed = movie.copy(id = 9)

        assertThat(
                selectSpotlight(
                    category = HomeCategory.MOVIES,
                    nowPlaying = listOf(refreshed),
                    popularMovies = emptyList(),
                    trendingSeries = emptyList(),
                    existingKey = SpotlightKey.Movie(movie.id),
                )
            )
            .isEqualTo(SpotlightItem.MovieItem(refreshed))
    }
}
