package com.movieapp.data.movies.mapper

import com.movieapp.core.testing.FixedClock
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.model.CastMember
import com.movieapp.core.model.MovieCollection
import com.movieapp.core.model.Credits
import com.movieapp.core.model.ExternalIds
import com.movieapp.core.model.Genre
import com.movieapp.core.model.Image
import com.movieapp.core.model.ImageResponse
import com.movieapp.core.model.Keyword
import com.movieapp.core.model.Movie
import com.movieapp.core.model.PagedResponse
import com.movieapp.core.model.ProductionCompany
import com.movieapp.core.model.ProductionCountry
import com.movieapp.core.model.SpokenLanguage
import com.movieapp.core.model.WatchProviderCountry
import com.movieapp.core.model.WatchProviders
import kotlin.time.Clock
import kotlin.time.Instant
import org.junit.Test

class MovieMapperTest {

    private val fixedMillis = 1_700_000_000_000L
    private val clock = FixedClock(Instant.fromEpochMilliseconds(fixedMillis))
    private val mapper = MovieMapper(clock)

    @Test
    fun `mapToEntity - sets cachedAt from injected clock`() {
        val movie = Movie(id = 1, title = "Fight Club")

        val entity = mapper.mapToEntity(movie)

        assertThat(entity.cachedAt).isEqualTo(fixedMillis)
    }

    @Test
    fun `round trip - minimal movie preserves scalar fields`() {
        val movie = Movie(id = 42, title = "Minimal", overview = "o")

        val roundTripped = mapper.mapToDomain(mapper.mapToEntity(movie))

        assertThat(roundTripped).isEqualTo(movie.copy(accountRating = null))
    }

    @Test
    fun `round trip - fully populated movie preserves nested fields`() {
        val movie =
            Movie(
                id = 550,
                title = "Fight Club",
                overview = "An insomniac office worker.",
                posterPath = "/poster.jpg",
                backdropPath = "/backdrop.jpg",
                releaseDate = "1999-10-15",
                voteAverage = 8.4,
                voteCount = 27_000,
                popularity = 99.5,
                adult = false,
                originalLanguage = "en",
                originalTitle = "Fight Club",
                video = false,
                genreIds = listOf(18, 53),
                genres = listOf(Genre(18, "Drama"), Genre(53, "Thriller")),
                runtime = 139,
                status = "Released",
                tagline = "Mischief. Mayhem. Soap.",
                budget = 63_000_000L,
                revenue = 100_853_753L,
                homepage = "https://example.com",
                imdbId = "tt0137523",
                productionCompanies = listOf(ProductionCompany(1, "Fox", "/logo.png", "US")),
                productionCountries = listOf(ProductionCountry("US", "United States")),
                spokenLanguages = listOf(SpokenLanguage("English", "en", "English")),
                belongsToCollection =
                    MovieCollection(id = 99, name = "Collection", posterPath = "/p.jpg"),
                images = ImageResponse(posters = listOf(Image(filePath = "/a.jpg"))),
                credits = Credits(cast = listOf(CastMember(id = 1, name = "Brad Pitt"))),
                similar = PagedResponse(page = 1, results = listOf(Movie(id = 2, title = "Other"))),
                recommendations = PagedResponse(page = 1, results = emptyList()),
                keywords = listOf(Keyword(1, "fight")),
                externalIds = ExternalIds(imdbId = "tt0137523"),
                watchProviders =
                    WatchProviders(
                        results = mapOf("US" to WatchProviderCountry(link = "https://x"))
                    ),
            )

        val roundTripped = mapper.mapToDomain(mapper.mapToEntity(movie))

        assertThat(roundTripped).isEqualTo(movie.copy(accountRating = null))
    }

    @Test
    fun `round trip - empty collections stay empty`() {
        val movie = Movie(id = 1, title = "t")

        val roundTripped = mapper.mapToDomain(mapper.mapToEntity(movie))

        assertThat(roundTripped.productionCompanies).isEmpty()
        assertThat(roundTripped.productionCountries).isEmpty()
        assertThat(roundTripped.spokenLanguages).isEmpty()
        assertThat(roundTripped.keywords).isEmpty()
        assertThat(roundTripped.belongsToCollection).isNull()
        assertThat(roundTripped.images).isNull()
        assertThat(roundTripped.credits).isNull()
    }

    @Test
    fun `mapToEntityList - maps each movie`() {
        val movies = listOf(Movie(id = 1, title = "A"), Movie(id = 2, title = "B"))

        val entities = mapper.mapToEntityList(movies)

        assertThat(entities.map { it.id }).containsExactly(1, 2).inOrder()
        assertThat(entities.map { it.cachedAt }).containsExactly(fixedMillis, fixedMillis)
    }

    @Test
    fun `mapToDomainList - round trips list`() {
        val movies = listOf(Movie(id = 1, title = "A"), Movie(id = 2, title = "B"))

        val roundTripped = mapper.mapToDomainList(mapper.mapToEntityList(movies))

        assertThat(roundTripped.map { it.id to it.title })
            .containsExactly(1 to "A", 2 to "B")
            .inOrder()
    }
}
