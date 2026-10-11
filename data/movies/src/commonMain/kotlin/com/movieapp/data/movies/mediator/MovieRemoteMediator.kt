package com.movieapp.data.movies.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.movieapp.core.database.TransactionRunner
import com.movieapp.core.database.dao.RemoteKeysDao
import com.movieapp.core.database.dao.MovieDao
import com.movieapp.core.database.entity.MovieEntity
import com.movieapp.core.database.entity.RemoteKeysEntity
import com.movieapp.core.network.TmdbApiService
import com.movieapp.data.movies.mapper.MovieMapper
import kotlinx.io.IOException
import io.ktor.client.plugins.ResponseException

enum class MovieCategory {
    POPULAR,
    TOP_RATED,
    UPCOMING,
    NOW_PLAYING,
}

@OptIn(ExperimentalPagingApi::class)
class MovieRemoteMediator(
    private val category: MovieCategory,
    private val apiService: TmdbApiService,
    private val transactionRunner: TransactionRunner,
    private val remoteKeysDao: RemoteKeysDao,
    private val movieDao: MovieDao,
    private val mapper: MovieMapper,
) : RemoteMediator<Int, MovieEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MovieEntity>,
    ): MediatorResult {
        return try {
            val remoteKeyContentType = "movie_${category.name.lowercase()}"
            val page =
                when (loadType) {
                    LoadType.REFRESH -> {
                        val remoteKeys =
                            getRemoteKeyClosestToCurrentPosition(state, remoteKeyContentType)
                        remoteKeys?.nextKey?.minus(1) ?: 1
                    }
                    LoadType.PREPEND -> {
                        val remoteKeys = getRemoteKeyForFirstItem(state, remoteKeyContentType)
                        val prevKey =
                            remoteKeys?.prevKey
                                ?: return MediatorResult.Success(
                                    endOfPaginationReached = remoteKeys != null
                                )
                        prevKey
                    }
                    LoadType.APPEND -> {
                        val remoteKeys = getRemoteKeyForLastItem(state, remoteKeyContentType)
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
                    MovieCategory.POPULAR -> apiService.getPopularMovies(page)
                    MovieCategory.TOP_RATED -> apiService.getTopRatedMovies(page)
                    MovieCategory.UPCOMING -> apiService.getUpcomingMovies(page)
                    MovieCategory.NOW_PLAYING -> apiService.getNowPlayingMovies(page)
                }

            val movies = response.results
            val endOfPaginationReached = movies.isEmpty()

            transactionRunner.run {
                if (loadType == LoadType.REFRESH) {
                    remoteKeysDao.clearByContentType(remoteKeyContentType)
                }

                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = movies.map {
                    RemoteKeysEntity(
                        id = it.id,
                        contentType = remoteKeyContentType,
                        prevKey = prevKey,
                        nextKey = nextKey,
                    )
                }

                remoteKeysDao.insertAll(keys)
                movieDao.insertMovies(mapper.mapToEntityList(movies))
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            MediatorResult.Error(exception)
        } catch (exception: ResponseException) {
            MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(
        state: PagingState<Int, MovieEntity>,
        contentType: String,
    ): RemoteKeysEntity? {
        return state.pages
            .lastOrNull { it.data.isNotEmpty() }
            ?.data
            ?.lastOrNull()
            ?.let { movie -> remoteKeysDao.getRemoteKeyById(movie.id, contentType) }
    }

    private suspend fun getRemoteKeyForFirstItem(
        state: PagingState<Int, MovieEntity>,
        contentType: String,
    ): RemoteKeysEntity? {
        return state.pages
            .firstOrNull { it.data.isNotEmpty() }
            ?.data
            ?.firstOrNull()
            ?.let { movie -> remoteKeysDao.getRemoteKeyById(movie.id, contentType) }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, MovieEntity>,
        contentType: String,
    ): RemoteKeysEntity? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { movieId ->
                remoteKeysDao.getRemoteKeyById(movieId, contentType)
            }
        }
    }
}
