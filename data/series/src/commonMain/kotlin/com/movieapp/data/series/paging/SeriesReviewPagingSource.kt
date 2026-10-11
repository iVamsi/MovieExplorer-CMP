package com.movieapp.data.series.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.movieapp.core.model.Review
import com.movieapp.core.network.TmdbApiService
import kotlinx.coroutines.CancellationException

class SeriesReviewPagingSource(
    private val apiService: TmdbApiService,
    private val seriesId: Int,
    private val language: String,
) : PagingSource<Int, Review>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Review> {
        return try {
            val page = params.key ?: 1
            val response = apiService.getTvSeriesReviews(seriesId, page = page, language = language)
            LoadResult.Page(
                data = response.results,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (page < response.totalPages) page + 1 else null,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Review>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
