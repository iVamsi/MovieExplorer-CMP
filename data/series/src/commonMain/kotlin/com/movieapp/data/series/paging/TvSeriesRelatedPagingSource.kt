package com.movieapp.data.series.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.movieapp.core.model.PagedResponse
import com.movieapp.core.model.TvSeries
import kotlinx.coroutines.CancellationException

class TvSeriesRelatedPagingSource(
    private val seriesId: Int,
    private val language: String,
    private val fetchPage:
        suspend (seriesId: Int, page: Int, language: String) -> PagedResponse<TvSeries>,
) : PagingSource<Int, TvSeries>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TvSeries> {
        return try {
            val page = params.key ?: 1
            val response = fetchPage(seriesId, page, language)
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

    override fun getRefreshKey(state: PagingState<Int, TvSeries>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
