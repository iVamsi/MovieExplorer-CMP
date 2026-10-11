package com.movieapp.data.actors.repository

import com.movieapp.core.testing.FixedClock
import com.movieapp.core.testing.testDispatchers
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.database.TransactionRunner
import com.movieapp.core.database.dao.RemoteKeysDao
import com.movieapp.core.database.dao.ActorDao
import com.movieapp.core.domain.repository.ActorRepository
import com.movieapp.core.model.Actor
import com.movieapp.core.model.ActorMovieCast
import com.movieapp.core.model.ActorMovieCredits
import com.movieapp.core.model.PagedResponse
import com.movieapp.core.network.TmdbApiService
import com.movieapp.data.actors.mapper.ActorMapper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.io.IOException
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActorRepositoryTest {

    private val apiService = mockk<TmdbApiService>()
    private val actorDao = mockk<ActorDao>(relaxed = true)
    private val transactionRunner =
        object : TransactionRunner {
            override suspend fun <R> run(block: suspend () -> R): R = block()
        }
    private val remoteKeysDao = mockk<RemoteKeysDao>(relaxed = true)
    private val clock = FixedClock(Instant.fromEpochMilliseconds(1_700_000_000_000L))
    private val mapper = ActorMapper(clock)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val preferencesRepository =
        mockk<PreferencesRepository>(relaxed = true).also {
            every { it.tmdbLanguage } returns flowOf("en-US")
            every { it.watchRegion } returns flowOf("US")
        }

    private lateinit var repository: ActorRepository

    @Before
    fun setup() {
        repository =
            ActorRepositoryImpl(
                apiService = apiService,
                actorDao = actorDao,
                transactionRunner = transactionRunner,
                remoteKeysDao = remoteKeysDao,
                actorMapper = mapper,
                preferencesRepository = preferencesRepository,
                dispatchers = testDispatchers(testDispatcher),
            )
    }

    @Test
    fun `getActorById - api success - inserts entity and returns Success`() = runTest {
        val actor = Actor(id = 287, name = "Brad Pitt")
        coEvery { apiService.getActorDetails(287, language = "en-US") } returns actor

        val result = repository.getActorById(287)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data).isEqualTo(actor)
        coVerify { actorDao.insertActor(any()) }
    }

    @Test
    fun `getActorById - api failure with cache - returns cached Success`() = runTest {
        val cached = Actor(id = 287, name = "Cached Brad")
        coEvery { apiService.getActorDetails(287, language = "en-US") } throws IOException("x")
        coEvery { actorDao.getActorById(287) } returns mapper.mapToEntity(cached)

        val result = repository.getActorById(287)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data.name).isEqualTo("Cached Brad")
    }

    @Test
    fun `getActorById - api failure with no cache - returns Error`() = runTest {
        val failure = IOException("x")
        coEvery { apiService.getActorDetails(9, language = "en-US") } throws failure
        coEvery { actorDao.getActorById(9) } returns null

        val result = repository.getActorById(9)

        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).exception).isSameInstanceAs(failure)
    }

    @Test
    fun `searchActorsResults - api success - emits Loading then Success`() = runTest {
        val actors = listOf(Actor(id = 1, name = "A"))
        coEvery {
            apiService.searchActors(query = "A", page = 1, language = "en-US", region = "US")
        } returns PagedResponse(page = 1, results = actors)

        repository.searchActorsResults("A").test {
            assertThat(awaitItem()).isEqualTo(Result.Loading)
            val success = awaitItem()
            assertThat(success).isInstanceOf(Result.Success::class.java)
            assertThat((success as Result.Success).data).isEqualTo(actors)
            awaitComplete()
        }
    }

    @Test
    fun `searchActorsResults - api failure with cache - emits cached Success`() = runTest {
        val cached = Actor(id = 1, name = "Cached")
        coEvery {
            apiService.searchActors(query = "A", page = 1, language = "en-US", region = "US")
        } throws IOException("offline")
        every { actorDao.searchActors("A") } returns flowOf(listOf(mapper.mapToEntity(cached)))

        repository.searchActorsResults("A").test {
            assertThat(awaitItem()).isEqualTo(Result.Loading)
            val success = awaitItem()
            assertThat(success).isInstanceOf(Result.Success::class.java)
            assertThat((success as Result.Success).data.single().name).isEqualTo("Cached")
            awaitComplete()
        }
    }

    @Test
    fun `getActorMovieCredits - maps cast entries into Movie list`() = runTest {
        coEvery { apiService.getActorMovieCredits(287) } returns
            ActorMovieCredits(
                cast =
                    listOf(
                        ActorMovieCast(id = 550, title = "Fight Club", character = "Tyler"),
                        ActorMovieCast(id = 807, title = "Se7en", character = "Mills"),
                    )
            )

        repository.getActorMovieCredits(287).test {
            assertThat(awaitItem()).isEqualTo(Result.Loading)
            val success = awaitItem()
            assertThat(success).isInstanceOf(Result.Success::class.java)
            assertThat((success as Result.Success).data.map { it.id })
                .containsExactly(550, 807)
                .inOrder()
            awaitComplete()
        }
    }
}
