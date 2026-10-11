package com.movieapp.data.series.mapper

import com.movieapp.core.database.entity.TvSeriesEntity
import com.movieapp.core.model.*
import kotlin.time.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TvSeriesMapper(private val clock: Clock) {

    private val json = Json { ignoreUnknownKeys = true }

    fun mapToEntity(tvSeries: TvSeries): TvSeriesEntity {
        val now = clock.now().toEpochMilliseconds()
        return TvSeriesEntity(
            id = tvSeries.id,
            name = tvSeries.name,
            overview = tvSeries.overview,
            posterPath = tvSeries.posterPath,
            backdropPath = tvSeries.backdropPath,
            firstAirDate = tvSeries.firstAirDate,
            lastAirDate = tvSeries.lastAirDate,
            voteAverage = tvSeries.voteAverage,
            voteCount = tvSeries.voteCount,
            popularity = tvSeries.popularity,
            adult = tvSeries.adult,
            originalLanguage = tvSeries.originalLanguage,
            originalName = tvSeries.originalName,
            originCountry = tvSeries.originCountry,
            genreIds = tvSeries.genreIds,
            genres = tvSeries.genres,
            episodeRunTime = tvSeries.episodeRunTime,
            numberOfEpisodes = tvSeries.numberOfEpisodes,
            numberOfSeasons = tvSeries.numberOfSeasons,
            status = tvSeries.status,
            tagline = tvSeries.tagline,
            type = tvSeries.type,
            homepage = tvSeries.homepage,
            inProduction = tvSeries.inProduction,
            languages = tvSeries.languages,
            networks =
                tvSeries.networks.takeIf { it.isNotEmpty() }?.let { json.encodeToString(it) },
            productionCompanies =
                tvSeries.productionCompanies
                    .takeIf { it.isNotEmpty() }
                    ?.let { json.encodeToString(it) },
            productionCountries =
                tvSeries.productionCountries
                    .takeIf { it.isNotEmpty() }
                    ?.let { json.encodeToString(it) },
            spokenLanguages =
                tvSeries.spokenLanguages
                    .takeIf { it.isNotEmpty() }
                    ?.let { json.encodeToString(it) },
            seasons = tvSeries.seasons.takeIf { it.isNotEmpty() }?.let { json.encodeToString(it) },
            videos = tvSeries.videos,
            images = tvSeries.images?.let { json.encodeToString(it) },
            credits = tvSeries.credits?.let { json.encodeToString(it) },
            similar = tvSeries.similar?.let { json.encodeToString(it) },
            recommendations = tvSeries.recommendations?.let { json.encodeToString(it) },
            reviews = tvSeries.reviews?.let { json.encodeToString(it) },
            keywords =
                tvSeries.keywords.takeIf { it.isNotEmpty() }?.let { json.encodeToString(it) },
            externalIds = tvSeries.externalIds?.let { json.encodeToString(it) },
            watchProviders = tvSeries.watchProviders?.let { json.encodeToString(it) },
            createdBy =
                tvSeries.createdBy.takeIf { it.isNotEmpty() }?.let { json.encodeToString(it) },
            lastEpisodeToAir = tvSeries.lastEpisodeToAir?.let { json.encodeToString(it) },
            nextEpisodeToAir = tvSeries.nextEpisodeToAir?.let { json.encodeToString(it) },
            cachedAt = now,
        )
    }

    fun mapToDomain(entity: TvSeriesEntity): TvSeries {
        return TvSeries(
            id = entity.id,
            name = entity.name,
            overview = entity.overview,
            posterPath = entity.posterPath,
            backdropPath = entity.backdropPath,
            firstAirDate = entity.firstAirDate,
            lastAirDate = entity.lastAirDate,
            voteAverage = entity.voteAverage,
            voteCount = entity.voteCount,
            popularity = entity.popularity,
            adult = entity.adult,
            originalLanguage = entity.originalLanguage,
            originalName = entity.originalName,
            originCountry = entity.originCountry,
            genreIds = entity.genreIds,
            genres = entity.genres ?: emptyList(),
            episodeRunTime = entity.episodeRunTime,
            numberOfEpisodes = entity.numberOfEpisodes,
            numberOfSeasons = entity.numberOfSeasons,
            status = entity.status,
            tagline = entity.tagline,
            type = entity.type,
            homepage = entity.homepage,
            inProduction = entity.inProduction,
            languages = entity.languages,
            networks =
                entity.networks?.let {
                    try {
                        json.decodeFromString<List<Network>>(it)
                    } catch (e: Exception) {
                        emptyList()
                    }
                } ?: emptyList(),
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
            seasons =
                entity.seasons?.let {
                    try {
                        json.decodeFromString<List<Season>>(it)
                    } catch (e: Exception) {
                        emptyList()
                    }
                } ?: emptyList(),
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
                        json.decodeFromString<PagedResponse<TvSeries>>(it)
                    } catch (e: Exception) {
                        null
                    }
                },
            recommendations =
                entity.recommendations?.let {
                    try {
                        json.decodeFromString<PagedResponse<TvSeries>>(it)
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
            createdBy =
                entity.createdBy?.let {
                    try {
                        json.decodeFromString<List<Creator>>(it)
                    } catch (e: Exception) {
                        emptyList()
                    }
                } ?: emptyList(),
            lastEpisodeToAir =
                entity.lastEpisodeToAir?.let {
                    try {
                        json.decodeFromString<Episode>(it)
                    } catch (e: Exception) {
                        null
                    }
                },
            nextEpisodeToAir =
                entity.nextEpisodeToAir?.let {
                    try {
                        json.decodeFromString<Episode>(it)
                    } catch (e: Exception) {
                        null
                    }
                },
        )
    }

    fun mapToEntityList(tvSeriesList: List<TvSeries>): List<TvSeriesEntity> {
        return tvSeriesList.map { mapToEntity(it) }
    }

    fun mapToDomainList(entities: List<TvSeriesEntity>): List<TvSeries> {
        return entities.map { mapToDomain(it) }
    }
}
