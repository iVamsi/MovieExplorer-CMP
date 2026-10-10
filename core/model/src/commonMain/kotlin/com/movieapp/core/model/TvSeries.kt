package com.movieapp.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TvSeries(
    val id: Int = 0,
    val name: String = "",
    val overview: String = "",
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val firstAirDate: String? = null,
    val lastAirDate: String? = null,
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
    val popularity: Double = 0.0,
    val adult: Boolean = false,
    val originalLanguage: String = "",
    val originalName: String = "",
    val originCountry: List<String> = emptyList(),
    val genreIds: List<Int> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val episodeRunTime: List<Int> = emptyList(),
    val numberOfEpisodes: Int? = null,
    val numberOfSeasons: Int? = null,
    val status: String? = null,
    val tagline: String? = null,
    val type: String? = null,
    val homepage: String? = null,
    val inProduction: Boolean? = null,
    val languages: List<String> = emptyList(),
    val networks: List<Network> = emptyList(),
    val productionCompanies: List<ProductionCompany> = emptyList(),
    val productionCountries: List<ProductionCountry> = emptyList(),
    val spokenLanguages: List<SpokenLanguage> = emptyList(),
    val seasons: List<Season> = emptyList(),
    val videos: VideoResponse? = null,
    val images: ImageResponse? = null,
    val credits: Credits? = null,
    val similar: PagedResponse<TvSeries>? = null,
    val recommendations: PagedResponse<TvSeries>? = null,
    val reviews: PagedResponse<Review>? = null,
    val keywords: List<Keyword> = emptyList(),
    val externalIds: ExternalIds? = null,
    val watchProviders: WatchProviders? = null,
    val createdBy: List<Creator> = emptyList(),
    val lastEpisodeToAir: Episode? = null,
    val nextEpisodeToAir: Episode? = null,
    /** User score on account "rated" lists (TMDB 0.5–10). */
    @SerialName("rating") val accountRating: Double? = null,
)

@Serializable
data class Network(
    val id: Int = 0,
    val name: String = "",
    val logoPath: String? = null,
    val originCountry: String = "",
)

@Serializable
data class Season(
    val id: Int = 0,
    val name: String = "",
    val overview: String = "",
    val posterPath: String? = null,
    val seasonNumber: Int = 0,
    val airDate: String? = null,
    val episodeCount: Int = 0,
    val voteAverage: Double = 0.0,
)

@Serializable
data class Creator(
    val id: Int = 0,
    val name: String = "",
    val profilePath: String? = null,
    val creditId: String = "",
)

@Serializable
data class Episode(
    val id: Int = 0,
    val name: String = "",
    val overview: String = "",
    val airDate: String? = null,
    val episodeNumber: Int = 0,
    val seasonNumber: Int = 0,
    val stillPath: String? = null,
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
    val runtime: Int? = null,
)
