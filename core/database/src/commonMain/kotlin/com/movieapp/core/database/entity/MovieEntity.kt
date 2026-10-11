package com.movieapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.movieapp.core.database.converter.ListConverter
import com.movieapp.core.database.converter.MapConverter
import com.movieapp.core.database.converter.ModelConverters
import com.movieapp.core.model.Genre
import com.movieapp.core.model.VideoResponse

@Entity(tableName = "movies")
@TypeConverters(
    ListConverter::class,
    MapConverter::class,
    ModelConverters::class,
)
data class MovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val popularity: Double,
    val adult: Boolean,
    val originalLanguage: String,
    val originalTitle: String,
    val video: Boolean,
    val genreIds: List<Int>,
    val genres: List<Genre>?, // Converted via ModelConverters
    val runtime: Int?,
    val status: String?,
    val tagline: String?,
    val budget: Long?,
    val revenue: Long?,
    val homepage: String?,
    val imdbId: String?,
    val productionCompanies: String?, // JSON string
    val productionCountries: String?, // JSON string
    val spokenLanguages: String?, // JSON string
    val belongsToCollection: String?, // JSON string
    val videos: VideoResponse?, // Converted via ModelConverters
    val images: String?, // JSON string
    val credits: String?, // JSON string
    val similar: String?, // JSON string
    val recommendations: String?, // JSON string
    val reviews: String?, // JSON string
    val keywords: String?, // JSON string
    val externalIds: String?, // JSON string
    val watchProviders: String?, // JSON string
    val cachedAt: Long,
)

@Entity(tableName = "tv_series")
@TypeConverters(
    ListConverter::class,
    MapConverter::class,
    ModelConverters::class,
)
data class TvSeriesEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val firstAirDate: String?,
    val lastAirDate: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val popularity: Double,
    val adult: Boolean,
    val originalLanguage: String,
    val originalName: String,
    val originCountry: List<String>,
    val genreIds: List<Int>,
    val genres: List<Genre>?, // Converted via ModelConverters
    val episodeRunTime: List<Int>,
    val numberOfEpisodes: Int?,
    val numberOfSeasons: Int?,
    val status: String?,
    val tagline: String?,
    val type: String?,
    val homepage: String?,
    val inProduction: Boolean?,
    val languages: List<String>,
    val networks: String?, // JSON string
    val productionCompanies: String?, // JSON string
    val productionCountries: String?, // JSON string
    val spokenLanguages: String?, // JSON string
    val seasons: String?, // JSON string
    val videos: VideoResponse?, // Converted via ModelConverters
    val images: String?, // JSON string
    val credits: String?, // JSON string
    val similar: String?, // JSON string
    val recommendations: String?, // JSON string
    val reviews: String?, // JSON string
    val keywords: String?, // JSON string
    val externalIds: String?, // JSON string
    val watchProviders: String?, // JSON string
    val createdBy: String?, // JSON string
    val lastEpisodeToAir: String?, // JSON string
    val nextEpisodeToAir: String?, // JSON string
    val cachedAt: Long,
    val category: String? = null,
)

@Entity(tableName = "actors")
@TypeConverters(ListConverter::class, MapConverter::class)
data class ActorEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val biography: String?,
    val birthday: String?,
    val deathday: String?,
    val placeOfBirth: String?,
    val profilePath: String?,
    val adult: Boolean,
    val gender: Int,
    val knownForDepartment: String,
    val originalName: String,
    val popularity: Double,
    val alsoKnownAs: List<String>,
    val images: String?, // JSON string
    val movieCredits: String?, // JSON string
    val tvCredits: String?, // JSON string
    val externalIds: String?, // JSON string
    val taggedImages: String?, // JSON string
    val cachedAt: Long,
)

@Entity(tableName = "genres")
data class GenreEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val type: String, // "movie" or "tv"
)

@Entity(tableName = "user_collections")
@TypeConverters(ListConverter::class)
data class UserCollectionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val isPublic: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val itemCount: Int,
    val items: String?, // JSON string
)

@Entity(tableName = "collection_items")
data class CollectionItemEntity(
    @PrimaryKey val id: String,
    val collectionId: String,
    val contentId: Int,
    val contentType: String, // "movie", "tv_series", "actor"
    val addedAt: Long,
    val userRating: Int?,
    val userNotes: String?,
    val isWatched: Boolean,
    val watchedAt: Long?,
)

@Entity(tableName = "watchlist_items")
data class WatchlistItemEntity(
    @PrimaryKey val id: String,
    val contentId: Int,
    val contentType: String, // "movie", "tv_series", "actor"
    val addedAt: Long,
    val priority: Int,
)

@Entity(tableName = "favorite_items")
data class FavoriteItemEntity(
    @PrimaryKey val id: String,
    val contentId: Int,
    val contentType: String, // "movie", "tv_series", "actor"
    val addedAt: Long,
)

@Entity(tableName = "user_ratings")
data class UserRatingEntity(
    @PrimaryKey val id: String,
    val contentId: Int,
    val contentType: String, // "movie", "tv_series", "actor"
    val rating: Int,
    val ratedAt: Long,
    val notes: String?,
)
