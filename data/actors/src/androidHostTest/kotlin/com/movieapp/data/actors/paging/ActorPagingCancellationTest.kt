package com.movieapp.data.actors.paging

import androidx.paging.PagingSource
import com.movieapp.core.network.TmdbApiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ActorPagingCancellationTest {

    @Test
    fun `load - api cancellation - propagates cancellation`() = runTest {
        val api = mockk<TmdbApiService>()
        coEvery {
            api.searchActors(
                query = any(),
                page = any(),
                language = any(),
                region = any(),
            )
        } throws CancellationException("cancelled")
        val source = ActorSearchPagingSource(api, "query", "en-US", "US")

        assertCancellation {
            source.load(
                PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 20,
                    placeholdersEnabled = false,
                )
            )
        }
    }

    private suspend fun assertCancellation(block: suspend () -> Unit) {
        try {
            block()
            throw AssertionError("Expected CancellationException")
        } catch (_: CancellationException) {
            // Expected.
        }
    }
}
