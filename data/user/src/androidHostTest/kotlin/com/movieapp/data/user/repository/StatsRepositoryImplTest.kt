package com.movieapp.data.user.repository

import com.movieapp.core.testing.testDispatchers
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.database.dao.MovieDao
import com.movieapp.core.database.dao.TvSeriesDao
import com.movieapp.core.database.dao.UserDao
import com.movieapp.core.database.entity.CollectionItemEntity
import com.movieapp.core.database.entity.FavoriteItemEntity
import com.movieapp.core.database.entity.MovieEntity
import com.movieapp.core.database.entity.TvSeriesEntity
import com.movieapp.core.database.entity.UserRatingEntity
import com.movieapp.core.model.CastMember
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.Credits
import com.movieapp.core.model.CrewMember
import com.movieapp.core.model.Genre
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatsRepositoryImplTest {

    private val userDao = mockk<UserDao>()
    private val movieDao = mockk<MovieDao>()
    private val tvSeriesDao = mockk<TvSeriesDao>()

    @Test
    fun `getUserWatchStats - aggregates deduped favorites ratings and watched collection items`() =
        runTest {
            val repository =
                StatsRepositoryImpl(
                    userDao,
                    movieDao,
                    tvSeriesDao,
                    testDispatchers(UnconfinedTestDispatcher(testScheduler)),
                )
            every { userDao.getAllFavoriteItems() } returns
                flowOf(
                    listOf(
                        FavoriteItemEntity(
                            id = "favorite-1",
                            contentId = 1,
                            contentType = ContentType.MOVIE.name,
                            addedAt = 1L,
                        )
                    )
                )
            every { userDao.getAllUserRatings() } returns
                flowOf(
                    listOf(
                        UserRatingEntity(
                            id = "rating-1",
                            contentId = 1,
                            contentType = ContentType.MOVIE.name,
                            rating = 8,
                            ratedAt = 2L,
                            notes = null,
                        ),
                        UserRatingEntity(
                            id = "rating-2",
                            contentId = 2,
                            contentType = ContentType.TV_SERIES.name,
                            rating = 9,
                            ratedAt = 3L,
                            notes = null,
                        ),
                    )
                )
            every { userDao.getWatchedCollectionItems() } returns
                flowOf(
                    listOf(
                        CollectionItemEntity(
                            id = "item-1",
                            collectionId = "collection-1",
                            contentId = 1,
                            contentType = ContentType.MOVIE.name,
                            addedAt = 4L,
                            userRating = null,
                            userNotes = null,
                            isWatched = true,
                            watchedAt = 5L,
                        )
                    )
                )
            coEvery { movieDao.getMovieById(1) } returns movieEntity()
            coEvery { tvSeriesDao.getTvSeriesById(2) } returns tvSeriesEntity()

            repository.getUserWatchStats().test {
                assertThat(awaitItem()).isEqualTo(Result.Loading)

                val item = awaitItem()
                assertThat(item).isInstanceOf(Result.Success::class.java)
                val stats = (item as Result.Success).data
                assertThat(stats.totalWatchTimeMinutes).isEqualTo(620)
                assertThat(stats.genreBreakdown.map { it.genreName })
                    .containsAtLeast("Drama", "Action")
                assertThat(stats.decadeBreakdown.map { it.decade })
                    .containsAtLeast("1990s", "2000s")
                assertThat(stats.ratingHistogram).containsExactly(4, 1, 5, 1)
                assertThat(stats.topPeople.map { it.name })
                    .containsAtLeast("Lana Wachowski", "Keanu Reeves", "Bryan Cranston")
                awaitComplete()
            }
        }

    private fun movieEntity(): MovieEntity =
        MovieEntity(
            id = 1,
            title = "The Matrix",
            overview = "Reality is stranger than it looks.",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            releaseDate = "1999-03-31",
            voteAverage = 8.7,
            voteCount = 1000,
            popularity = 100.0,
            adult = false,
            originalLanguage = "en",
            originalTitle = "The Matrix",
            video = false,
            genreIds = listOf(28, 18),
            genres = listOf(Genre(28, "Action"), Genre(18, "Drama")),
            runtime = 120,
            status = "Released",
            tagline = null,
            budget = null,
            revenue = null,
            homepage = null,
            imdbId = null,
            productionCompanies = null,
            productionCountries = null,
            spokenLanguages = null,
            belongsToCollection = null,
            videos = null,
            images = null,
            credits =
                Json.encodeToString(
                    Credits(
                        cast =
                            listOf(
                                CastMember(
                                    id = 10,
                                    name = "Keanu Reeves",
                                    profilePath = "/keanu.jpg",
                                )
                            ),
                        crew =
                            listOf(
                                CrewMember(
                                    id = 20,
                                    name = "Lana Wachowski",
                                    job = "Director",
                                    profilePath = "/lana.jpg",
                                )
                            ),
                    )
                ),
            similar = null,
            recommendations = null,
            reviews = null,
            keywords = null,
            externalIds = null,
            watchProviders = null,
            cachedAt = 0L,
        )

    private fun tvSeriesEntity(): TvSeriesEntity =
        TvSeriesEntity(
            id = 2,
            name = "Breaking Bad",
            overview = "A chemistry teacher changes course.",
            posterPath = "/tv-poster.jpg",
            backdropPath = "/tv-backdrop.jpg",
            firstAirDate = "2008-01-20",
            lastAirDate = "2013-09-29",
            voteAverage = 9.5,
            voteCount = 2000,
            popularity = 95.0,
            adult = false,
            originalLanguage = "en",
            originalName = "Breaking Bad",
            originCountry = listOf("US"),
            genreIds = listOf(18),
            genres = listOf(Genre(18, "Drama")),
            episodeRunTime = listOf(50),
            numberOfEpisodes = 10,
            numberOfSeasons = 1,
            status = "Ended",
            tagline = null,
            type = "Scripted",
            homepage = null,
            inProduction = false,
            languages = listOf("en"),
            networks = null,
            productionCompanies = null,
            productionCountries = null,
            spokenLanguages = null,
            seasons = null,
            videos = null,
            images = null,
            credits =
                Json.encodeToString(
                    Credits(cast = listOf(CastMember(id = 30, name = "Bryan Cranston")))
                ),
            similar = null,
            recommendations = null,
            reviews = null,
            keywords = null,
            externalIds = null,
            watchProviders = null,
            createdBy = null,
            lastEpisodeToAir = null,
            nextEpisodeToAir = null,
            cachedAt = 0L,
        )
}
