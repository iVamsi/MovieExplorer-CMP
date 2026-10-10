package com.movieapp.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    val movies: PagedResponse<Movie>? = null,
    val tvSeries: PagedResponse<TvSeries>? = null,
    val actors: PagedResponse<Actor>? = null,
    val companies: PagedResponse<Company>? = null,
    val collections: PagedResponse<MovieCollection>? = null,
    val keywords: PagedResponse<Keyword>? = null,
)

@Serializable
data class Company(
    val id: Int = 0,
    val name: String = "",
    val logoPath: String? = null,
    val originCountry: String = "",
)

@Serializable
data class SearchQuery(
    val query: String,
    val includeAdult: Boolean = false,
    val language: String = "en-US",
    val page: Int = 1,
    val region: String? = null,
    val year: Int? = null,
    val primaryReleaseYear: Int? = null,
    val firstAirDateYear: Int? = null,
)

@Serializable
data class MultiSearchResult(
    val page: Int = 1,
    val results: List<MultiSearchItem> = emptyList(),
    val totalPages: Int = 0,
    val totalResults: Int = 0,
)

/** TMDB [/search/multi] row mapped into domain models for a single paged list. */
sealed class MultiSearchListRow {
    data class MovieRow(val movie: Movie) : MultiSearchListRow()

    data class TvRow(val series: TvSeries) : MultiSearchListRow()

    data class PersonRow(val actor: Actor) : MultiSearchListRow()
}

@Serializable
data class MultiSearchItem(
    val id: Int = 0,
    val mediaType: String = "",
    val title: String? = null,
    val name: String? = null,
    val overview: String? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val releaseDate: String? = null,
    val firstAirDate: String? = null,
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
    val popularity: Double = 0.0,
    val adult: Boolean = false,
    val originalLanguage: String = "",
    val originalTitle: String? = null,
    val originalName: String? = null,
    val genreIds: List<Int> = emptyList(),
    val originCountry: List<String> = emptyList(),
    val profilePath: String? = null,
    val knownFor: List<MultiSearchItem>? = null,
    val knownForDepartment: String? = null,
    val gender: Int? = null,
)

@Serializable
enum class SearchFilter {
    ALL,
    MOVIES,
    TV_SERIES,
    ACTORS,
    COMPANIES,
    COLLECTIONS,
    KEYWORDS,
}

@Serializable
enum class SortOrder {
    POPULARITY_ASC,
    POPULARITY_DESC,
    RELEASE_DATE_ASC,
    RELEASE_DATE_DESC,
    VOTE_AVERAGE_ASC,
    VOTE_AVERAGE_DESC,
    TITLE_ASC,
    TITLE_DESC,
    ORIGINAL_TITLE_ASC,
    ORIGINAL_TITLE_DESC,
}
