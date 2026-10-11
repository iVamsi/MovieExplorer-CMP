package com.movieapp.data.actors.mapper

import com.movieapp.core.testing.FixedClock
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.model.Actor
import com.movieapp.core.model.ActorImage
import com.movieapp.core.model.ActorImageResponse
import com.movieapp.core.model.ActorMovieCast
import com.movieapp.core.model.ActorMovieCredits
import com.movieapp.core.model.ActorTvCast
import com.movieapp.core.model.ActorTvCredits
import com.movieapp.core.model.ExternalIds
import com.movieapp.core.model.PagedResponse
import com.movieapp.core.model.TaggedImage
import kotlin.time.Clock
import kotlin.time.Instant
import org.junit.Test

class ActorMapperTest {

    private val fixedMillis = 1_700_000_000_000L
    private val clock = FixedClock(Instant.fromEpochMilliseconds(fixedMillis))
    private val mapper = ActorMapper(clock)

    @Test
    fun `mapToEntity - sets cachedAt from injected clock`() {
        val actor = Actor(id = 1, name = "Brad Pitt")

        val entity = mapper.mapToEntity(actor)

        assertThat(entity.cachedAt).isEqualTo(fixedMillis)
    }

    @Test
    fun `round trip - minimal actor preserves scalar fields`() {
        val actor = Actor(id = 287, name = "Brad Pitt")

        val roundTripped = mapper.mapToDomain(mapper.mapToEntity(actor))

        assertThat(roundTripped).isEqualTo(actor)
    }

    @Test
    fun `round trip - fully populated actor preserves nested fields`() {
        val actor =
            Actor(
                id = 287,
                name = "Brad Pitt",
                biography = "An American actor.",
                birthday = "1963-12-18",
                deathday = null,
                placeOfBirth = "Shawnee, Oklahoma, USA",
                profilePath = "/profile.jpg",
                adult = false,
                gender = 2,
                knownForDepartment = "Acting",
                originalName = "Brad Pitt",
                popularity = 45.2,
                alsoKnownAs = listOf("William Bradley Pitt"),
                images =
                    ActorImageResponse(
                        profiles =
                            listOf(
                                ActorImage(iso6391 = null, filePath = "/p.jpg", aspectRatio = 0.6)
                            )
                    ),
                movieCredits =
                    ActorMovieCredits(
                        cast =
                            listOf(
                                ActorMovieCast(id = 550, title = "Fight Club", character = "Tyler")
                            )
                    ),
                tvCredits =
                    ActorTvCredits(
                        cast = listOf(ActorTvCast(id = 1, name = "Show", character = "Guest"))
                    ),
                externalIds = ExternalIds(imdbId = "nm0000093"),
                taggedImages =
                    PagedResponse(
                        page = 1,
                        results = listOf(TaggedImage(filePath = "/tagged.jpg")),
                    ),
            )

        val roundTripped = mapper.mapToDomain(mapper.mapToEntity(actor))

        assertThat(roundTripped).isEqualTo(actor)
    }

    @Test
    fun `round trip - null nested fields stay null`() {
        val actor = Actor(id = 1, name = "x")

        val roundTripped = mapper.mapToDomain(mapper.mapToEntity(actor))

        assertThat(roundTripped.images).isNull()
        assertThat(roundTripped.movieCredits).isNull()
        assertThat(roundTripped.tvCredits).isNull()
        assertThat(roundTripped.externalIds).isNull()
        assertThat(roundTripped.taggedImages).isNull()
    }

    @Test
    fun `mapToEntityList - maps each actor`() {
        val actors = listOf(Actor(id = 1, name = "A"), Actor(id = 2, name = "B"))

        val entities = mapper.mapToEntityList(actors)

        assertThat(entities.map { it.id }).containsExactly(1, 2).inOrder()
        assertThat(entities.map { it.cachedAt }).containsExactly(fixedMillis, fixedMillis)
    }

    @Test
    fun `mapToDomainList - round trips list`() {
        val actors = listOf(Actor(id = 1, name = "A"), Actor(id = 2, name = "B"))

        val roundTripped = mapper.mapToDomainList(mapper.mapToEntityList(actors))

        assertThat(roundTripped).isEqualTo(actors)
    }
}
