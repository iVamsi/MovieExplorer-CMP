package com.movieapp.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Actor(
    val id: Int = 0,
    val name: String = "",
    val biography: String? = null,
    val birthday: String? = null,
    val deathday: String? = null,
    val placeOfBirth: String? = null,
    val profilePath: String? = null,
    val adult: Boolean = false,
    val gender: Int = 0,
    val knownForDepartment: String = "",
    val originalName: String = "",
    val popularity: Double = 0.0,
    val alsoKnownAs: List<String> = emptyList(),
    val images: ActorImageResponse? = null,
    val movieCredits: ActorMovieCredits? = null,
    val tvCredits: ActorTvCredits? = null,
    val externalIds: ExternalIds? = null,
    val taggedImages: PagedResponse<TaggedImage>? = null,
)

@Serializable
data class ActorImageResponse(val profiles: List<ActorImage> = emptyList())

@Serializable
data class ActorImage(
    val aspectRatio: Double = 0.0,
    val height: Int = 0,
    @SerialName("iso_639_1") val iso6391: String?,
    val filePath: String = "",
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
    val width: Int = 0,
)

@Serializable
data class ActorMovieCredits(
    val cast: List<ActorMovieCast> = emptyList(),
    val crew: List<ActorMovieCrew> = emptyList(),
)

@Serializable
data class ActorMovieCast(
    val id: Int = 0,
    val title: String = "",
    val character: String = "",
    val order: Int = 0,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val adult: Boolean = false,
    val genreIds: List<Int> = emptyList(),
    val originalLanguage: String = "",
    val originalTitle: String = "",
    val overview: String = "",
    val popularity: Double = 0.0,
    val releaseDate: String? = null,
    val video: Boolean = false,
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
    val creditId: String = "",
)

@Serializable
data class ActorMovieCrew(
    val id: Int = 0,
    val title: String = "",
    val job: String = "",
    val department: String = "",
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val adult: Boolean = false,
    val genreIds: List<Int> = emptyList(),
    val originalLanguage: String = "",
    val originalTitle: String = "",
    val overview: String = "",
    val popularity: Double = 0.0,
    val releaseDate: String? = null,
    val video: Boolean = false,
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
    val creditId: String = "",
)

@Serializable
data class ActorTvCredits(
    val cast: List<ActorTvCast> = emptyList(),
    val crew: List<ActorTvCrew> = emptyList(),
)

@Serializable
data class ActorTvCast(
    val id: Int = 0,
    val name: String = "",
    val character: String = "",
    val order: Int = 0,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val adult: Boolean = false,
    val genreIds: List<Int> = emptyList(),
    val originalLanguage: String = "",
    val originalName: String = "",
    val overview: String = "",
    val popularity: Double = 0.0,
    val firstAirDate: String? = null,
    val originCountry: List<String> = emptyList(),
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
    val creditId: String = "",
)

@Serializable
data class ActorTvCrew(
    val id: Int = 0,
    val name: String = "",
    val job: String = "",
    val department: String = "",
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val adult: Boolean = false,
    val genreIds: List<Int> = emptyList(),
    val originalLanguage: String = "",
    val originalName: String = "",
    val overview: String = "",
    val popularity: Double = 0.0,
    val firstAirDate: String? = null,
    val originCountry: List<String> = emptyList(),
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
    val creditId: String = "",
)

@Serializable
data class TaggedImage(
    val aspectRatio: Double = 0.0,
    val height: Int = 0,
    @SerialName("iso_639_1") val iso6391: String? = null,
    val filePath: String = "",
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
    val width: Int = 0,
    val imageType: String = "",
    val media: TaggedImageMedia =
        TaggedImageMedia(
            id = 0,
            title = null,
            name = null,
            posterPath = null,
            backdropPath = null,
            voteAverage = 0.0,
            voteCount = 0,
            mediaType = "",
        ),
    val mediaType: String = "",
)

@Serializable
data class TaggedImageMedia(
    val id: Int = 0,
    val title: String? = null,
    val name: String? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
    val mediaType: String = "",
)

@Serializable
data class TaggedImageResponse(val results: List<TaggedImage> = emptyList())
