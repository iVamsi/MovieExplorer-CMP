package com.movieapp.core.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.movieapp.core.database.entity.ActorEntity
import com.movieapp.core.database.entity.FavoriteItemEntity
import com.movieapp.core.database.entity.MovieEntity
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.Genre
import com.movieapp.core.model.Video
import com.movieapp.core.model.VideoResponse
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class MovieAppDatabaseTest {

    private val db = Room.inMemoryDatabaseBuilder<MovieAppDatabase>()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

    @AfterTest
    fun tearDown() {
        db.close()
    }

    @Test
    fun `insertMovie - entity with JSON columns - reads back equal`() = runTest(timeout = 10.seconds) {
        val movie = movieEntity(id = 603)

        db.movieDao().insertMovie(movie)

        assertEquals(movie, db.movieDao().getMovieById(603))
    }

    @Test
    fun `getFavoriteActors - actor favorited with ContentType name - returns actor`() = runTest(timeout = 10.seconds) {
        val actor = actorEntity(id = 6384)
        db.actorDao().insertActor(actor)
        db.userDao().insertFavoriteItem(
            FavoriteItemEntity(id = "fav-1", contentId = 6384, contentType = ContentType.ACTOR.name, addedAt = 1L),
        )

        assertEquals(listOf(actor), db.actorDao().getFavoriteActors().first())
    }

    @Test
    fun `RoomTransactionRunner - block throws - rolls back writes`() = runTest(timeout = 10.seconds) {
        val runner = RoomTransactionRunner(db)

        assertFailsWith<IllegalStateException> {
            runner.run {
                db.movieDao().insertMovie(movieEntity(id = 1))
                error("boom")
            }
        }

        assertNull(db.movieDao().getMovieById(1))
    }

    @Test
    fun `RoomTransactionRunner - block succeeds - commits writes and returns result`() = runTest(timeout = 10.seconds) {
        val runner = RoomTransactionRunner(db)

        val result = runner.run {
            db.movieDao().insertMovie(movieEntity(id = 2))
            "done"
        }

        assertEquals("done", result)
        assertEquals(2, db.movieDao().getMovieById(2)?.id)
    }

    private fun movieEntity(id: Int) = MovieEntity(
        id = id,
        title = "The Matrix",
        overview = "A hacker learns the truth.",
        posterPath = "/poster.jpg",
        backdropPath = null,
        releaseDate = "1999-03-31",
        voteAverage = 8.2,
        voteCount = 25000,
        popularity = 80.5,
        adult = false,
        originalLanguage = "en",
        originalTitle = "The Matrix",
        video = false,
        genreIds = listOf(28, 878),
        genres = listOf(Genre(28, "Action"), Genre(878, "Science Fiction")),
        runtime = 136,
        status = "Released",
        tagline = null,
        budget = 63_000_000L,
        revenue = 463_517_383L,
        homepage = null,
        imdbId = "tt0133093",
        productionCompanies = null,
        productionCountries = null,
        spokenLanguages = null,
        belongsToCollection = null,
        videos = VideoResponse(listOf(Video(id = "v1", key = "vKQi3bBA1y8", name = "Trailer", site = "YouTube", type = "Trailer"))),
        images = null,
        credits = null,
        similar = null,
        recommendations = null,
        reviews = null,
        keywords = null,
        externalIds = null,
        watchProviders = null,
        cachedAt = 1_700_000_000_000L,
    )

    private fun actorEntity(id: Int) = ActorEntity(
        id = id,
        name = "Keanu Reeves",
        biography = null,
        birthday = "1964-09-02",
        deathday = null,
        placeOfBirth = "Beirut, Lebanon",
        profilePath = "/keanu.jpg",
        adult = false,
        gender = 2,
        knownForDepartment = "Acting",
        originalName = "Keanu Reeves",
        popularity = 50.0,
        alsoKnownAs = listOf("K. Reeves"),
        images = null,
        movieCredits = null,
        tvCredits = null,
        externalIds = null,
        taggedImages = null,
        cachedAt = 1_700_000_000_000L,
    )
}
