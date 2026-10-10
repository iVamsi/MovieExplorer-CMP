package com.movieapp.core.network

import com.movieapp.core.model.Actor
import com.movieapp.core.model.ActorMovieCredits
import com.movieapp.core.model.ActorTvCredits
import com.movieapp.core.model.Movie
import com.movieapp.core.model.PagedResponse
import com.movieapp.core.model.TaggedImage
import com.movieapp.core.model.TvSeries
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

class TmdbJsonDeserializationTest {

    private val json: Json = tmdbTestJson()

    @Test
    fun `paged movies popular decodes`() {
        val paged =
            json.decodeFromString<PagedResponse<Movie>>(TmdbFixtures.read("movie_popular.json"))
        assertEquals(1, paged.page)
        assertEquals(500, paged.totalPages)
        assertEquals(10000, paged.totalResults)
        assertEquals(1, paged.results.size)
        assertEquals(278, paged.results[0].id)
        assertTrue(paged.results[0].title.contains("Shawshank"))
    }

    @Test
    fun `paged movies empty results decodes`() {
        val paged =
            json.decodeFromString<PagedResponse<Movie>>(
                TmdbFixtures.read("paged_movies_empty.json")
            )
        assertTrue(paged.results.isEmpty())
    }

    @Test
    fun `movie detail with append videos decodes`() {
        val movie = json.decodeFromString<Movie>(TmdbFixtures.read("movie_detail_with_videos.json"))
        assertEquals(278, movie.id)
        assertEquals(142, movie.runtime)
        assertEquals(1, movie.videos?.results?.size)
        assertEquals("PLl99DlL6b4", movie.videos?.results?.first()?.key)
        assertEquals("YouTube", movie.videos?.results?.first()?.site)
        assertEquals("2015-01-01T12:00:00.000Z", movie.videos?.results?.first()?.publishedAt)
    }

    @Test
    fun `movie detail sparse video optional fields use defaults`() {
        val movie =
            json.decodeFromString<Movie>(TmdbFixtures.read("movie_detail_sparse_video.json"))
        val video = movie.videos?.results?.first()
        assertNotNull(video)
        assertTrue(video.name.isEmpty())
        assertEquals(0, video.size)
        assertFalse(video.official)
        assertNull(video.publishedAt)
    }

    @Test
    fun `tv detail with videos decodes`() {
        val series =
            json.decodeFromString<TvSeries>(TmdbFixtures.read("tv_detail_with_videos.json"))
        assertEquals(1396, series.id)
        assertTrue(series.name.contains("Breaking"))
        assertEquals(5, series.numberOfSeasons)
        assertEquals("HhesaRleEyA", series.videos?.results?.first()?.key)
    }

    @Test
    fun `search movies paged decodes`() {
        val paged =
            json.decodeFromString<PagedResponse<Movie>>(TmdbFixtures.read("search_movie.json"))
        assertEquals(550, paged.results.single().id)
    }

    @Test
    fun `discover movies paged decodes`() {
        val paged =
            json.decodeFromString<PagedResponse<Movie>>(TmdbFixtures.read("discover_movie.json"))
        assertEquals(27205, paged.results.single().id)
    }

    @Test
    fun `discover tv paged decodes`() {
        val paged =
            json.decodeFromString<PagedResponse<TvSeries>>(TmdbFixtures.read("discover_tv.json"))
        assertEquals(66732, paged.results.single().id)
        assertTrue(paged.results.single().name.contains("Stranger"))
    }

    @Test
    fun `actor detail decodes`() {
        val actor = json.decodeFromString<Actor>(TmdbFixtures.read("actor_detail.json"))
        assertEquals(3223, actor.id)
        assertTrue(actor.name.contains("Robert Downey"))
        assertEquals(listOf("RDJ"), actor.alsoKnownAs)
    }

    @Test
    fun `actor movie credits decodes`() {
        val credits =
            json.decodeFromString<ActorMovieCredits>(TmdbFixtures.read("actor_movie_credits.json"))
        assertEquals(1, credits.cast.size)
        assertEquals(99861, credits.cast[0].id)
        assertTrue(credits.cast[0].title.contains("Avengers"))
    }

    @Test
    fun `actor tv credits decodes`() {
        val credits =
            json.decodeFromString<ActorTvCredits>(TmdbFixtures.read("actor_tv_credits.json"))
        assertEquals(1396, credits.cast.single().id)
        assertEquals("Breaking Bad", credits.cast.single().name)
    }

    @Test
    fun `actor movie credits maps to Movie like ActorRepositoryImpl`() {
        val credits =
            json.decodeFromString<ActorMovieCredits>(TmdbFixtures.read("actor_movie_credits.json"))
        val movies =
            credits.cast.map { cast ->
                Movie(
                    id = cast.id,
                    title = cast.title,
                    overview = cast.overview,
                    posterPath = cast.posterPath,
                    backdropPath = cast.backdropPath,
                    releaseDate = cast.releaseDate,
                    voteAverage = cast.voteAverage,
                    voteCount = cast.voteCount,
                    popularity = cast.popularity,
                    adult = cast.adult,
                    originalLanguage = cast.originalLanguage,
                    originalTitle = cast.originalTitle,
                    video = cast.video,
                    genreIds = cast.genreIds,
                )
            }
        assertEquals(99861, movies.single().id)
        assertEquals("Avengers: Age of Ultron", movies.single().title)
    }

    @Test
    fun `paged actors decodes`() {
        val paged =
            json.decodeFromString<PagedResponse<Actor>>(TmdbFixtures.read("paged_actors.json"))
        assertEquals(287, paged.results.single().id)
        assertEquals("Brad Pitt", paged.results.single().name)
    }

    @Test
    fun `person tagged images paged decodes`() {
        val paged =
            json.decodeFromString<PagedResponse<TaggedImage>>(
                TmdbFixtures.read("person_tagged_images.json")
            )
        assertEquals(1, paged.page)
        assertEquals(1, paged.totalPages)
        assertEquals(1, paged.results.size)
        assertEquals("/tagged_still.jpg", paged.results.single().filePath)
        assertEquals("Test Movie", paged.results.single().media.title)
    }
}
