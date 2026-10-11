package com.movieapp.data.movies.paging

import com.google.common.truth.Truth.assertThat
import com.movieapp.core.model.Actor
import com.movieapp.core.model.Movie
import com.movieapp.core.model.MultiSearchItem
import com.movieapp.core.model.MultiSearchListRow
import com.movieapp.core.model.TvSeries
import org.junit.Test

class MultiSearchItemMapperTest {

    @Test
    fun `toMultiSearchListRow - movie media type - returns MovieRow`() {
        val item =
            MultiSearchItem(
                id = 42,
                mediaType = "movie",
                title = "Fight Club",
                overview = "An insomniac office worker.",
                posterPath = "/poster.jpg",
                releaseDate = "1999-10-15",
                voteAverage = 8.4,
                voteCount = 27_000,
                popularity = 99.5,
                originalTitle = "Fight Club",
                genreIds = listOf(18),
            )

        val row = item.toMultiSearchListRow()

        assertThat(row).isInstanceOf(MultiSearchListRow.MovieRow::class.java)
        val movie = (row as MultiSearchListRow.MovieRow).movie
        assertThat(movie)
            .isEqualTo(
                Movie(
                    id = 42,
                    title = "Fight Club",
                    overview = "An insomniac office worker.",
                    posterPath = "/poster.jpg",
                    releaseDate = "1999-10-15",
                    voteAverage = 8.4,
                    voteCount = 27_000,
                    popularity = 99.5,
                    originalTitle = "Fight Club",
                    genreIds = listOf(18),
                )
            )
    }

    @Test
    fun `toMultiSearchListRow - tv media type - returns TvRow`() {
        val item =
            MultiSearchItem(
                id = 7,
                mediaType = "TV",
                name = "Breaking Bad",
                overview = "A chemistry teacher turned meth cook.",
                firstAirDate = "2008-01-20",
                originalName = "Breaking Bad",
                originCountry = listOf("US"),
            )

        val row = item.toMultiSearchListRow()

        assertThat(row).isInstanceOf(MultiSearchListRow.TvRow::class.java)
        val series = (row as MultiSearchListRow.TvRow).series
        assertThat(series)
            .isEqualTo(
                TvSeries(
                    id = 7,
                    name = "Breaking Bad",
                    overview = "A chemistry teacher turned meth cook.",
                    firstAirDate = "2008-01-20",
                    originalName = "Breaking Bad",
                    originCountry = listOf("US"),
                )
            )
    }

    @Test
    fun `toMultiSearchListRow - person media type - returns PersonRow`() {
        val item =
            MultiSearchItem(
                id = 3,
                mediaType = "person",
                name = "Leonardo DiCaprio",
                profilePath = "/profile.jpg",
                knownForDepartment = "Acting",
                gender = 2,
                popularity = 85.2,
                originalName = "Leonardo DiCaprio",
            )

        val row = item.toMultiSearchListRow()

        assertThat(row).isInstanceOf(MultiSearchListRow.PersonRow::class.java)
        val actor = (row as MultiSearchListRow.PersonRow).actor
        assertThat(actor)
            .isEqualTo(
                Actor(
                    id = 3,
                    name = "Leonardo DiCaprio",
                    profilePath = "/profile.jpg",
                    knownForDepartment = "Acting",
                    gender = 2,
                    popularity = 85.2,
                    originalName = "Leonardo DiCaprio",
                )
            )
    }

    @Test
    fun `toMultiSearchListRow - unknown media type - returns null`() {
        val item = MultiSearchItem(id = 1, mediaType = "company", title = "Pixar")

        assertThat(item.toMultiSearchListRow()).isNull()
    }

    @Test
    fun `toMultiSearchListRow - missing optional strings - uses empty defaults`() {
        val item = MultiSearchItem(id = 9, mediaType = "movie")

        val movie = (item.toMultiSearchListRow() as MultiSearchListRow.MovieRow).movie

        assertThat(movie.title).isEmpty()
        assertThat(movie.overview).isEmpty()
        assertThat(movie.originalTitle).isEmpty()
    }
}
