package com.movieapp.data.user.repository

import com.movieapp.core.testing.FixedClock
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.database.dao.UserDao
import com.movieapp.core.database.entity.FavoriteItemEntity
import com.movieapp.core.database.entity.UserRatingEntity
import com.movieapp.core.database.entity.WatchlistItemEntity
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.model.ContentType
import com.movieapp.data.user.sync.TmdbUserLibrarySync
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.slot
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UserRepositoryTest {

    private val userDao = mockk<UserDao>(relaxed = true)
    private val fixedMillis = 1_700_000_000_000L
    private val clock = FixedClock(Instant.fromEpochMilliseconds(fixedMillis))
    private val tmdbSync = mockk<TmdbUserLibrarySync>(relaxed = true)

    private lateinit var repository: UserRepository

    @Before
    fun setup() {
        repository = UserRepositoryImpl(userDao, clock, tmdbSync)
    }

    @Test
    fun `addToFavorites - inserts entity with clock timestamp and pushes to tmdb`() = runTest {
        val slot = slot<FavoriteItemEntity>()
        coJustRun { userDao.insertFavoriteItem(capture(slot)) }

        val result = repository.addToFavorites(contentId = 550, contentType = ContentType.MOVIE)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat(slot.captured.contentId).isEqualTo(550)
        assertThat(slot.captured.contentType).isEqualTo(ContentType.MOVIE.name)
        assertThat(slot.captured.addedAt).isEqualTo(fixedMillis)
        coVerify { tmdbSync.pushFavoriteState(550, ContentType.MOVIE, favorite = true) }
    }

    @Test
    fun `addToFavorites - dao failure returns Error`() = runTest {
        val failure = IllegalStateException("db error")
        coEvery { userDao.insertFavoriteItem(any()) } throws failure

        val result = repository.addToFavorites(1, ContentType.MOVIE)

        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).exception).isSameInstanceAs(failure)
    }

    @Test
    fun `removeFromFavorites - delegates to dao and pushes false to tmdb`() = runTest {
        val result =
            repository.removeFromFavorites(contentId = 99, contentType = ContentType.TV_SERIES)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        coVerify { userDao.deleteFavoriteItemByContent(99, ContentType.TV_SERIES.name) }
        coVerify { tmdbSync.pushFavoriteState(99, ContentType.TV_SERIES, favorite = false) }
    }

    @Test
    fun `isFavorite - returns true when dao row exists`() = runTest {
        coEvery { userDao.getFavoriteItemByContent(3, ContentType.MOVIE.name) } returns
            FavoriteItemEntity(
                id = "id",
                contentId = 3,
                contentType = ContentType.MOVIE.name,
                addedAt = fixedMillis,
            )

        assertThat(repository.isFavorite(3, ContentType.MOVIE)).isTrue()
    }

    @Test
    fun `isFavorite - returns false when dao row missing`() = runTest {
        coEvery { userDao.getFavoriteItemByContent(3, ContentType.MOVIE.name) } returns null

        assertThat(repository.isFavorite(3, ContentType.MOVIE)).isFalse()
    }

    @Test
    fun `addToWatchlist - inserts entity with clock timestamp`() = runTest {
        val slot = slot<WatchlistItemEntity>()
        coJustRun { userDao.insertWatchlistItem(capture(slot)) }

        val result = repository.addToWatchlist(contentId = 42, contentType = ContentType.MOVIE)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat(slot.captured.contentId).isEqualTo(42)
        assertThat(slot.captured.addedAt).isEqualTo(fixedMillis)
        coVerify { tmdbSync.pushWatchlistState(42, ContentType.MOVIE, inWatchlist = true) }
    }

    @Test
    fun `rateContent - when no existing rating - inserts new row`() = runTest {
        coEvery { userDao.getUserRatingByContent(1, ContentType.MOVIE.name) } returns null
        val slot = slot<UserRatingEntity>()
        coJustRun { userDao.insertUserRating(capture(slot)) }

        val result =
            repository.rateContent(
                contentId = 1,
                contentType = ContentType.MOVIE,
                rating = 8.0f,
                notes = "great",
            )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat(slot.captured.rating).isEqualTo(8)
        assertThat(slot.captured.notes).isEqualTo("great")
        assertThat(slot.captured.ratedAt).isEqualTo(fixedMillis)
        coVerify { tmdbSync.pushRatingState(1, ContentType.MOVIE, 8.0f) }
    }

    @Test
    fun `rateContent - when existing rating - updates and preserves notes if null`() = runTest {
        val existing =
            UserRatingEntity(
                id = "uuid",
                contentId = 1,
                contentType = ContentType.MOVIE.name,
                rating = 5,
                ratedAt = 0L,
                notes = "old notes",
            )
        coEvery { userDao.getUserRatingByContent(1, ContentType.MOVIE.name) } returns existing
        val slot = slot<UserRatingEntity>()
        coJustRun { userDao.updateUserRating(capture(slot)) }

        repository.rateContent(1, ContentType.MOVIE, rating = 9.0f, notes = null)

        assertThat(slot.captured.rating).isEqualTo(9)
        assertThat(slot.captured.notes).isEqualTo("old notes")
        assertThat(slot.captured.ratedAt).isEqualTo(fixedMillis)
    }

    @Test
    fun `rateContent - coerces rating into 1-10 bounds`() = runTest {
        coEvery { userDao.getUserRatingByContent(any(), any()) } returns null
        val slot = slot<UserRatingEntity>()
        coJustRun { userDao.insertUserRating(capture(slot)) }

        repository.rateContent(1, ContentType.MOVIE, rating = 99.0f, notes = null)

        assertThat(slot.captured.rating).isEqualTo(10)
    }

    @Test
    fun `getUserRating - returns Success with float rating when present`() = runTest {
        coEvery { userDao.getUserRatingByContent(5, ContentType.MOVIE.name) } returns
            UserRatingEntity(
                id = "id",
                contentId = 5,
                contentType = ContentType.MOVIE.name,
                rating = 7,
                ratedAt = fixedMillis,
                notes = null,
            )

        val result = repository.getUserRating(5, ContentType.MOVIE)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data).isEqualTo(7f)
    }

    @Test
    fun `getUserRating - returns Success with null when dao has no row`() = runTest {
        coEvery { userDao.getUserRatingByContent(5, ContentType.MOVIE.name) } returns null

        val result = repository.getUserRating(5, ContentType.MOVIE)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data).isNull()
    }

    @Test
    fun `removeRating - delegates to dao and pushes null to tmdb`() = runTest {
        val result = repository.removeRating(5, ContentType.MOVIE)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        coVerify { userDao.deleteUserRatingByContent(5, ContentType.MOVIE.name) }
        coVerify { tmdbSync.pushRatingState(5, ContentType.MOVIE, null) }
    }

    @Test
    fun `getCollectionById - emits Loading then Error when dao returns null`() = runTest {
        coEvery { userDao.getCollectionByIdFlow("missing") } returns flowOf(null)

        repository.getCollectionById("missing").test {
            assertThat(awaitItem()).isEqualTo(Result.Loading)
            val error = awaitItem()
            assertThat(error).isInstanceOf(Result.Error::class.java)
            assertThat((error as Result.Error).exception)
                .isInstanceOf(NoSuchElementException::class.java)
            awaitComplete()
        }
    }

    @Test
    fun `deleteCollection - deletes child items before collection row`() = runTest {
        val result = repository.deleteCollection(collectionId = "collection-1")

        assertThat(result).isInstanceOf(Result.Success::class.java)
        coVerifyOrder {
            userDao.deleteCollectionItemsByCollectionId("collection-1")
            userDao.deleteCollectionById("collection-1")
        }
    }

    @Test
    fun `removeFromCollection - deletes matching content only from target collection`() = runTest {
        val result =
            repository.removeFromCollection(
                collectionId = "collection-1",
                contentId = 550,
                contentType = ContentType.MOVIE,
            )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        coVerify {
            userDao.deleteCollectionItemByCollectionAndContent(
                collectionId = "collection-1",
                contentId = 550,
                contentType = ContentType.MOVIE.name,
            )
        }
    }
}
