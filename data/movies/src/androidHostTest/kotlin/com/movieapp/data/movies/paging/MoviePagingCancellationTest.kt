package com.movieapp.data.movies.paging

import androidx.paging.PagingSource
import com.movieapp.core.network.TmdbApiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MoviePagingCancellationTest {

    private val params =
        PagingSource.LoadParams.Refresh<Int>(
            key = null,
            loadSize = 20,
            placeholdersEnabled = false,
        )

    @Test
    fun `movie search load - api cancellation - propagates cancellation`() = runTest {
        val api = mockk<TmdbApiService>()
        coEvery {
            api.searchMovies(
                query = any(),
                page = any(),
                language = any(),
                region = any(),
            )
        } throws CancellationException("cancelled")

        assertCancellation {
            MovieSearchPagingSource(api, "query", "en-US", "US").load(params)
        }
    }

    @Test
    fun `multi search load - api cancellation - propagates cancellation`() = runTest {
        val api = mockk<TmdbApiService>()
        coEvery {
            api.multiSearch(
                query = any(),
                page = any(),
                language = any(),
                includeAdult = any(),
                region = any(),
            )
        } throws CancellationException("cancelled")

        assertCancellation {
            MultiSearchPagingSource(api, "query", "en-US", "US").load(params)
        }
    }

    @Test
    fun `movie review load - api cancellation - propagates cancellation`() = runTest {
        val api = mockk<TmdbApiService>()
        coEvery {
            api.getMovieReviews(movieId = any(), page = any(), language = any())
        } throws CancellationException("cancelled")

        assertCancellation {
            MovieReviewPagingSource(api, movieId = 1, language = "en-US").load(params)
        }
    }

    @Test
    fun `related movie load - api cancellation - propagates cancellation`() = runTest {
        val source =
            MovieRelatedPagingSource(
                apiService = mockk(),
                movieId = 1,
                language = "en-US",
                fetchPage = { _, _, _ -> throw CancellationException("cancelled") },
            )

        assertCancellation { source.load(params) }
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
