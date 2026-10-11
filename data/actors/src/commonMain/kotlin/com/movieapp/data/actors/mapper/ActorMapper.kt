package com.movieapp.data.actors.mapper

import com.movieapp.core.database.entity.ActorEntity
import com.movieapp.core.model.*
import kotlin.time.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ActorMapper(private val clock: Clock) {

    private val json = Json { ignoreUnknownKeys = true }

    fun mapToEntity(actor: Actor): ActorEntity {
        val now = clock.now().toEpochMilliseconds()
        return ActorEntity(
            id = actor.id,
            name = actor.name,
            biography = actor.biography,
            birthday = actor.birthday,
            deathday = actor.deathday,
            placeOfBirth = actor.placeOfBirth,
            profilePath = actor.profilePath,
            adult = actor.adult,
            gender = actor.gender,
            knownForDepartment = actor.knownForDepartment,
            originalName = actor.originalName,
            popularity = actor.popularity,
            alsoKnownAs = actor.alsoKnownAs,
            images = actor.images?.let { json.encodeToString(it) },
            movieCredits = actor.movieCredits?.let { json.encodeToString(it) },
            tvCredits = actor.tvCredits?.let { json.encodeToString(it) },
            externalIds = actor.externalIds?.let { json.encodeToString(it) },
            taggedImages = actor.taggedImages?.let { json.encodeToString(it) },
            cachedAt = now,
        )
    }

    fun mapToDomain(entity: ActorEntity): Actor {
        return Actor(
            id = entity.id,
            name = entity.name,
            biography = entity.biography,
            birthday = entity.birthday,
            deathday = entity.deathday,
            placeOfBirth = entity.placeOfBirth,
            profilePath = entity.profilePath,
            adult = entity.adult,
            gender = entity.gender,
            knownForDepartment = entity.knownForDepartment,
            originalName = entity.originalName,
            popularity = entity.popularity,
            alsoKnownAs = entity.alsoKnownAs,
            images =
                entity.images?.let {
                    try {
                        json.decodeFromString<ActorImageResponse>(it)
                    } catch (e: Exception) {
                        null
                    }
                },
            movieCredits =
                entity.movieCredits?.let {
                    try {
                        json.decodeFromString<ActorMovieCredits>(it)
                    } catch (e: Exception) {
                        null
                    }
                },
            tvCredits =
                entity.tvCredits?.let {
                    try {
                        json.decodeFromString<ActorTvCredits>(it)
                    } catch (e: Exception) {
                        null
                    }
                },
            externalIds =
                entity.externalIds?.let {
                    try {
                        json.decodeFromString<ExternalIds>(it)
                    } catch (e: Exception) {
                        null
                    }
                },
            taggedImages =
                entity.taggedImages?.let {
                    try {
                        json.decodeFromString<PagedResponse<TaggedImage>>(it)
                    } catch (e: Exception) {
                        null
                    }
                },
        )
    }

    fun mapToEntityList(actors: List<Actor>): List<ActorEntity> {
        return actors.map { mapToEntity(it) }
    }

    fun mapToDomainList(entities: List<ActorEntity>): List<Actor> {
        return entities.map { mapToDomain(it) }
    }
}

internal fun ActorImage.toDomainImage(): Image =
    Image(
        aspectRatio = aspectRatio,
        height = height,
        iso6391 = iso6391,
        filePath = filePath,
        voteAverage = voteAverage,
        voteCount = voteCount,
        width = width,
    )

internal fun TaggedImage.toDomainImage(): Image? =
    if (filePath.isBlank()) {
        null
    } else {
        Image(
            aspectRatio = aspectRatio,
            height = height,
            iso6391 = iso6391,
            filePath = filePath,
            voteAverage = voteAverage,
            voteCount = voteCount,
            width = width,
        )
    }
