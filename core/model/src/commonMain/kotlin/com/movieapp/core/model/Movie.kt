package com.movieapp.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    val id: Int,
    val title: String = "",
    val overview: String = "",
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val releaseDate: String? = null,
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
    val popularity: Double = 0.0,
    val adult: Boolean = false,
    val originalLanguage: String = "",
    val originalTitle: String = "",
    val video: Boolean = false,
    val genreIds: List<Int> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val runtime: Int? = null,
    val status: String? = null,
    val tagline: String? = null,
    val budget: Long? = null,
    val revenue: Long? = null,
    val homepage: String? = null,
    val imdbId: String? = null,
    val productionCompanies: List<ProductionCompany> = emptyList(),
    val productionCountries: List<ProductionCountry> = emptyList(),
    val spokenLanguages: List<SpokenLanguage> = emptyList(),
    val belongsToCollection: MovieCollection? = null,
    val videos: VideoResponse? = null,
    val images: ImageResponse? = null,
    val credits: Credits? = null,
    val similar: PagedResponse<Movie>? = null,
    val recommendations: PagedResponse<Movie>? = null,
    val reviews: PagedResponse<Review>? = null,
    val keywords: List<Keyword> = emptyList(),
    val externalIds: ExternalIds? = null,
    val watchProviders: WatchProviders? = null,
    /** User score on account "rated" lists (TMDB 0.5–10); absent on most other responses. */
    @SerialName("rating") val accountRating: Double? = null,
)

@Serializable data class Genre(val id: Int, val name: String)

@Serializable
data class ProductionCompany(
    val id: Int = 0,
    val name: String = "",
    val logoPath: String? = null,
    val originCountry: String = "",
)

@Serializable
data class ProductionCountry(@SerialName("iso_3166_1") val iso31661: String, val name: String)

@Serializable
data class SpokenLanguage(
    val englishName: String,
    @SerialName("iso_639_1") val iso6391: String,
    val name: String,
)

@Serializable
data class MovieCollection(
    val id: Int = 0,
    val name: String = "",
    val posterPath: String? = null,
    val backdropPath: String? = null,
)

@Serializable
data class VideoResponse(val results: List<Video> = emptyList())

@Serializable
data class Video(
    val id: String = "",
    val key: String = "",
    val name: String = "",
    val site: String = "",
    val size: Int = 0,
    val type: String = "",
    val official: Boolean = false,
    val publishedAt: String? = null,
)

@Serializable
data class ImageResponse(
    val backdrops: List<Image> = emptyList(),
    val logos: List<Image> = emptyList(),
    val posters: List<Image> = emptyList(),
)

@Serializable
data class Image(
    val aspectRatio: Double = 0.0,
    val height: Int = 0,
    @SerialName("iso_639_1") val iso6391: String? = null,
    val filePath: String = "",
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
    val width: Int = 0,
)

@Serializable
data class Credits(
    val cast: List<CastMember> = emptyList(),
    val crew: List<CrewMember> = emptyList(),
)

@Serializable
data class CastMember(
    val id: Int = 0,
    val name: String = "",
    val character: String = "",
    val order: Int = 0,
    val profilePath: String? = null,
    val adult: Boolean = false,
    val gender: Int = 0,
    val knownForDepartment: String = "",
    val originalName: String = "",
    val popularity: Double = 0.0,
    val castId: Int = 0,
    val creditId: String = "",
)

@Serializable
data class CrewMember(
    val id: Int = 0,
    val name: String = "",
    val job: String = "",
    val department: String = "",
    val profilePath: String? = null,
    val adult: Boolean = false,
    val gender: Int = 0,
    val knownForDepartment: String = "",
    val originalName: String = "",
    val popularity: Double = 0.0,
    val creditId: String = "",
)

@Serializable
data class Review(
    val id: String,
    val author: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val url: String,
    val authorDetails: AuthorDetails,
)

@Serializable
data class AuthorDetails(
    val name: String = "",
    val username: String = "",
    val avatarPath: String? = null,
    val rating: Double? = null,
)

@Serializable data class Keyword(val id: Int, val name: String)

@Serializable
data class ExternalIds(
    val imdbId: String? = null,
    val facebookId: String? = null,
    val instagramId: String? = null,
    val twitterId: String? = null,
    val id: Int = 0,
)

@Serializable
data class WatchProviders(val results: Map<String, WatchProviderCountry>)

@Serializable
data class WatchProviderCountry(
    val link: String,
    val flatrate: List<WatchProvider> = emptyList(),
    val rent: List<WatchProvider> = emptyList(),
    val buy: List<WatchProvider> = emptyList(),
)

@Serializable
data class WatchProvider(
    val displayPriority: Int,
    val logoPath: String,
    val providerId: Int,
    val providerName: String,
)

@Serializable data class WatchProviderCatalog(val results: List<WatchProvider> = emptyList())

@Serializable
data class PagedResponse<T>(
    val page: Int = 1,
    val results: List<T> = emptyList(),
    val totalPages: Int = 0,
    val totalResults: Int = 0,
)
