package com.movieapp.data.movies.mapper

import com.movieapp.core.database.entity.MovieEntity
import com.movieapp.core.model.*
import kotlin.time.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MovieMapper(private val clock: Clock) {

    private val json = Json { ignoreUnknownKeys = true }

    fun mapToEntity(movie: Movie): MovieEntity {
        val now = clock.now().toEpochMilliseconds()
        return MovieEntity(
            id = movie.id,
            title = movie.title,
            overview = movie.overview,
            posterPath = movie.posterPath,
            backdropPath = movie.backdropPath,
            releaseDate = movie.releaseDate,
            voteAverage = movie.voteAverage,
            voteCount = movie.voteCount,
            popularity = movie.popularity,
            adult = movie.adult,
            originalLanguage = movie.originalLanguage,
            originalTitle = movie.originalTitle,
            video = movie.video,
            genreIds = movie.genreIds,
            genres = movie.genres,
            runtime = movie.runtime,
            status = movie.status,
            tagline = movie.tagline,
            budget = movie.budget,
            revenue = movie.revenue,
            homepage = movie.homepage,
            imdbId = movie.imdbId,
            productionCompanies =
                movie.productionCompanies
                    .takeIf { it.isNotEmpty() }
                    ?.let { json.encodeToString(it) },
            productionCountries =
                movie.productionCountries
                    .takeIf { it.isNotEmpty() }
                    ?.let { json.encodeToString(it) },
            spokenLanguages =
                movie.spokenLanguages.takeIf { it.isNotEmpty() }?.let { json.encodeToString(it) },
            belongsToCollection = movie.belongsToCollection?.let { json.encodeToString(it) },
            videos = movie.videos,
            images = movie.images?.let { json.encodeToString(it) },
            credits = movie.credits?.let { json.encodeToString(it) },
            similar = movie.similar?.let { json.encodeToString(it) },
            recommendations = movie.recommendations?.let { json.encodeToString(it) },
            reviews = movie.reviews?.let { json.encodeToString(it) },
            keywords = movie.keywords.takeIf { it.isNotEmpty() }?.let { json.encodeToString(it) },
            externalIds = movie.externalIds?.let { json.encodeToString(it) },
            watchProviders = movie.watchProviders?.let { json.encodeToString(it) },
            cachedAt = now,
        )
    }

    fun mapToDomain(entity: MovieEntity): Movie {
        return Movie(
            id = entity.id,
            title = entity.title,
            overview = entity.overview,
            posterPath = entity.posterPath,
            backdropPath = entity.backdropPath,
            releaseDate = entity.releaseDate,
            voteAverage = entity.voteAverage,
            voteCount = entity.voteCount,
            popularity = entity.popularity,
            adult = entity.adult,
            originalLanguage = entity.originalLanguage,
            originalTitle = entity.originalTitle,
            video = entity.video,
            genreIds = entity.genreIds,
            genres = entity.genres ?: emptyList(),
            runtime = entity.runtime,
            status = entity.status,
            tagline = entity.tagline,
            budget = entity.budget,
            revenue = entity.revenue,
            homepage = entity.homepage,
            imdbId = entity.imdbId,
            productionCompanies =
                entity.productionCompanies?.let {
                    try {
                        json.decodeFromString<List<ProductionCompany>>(it)
                    } catch (e: Exception) {
                        emptyList()
                    }
                } ?: emptyList(),
            productionCountries =
                entity.productionCountries?.let {
                    try {
                        json.decodeFromString<List<ProductionCountry>>(it)
                    } catch (e: Exception) {
                        emptyList()
                    }
                } ?: emptyList(),
            spokenLanguages =
                entity.spokenLanguages?.let {
                    try {
                        json.decodeFromString<List<SpokenLanguage>>(it)
                    } catch (e: Exception) {
                        emptyList()
                    }
                } ?: emptyList(),
            belongsToCollection =
                entity.belongsToCollection?.let {
                    try {
                        json.decodeFromString<com.movieapp.core.model.MovieCollection>(it)
                    } catch (e: Exception) {
                        null
                    }
                },
            videos = entity.videos,
            images =
                entity.images?.let {
                    try {
                        json.decodeFromString<ImageResponse>(it)
                    } catch (e: Exception) {
                        null
                    }
                },
            credits =
                entity.credits?.let {
                    try {
                        json.decodeFromString<Credits>(it)
                    } catch (e: Exception) {
                        null
                    }
                },
            similar =
                entity.similar?.let {
                    try {
                        json.decodeFromString<PagedResponse<Movie>>(it)
                    } catch (e: Exception) {
                        null
                    }
                },
            recommendations =
                entity.recommendations?.let {
                    try {
                        json.decodeFromString<PagedResponse<Movie>>(it)
                    } catch (e: Exception) {
                        null
                    }
                },
            reviews =
                entity.reviews?.let {
                    try {
                        json.decodeFromString<PagedResponse<Review>>(it)
                    } catch (e: Exception) {
                        null
                    }
                },
            keywords =
                entity.keywords?.let {
                    try {
                        json.decodeFromString<List<Keyword>>(it)
                    } catch (e: Exception) {
                        emptyList()
                    }
                } ?: emptyList(),
            externalIds =
                entity.externalIds?.let {
                    try {
                        json.decodeFromString<ExternalIds>(it)
                    } catch (e: Exception) {
                        null
                    }
                },
            watchProviders =
                entity.watchProviders?.let {
                    try {
                        json.decodeFromString<WatchProviders>(it)
                    } catch (e: Exception) {
                        null
                    }
                },
        )
    }

    fun mapToEntityList(movies: List<Movie>): List<MovieEntity> {
        return movies.map { mapToEntity(it) }
    }

    fun mapToDomainList(entities: List<MovieEntity>): List<Movie> {
        return entities.map { mapToDomain(it) }
    }
}
