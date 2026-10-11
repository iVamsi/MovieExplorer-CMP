package com.movieapp.data.series.paging

import androidx.paging.PagingSource
import com.movieapp.core.network.TmdbApiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SeriesPagingCancellationTest {

    private val params =
        PagingSource.LoadParams.Refresh<Int>(
            key = null,
            loadSize = 20,
            placeholdersEnabled = false,
        )

    @Test
    fun `series search load - api cancellation - propagates cancellation`() = runTest {
        val api = mockk<TmdbApiService>()
        coEvery {
            api.searchTvSeries(query = any(), page = any(), language = any())
        } throws CancellationException("cancelled")

        assertCancellation {
            TvSeriesSearchPagingSource(api, "query", "en-US").load(params)
        }
    }

    @Test
    fun `series review load - api cancellation - propagates cancellation`() = runTest {
        val api = mockk<TmdbApiService>()
        coEvery {
            api.getTvSeriesReviews(tvId = any(), page = any(), language = any())
        } throws CancellationException("cancelled")

        assertCancellation {
            SeriesReviewPagingSource(api, seriesId = 1, language = "en-US").load(params)
        }
    }

    @Test
    fun `related series load - api cancellation - propagates cancellation`() = runTest {
        val source =
            TvSeriesRelatedPagingSource(
                seriesId = 1,
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
