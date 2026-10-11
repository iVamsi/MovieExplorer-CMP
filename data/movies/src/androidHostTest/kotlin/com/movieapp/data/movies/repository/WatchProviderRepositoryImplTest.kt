package com.movieapp.data.movies.repository

import com.movieapp.core.testing.testDispatchers
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.movieapp.core.common.Result
import com.movieapp.core.common.preferences.PreferencesRepository
import com.movieapp.core.model.WatchProvider
import com.movieapp.core.model.WatchProviderCatalog
import com.movieapp.core.network.TmdbWatchProviderApi
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class WatchProviderRepositoryImplTest {

    private val api = mockk<TmdbWatchProviderApi>()
    private val preferencesRepository =
        mockk<PreferencesRepository>().also {
            every { it.tmdbLanguage } returns flowOf("en-US")
            every { it.watchRegion } returns flowOf("US")
        }
    private val repository =
        WatchProviderRepositoryImpl(
            api = api,
            preferencesRepository = preferencesRepository,
            dispatchers = testDispatchers(UnconfinedTestDispatcher()),
        )

    @Test
    fun `getWatchProviderCatalog - movie and tv providers - merges by provider id sorted by priority`() =
        runTest {
            val netflix =
                WatchProvider(
                    displayPriority = 0,
                    logoPath = "/netflix.png",
                    providerId = 8,
                    providerName = "Netflix",
                )
            val prime =
                WatchProvider(
                    displayPriority = 1,
                    logoPath = "/prime.png",
                    providerId = 9,
                    providerName = "Prime Video",
                )
            val duplicatePrime = prime.copy(displayPriority = 4)
            coEvery {
                api.getMovieWatchProviderCatalog(language = "en-US", watchRegion = "US")
            } returns WatchProviderCatalog(results = listOf(prime, netflix))
            coEvery {
                api.getTvWatchProviderCatalog(language = "en-US", watchRegion = "US")
            } returns WatchProviderCatalog(results = listOf(duplicatePrime))

            repository.getWatchProviderCatalog().test {
                assertThat(awaitItem()).isEqualTo(Result.Loading)
                val success = awaitItem()
                assertThat(success).isInstanceOf(Result.Success::class.java)
                assertThat((success as Result.Success).data.map { it.providerName })
                    .containsExactly("Netflix", "Prime Video")
                    .inOrder()
                awaitComplete()
            }
        }
}
