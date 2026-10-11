package com.movieapp.data.series.mapper

import com.movieapp.core.testing.FixedClock
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.model.Creator
import com.movieapp.core.model.Credits
import com.movieapp.core.model.Episode
import com.movieapp.core.model.ExternalIds
import com.movieapp.core.model.Genre
import com.movieapp.core.model.Image
import com.movieapp.core.model.ImageResponse
import com.movieapp.core.model.Keyword
import com.movieapp.core.model.Network
import com.movieapp.core.model.PagedResponse
import com.movieapp.core.model.ProductionCompany
import com.movieapp.core.model.ProductionCountry
import com.movieapp.core.model.Season
import com.movieapp.core.model.SpokenLanguage
import com.movieapp.core.model.TvSeries
import kotlin.time.Clock
import kotlin.time.Instant
import org.junit.Test

class TvSeriesMapperTest {

    private val fixedMillis = 1_700_000_000_000L
    private val clock = FixedClock(Instant.fromEpochMilliseconds(fixedMillis))
    private val mapper = TvSeriesMapper(clock)

    @Test
    fun `mapToEntity - sets cachedAt from injected clock`() {
        val series = TvSeries(id = 1, name = "Breaking Bad")

        val entity = mapper.mapToEntity(series)

        assertThat(entity.cachedAt).isEqualTo(fixedMillis)
    }

    @Test
    fun `round trip - minimal series preserves scalar fields`() {
        val series = TvSeries(id = 1396, name = "Breaking Bad")

        val roundTripped = mapper.mapToDomain(mapper.mapToEntity(series))

        assertThat(roundTripped).isEqualTo(series.copy(accountRating = null))
    }

    @Test
    fun `round trip - fully populated series preserves nested fields`() {
        val series =
            TvSeries(
                id = 1396,
                name = "Breaking Bad",
                overview = "A chemistry teacher turned meth cook.",
                posterPath = "/poster.jpg",
                backdropPath = "/backdrop.jpg",
                firstAirDate = "2008-01-20",
                lastAirDate = "2013-09-29",
                voteAverage = 8.9,
                voteCount = 12_000,
                popularity = 123.4,
                adult = false,
                originalLanguage = "en",
                originalName = "Breaking Bad",
                originCountry = listOf("US"),
                genreIds = listOf(18, 80),
                genres = listOf(Genre(18, "Drama"), Genre(80, "Crime")),
                episodeRunTime = listOf(45, 47),
                numberOfEpisodes = 62,
                numberOfSeasons = 5,
                status = "Ended",
                tagline = "Change the equation.",
                type = "Scripted",
                homepage = "https://example.com",
                inProduction = false,
                languages = listOf("en"),
                networks = listOf(Network(id = 1, name = "AMC", originCountry = "US")),
                productionCompanies = listOf(ProductionCompany(1, "Sony", null, "US")),
                productionCountries = listOf(ProductionCountry("US", "United States")),
                spokenLanguages = listOf(SpokenLanguage("English", "en", "English")),
                seasons = listOf(Season(id = 1, name = "Season 1", seasonNumber = 1)),
                images = ImageResponse(posters = listOf(Image(filePath = "/a.jpg"))),
                credits = Credits(),
                similar = PagedResponse(page = 1, results = emptyList()),
                recommendations = PagedResponse(page = 1, results = listOf(TvSeries(id = 2))),
                keywords = listOf(Keyword(1, "meth")),
                externalIds = ExternalIds(imdbId = "tt0903747"),
                createdBy = listOf(Creator(id = 1, name = "Vince Gilligan")),
                lastEpisodeToAir = Episode(id = 1, name = "Finale", episodeNumber = 16),
                nextEpisodeToAir = null,
            )

        val roundTripped = mapper.mapToDomain(mapper.mapToEntity(series))

        assertThat(roundTripped).isEqualTo(series.copy(accountRating = null))
    }

    @Test
    fun `round trip - empty collections and null nested stay empty or null`() {
        val series = TvSeries(id = 1, name = "x")

        val roundTripped = mapper.mapToDomain(mapper.mapToEntity(series))

        assertThat(roundTripped.networks).isEmpty()
        assertThat(roundTripped.productionCompanies).isEmpty()
        assertThat(roundTripped.seasons).isEmpty()
        assertThat(roundTripped.createdBy).isEmpty()
        assertThat(roundTripped.keywords).isEmpty()
        assertThat(roundTripped.images).isNull()
        assertThat(roundTripped.credits).isNull()
        assertThat(roundTripped.lastEpisodeToAir).isNull()
        assertThat(roundTripped.nextEpisodeToAir).isNull()
    }

    @Test
    fun `mapToEntityList - maps each series`() {
        val series = listOf(TvSeries(id = 1, name = "A"), TvSeries(id = 2, name = "B"))

        val entities = mapper.mapToEntityList(series)

        assertThat(entities.map { it.id }).containsExactly(1, 2).inOrder()
        assertThat(entities.map { it.cachedAt }).containsExactly(fixedMillis, fixedMillis)
    }

    @Test
    fun `mapToDomainList - round trips list`() {
        val series = listOf(TvSeries(id = 1, name = "A"), TvSeries(id = 2, name = "B"))

        val roundTripped = mapper.mapToDomainList(mapper.mapToEntityList(series))

        assertThat(roundTripped.map { it.id to it.name })
            .containsExactly(1 to "A", 2 to "B")
            .inOrder()
    }
}
