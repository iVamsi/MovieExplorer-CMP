package com.movieapp.data.actors.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.movieapp.core.database.TransactionRunner
import com.movieapp.core.database.dao.RemoteKeysDao
import com.movieapp.core.database.dao.ActorDao
import com.movieapp.core.database.entity.ActorEntity
import com.movieapp.core.database.entity.RemoteKeysEntity
import com.movieapp.core.network.TmdbApiService
import com.movieapp.data.actors.mapper.ActorMapper
import kotlinx.io.IOException
import io.ktor.client.plugins.ResponseException

private const val CONTENT_TYPE_ACTOR_POPULAR = "actor_popular"

@OptIn(ExperimentalPagingApi::class)
class ActorRemoteMediator(
    private val apiService: TmdbApiService,
    private val transactionRunner: TransactionRunner,
    private val remoteKeysDao: RemoteKeysDao,
    private val actorDao: ActorDao,
    private val mapper: ActorMapper,
) : RemoteMediator<Int, ActorEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ActorEntity>,
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

            val response = apiService.getPopularActors(page)
            val actors = response.results
            val endOfPaginationReached = actors.isEmpty()

            transactionRunner.run {
                if (loadType == LoadType.REFRESH) {
                    remoteKeysDao.clearByContentType(CONTENT_TYPE_ACTOR_POPULAR)
                    actorDao.deleteAllActors()
                }

                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = actors.map {
                    RemoteKeysEntity(
                        id = it.id,
                        contentType = CONTENT_TYPE_ACTOR_POPULAR,
                        prevKey = prevKey,
                        nextKey = nextKey,
                    )
                }

                remoteKeysDao.insertAll(keys)
                actorDao.insertActors(mapper.mapToEntityList(actors))
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            MediatorResult.Error(exception)
        } catch (exception: ResponseException) {
            MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(
        state: PagingState<Int, ActorEntity>
    ): RemoteKeysEntity? {
        return state.pages
            .lastOrNull { it.data.isNotEmpty() }
            ?.data
            ?.lastOrNull()
            ?.let { actor ->
                remoteKeysDao.getRemoteKeyById(actor.id, CONTENT_TYPE_ACTOR_POPULAR)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(
        state: PagingState<Int, ActorEntity>
    ): RemoteKeysEntity? {
        return state.pages
            .firstOrNull { it.data.isNotEmpty() }
            ?.data
            ?.firstOrNull()
            ?.let { actor ->
                remoteKeysDao.getRemoteKeyById(actor.id, CONTENT_TYPE_ACTOR_POPULAR)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, ActorEntity>
    ): RemoteKeysEntity? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { actorId ->
                remoteKeysDao.getRemoteKeyById(actorId, CONTENT_TYPE_ACTOR_POPULAR)
            }
        }
    }
}
