package com.movieapp.data.series.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.movieapp.core.database.TransactionRunner
import com.movieapp.core.common.Constants
import com.movieapp.core.database.dao.RemoteKeysDao
import com.movieapp.core.database.dao.TvSeriesDao
import com.movieapp.core.database.entity.RemoteKeysEntity
import com.movieapp.core.database.entity.TvSeriesEntity
import com.movieapp.core.network.TmdbApiService
import com.movieapp.data.series.mapper.TvSeriesMapper
import kotlinx.io.IOException
import io.ktor.client.plugins.ResponseException

enum class SeriesCategory {
    POPULAR,
    TOP_RATED,
    AIRING_TODAY,
    ON_THE_AIR,
    TRENDING,
}

@OptIn(ExperimentalPagingApi::class)
class SeriesRemoteMediator(
    private val category: SeriesCategory,
    private val apiService: TmdbApiService,
    private val transactionRunner: TransactionRunner,
    private val remoteKeysDao: RemoteKeysDao,
    private val tvSeriesDao: TvSeriesDao,
    private val mapper: TvSeriesMapper,
) : RemoteMediator<Int, TvSeriesEntity>() {

    private val contentType = "tv_${category.name.lowercase()}"

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TvSeriesEntity>,
    ): MediatorResult {
        return try {
            val page =
                when (loadType) {
                    LoadType.REFRESH -> {
                        val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                        remoteKeys?.nextKey?.minus(1) ?: 1
                    }
                    LoadType.PREPEND -> {
                        val remoteKeys = getRemoteKeyForFirstItem(state)
                        val prevKey =
                            remoteKeys?.prevKey
                                ?: return MediatorResult.Success(
                                    endOfPaginationReached = remoteKeys != null
                                )
                        prevKey
                    }
                    LoadType.APPEND -> {
                        val remoteKeys = getRemoteKeyForLastItem(state)
                        val nextKey =
                            remoteKeys?.nextKey
                                ?: return MediatorResult.Success(
                                    endOfPaginationReached = remoteKeys != null
                                )
                        nextKey
                    }
                }

            val response =
                when (category) {
                    SeriesCategory.POPULAR -> apiService.getPopularTvSeries(page)
                    SeriesCategory.TOP_RATED -> apiService.getTopRatedTvSeries(page)
                    SeriesCategory.AIRING_TODAY -> apiService.getAiringTodayTvSeries(page)
                    SeriesCategory.ON_THE_AIR -> apiService.getOnTheAirTvSeries(page)
                    SeriesCategory.TRENDING ->
                        apiService.getTrendingTvSeries(
                            timeWindow = Constants.TimeWindow.DAY,
                            page = page,
                        )
                }

            val seriesList = response.results
            val endOfPaginationReached = seriesList.isEmpty()

            transactionRunner.run {
                if (loadType == LoadType.REFRESH) {
                    remoteKeysDao.clearByContentType(contentType)
                    tvSeriesDao.deleteByCategory(category.name)
                }

                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = seriesList.map {
                    RemoteKeysEntity(
                        id = it.id,
                        contentType = contentType,
                        prevKey = prevKey,
                        nextKey = nextKey,
                    )
                }

                remoteKeysDao.insertAll(keys)
                val entities = seriesList.map { series ->
                    mapper.mapToEntity(series).copy(category = category.name)
                }
                tvSeriesDao.insertTvSeriesList(entities)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            MediatorResult.Error(exception)
        } catch (exception: ResponseException) {
            MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(
        state: PagingState<Int, TvSeriesEntity>
    ): RemoteKeysEntity? {
        return state.pages
            .lastOrNull { it.data.isNotEmpty() }
            ?.data
            ?.lastOrNull()
            ?.let { series -> remoteKeysDao.getRemoteKeyById(series.id, contentType) }
    }

    private suspend fun getRemoteKeyForFirstItem(
        state: PagingState<Int, TvSeriesEntity>
    ): RemoteKeysEntity? {
        return state.pages
            .firstOrNull { it.data.isNotEmpty() }
            ?.data
            ?.firstOrNull()
            ?.let { series -> remoteKeysDao.getRemoteKeyById(series.id, contentType) }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, TvSeriesEntity>
    ): RemoteKeysEntity? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { seriesId ->
                remoteKeysDao.getRemoteKeyById(seriesId, contentType)
            }
        }
    }
}
