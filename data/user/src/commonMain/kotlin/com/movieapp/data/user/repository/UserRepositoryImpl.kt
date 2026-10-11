package com.movieapp.data.user.repository

import com.movieapp.core.common.Result
import com.movieapp.core.common.safeCall
import com.movieapp.core.database.dao.UserDao
import com.movieapp.core.database.entity.*
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.model.CollectionItem
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.FavoriteItem
import com.movieapp.core.model.UserCollection
import com.movieapp.core.model.WatchlistItem
import com.movieapp.data.user.sync.TmdbUserLibrarySync
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val clock: Clock,
    private val tmdbUserLibrarySync: TmdbUserLibrarySync,
) : UserRepository {

    override fun getAllCollections(): Flow<List<UserCollection>> {
        return userDao.getAllCollections().map { entities ->
            entities.map { it.toUserCollection() }
        }
    }

    override fun getCollectionById(collectionId: String): Flow<Result<UserCollection>> {
        return userDao
            .getCollectionByIdFlow(collectionId)
            .map<UserCollectionEntity?, Result<UserCollection>> { entity ->
                if (entity != null) {
                    Result.Success(entity.toUserCollection())
                } else {
                    Result.Error(NoSuchElementException())
                }
            }
            .onStart { emit(Result.Loading) }
            .catch { exception -> emit(Result.Error(exception)) }
    }

    override suspend fun createCollection(
        name: String,
        description: String?,
        isPublic: Boolean,
    ): Result<UserCollection> {
        return safeCall {
            val collection =
                UserCollectionEntity(
                    id = Uuid.random().toString(),
                    name = name,
                    description = description,
                    isPublic = isPublic,
                    itemCount = 0,
                    createdAt = clock.now().toEpochMilliseconds(),
                    updatedAt = clock.now().toEpochMilliseconds(),
                    items = null,
                )
            userDao.insertCollection(collection)
            collection.toUserCollection()
        }
    }

    override suspend fun updateCollection(collection: UserCollection): Result<Unit> {
        return safeCall { userDao.updateCollection(collection.toEntity()) }
    }

    override suspend fun deleteCollection(collectionId: String): Result<Unit> {
        return safeCall {
            userDao.deleteCollectionItemsByCollectionId(collectionId)
            userDao.deleteCollectionById(collectionId)
        }
    }

    override suspend fun addToCollection(
        collectionId: String,
        contentId: Int,
        contentType: ContentType,
    ): Result<Unit> {
        return safeCall {
            val item =
                CollectionItemEntity(
                    id = Uuid.random().toString(),
                    collectionId = collectionId,
                    contentId = contentId,
                    contentType = contentType.name,
                    addedAt = clock.now().toEpochMilliseconds(),
                    userRating = null,
                    userNotes = null,
                    isWatched = false,
                    watchedAt = null,
                )
            userDao.insertCollectionItem(item)
        }
    }

    override suspend fun removeFromCollection(
        collectionId: String,
        contentId: Int,
        contentType: ContentType,
    ): Result<Unit> {
        return safeCall {
            userDao.deleteCollectionItemByCollectionAndContent(
                collectionId = collectionId,
                contentId = contentId,
                contentType = contentType.name,
            )
        }
    }

    override fun getCollectionItems(collectionId: String): Flow<List<CollectionItem>> {
        return userDao.getCollectionItems(collectionId).map { entities ->
            entities.map { it.toCollectionItem() }
        }
    }

    override fun getWatchlist(): Flow<List<WatchlistItem>> {
        return userDao.getAllWatchlistItems().map { entities ->
            entities.map { it.toWatchlistItem() }
        }
    }

    override suspend fun addToWatchlist(contentId: Int, contentType: ContentType): Result<Unit> {
        return safeCall {
            val item =
                WatchlistItemEntity(
                    id = Uuid.random().toString(),
                    contentId = contentId,
                    contentType = contentType.name,
                    addedAt = clock.now().toEpochMilliseconds(),
                    priority = 0,
                )
            userDao.insertWatchlistItem(item)
            safeCall {
                tmdbUserLibrarySync.pushWatchlistState(contentId, contentType, inWatchlist = true)
            }
            Unit
        }
    }

    override suspend fun removeFromWatchlist(
        contentId: Int,
        contentType: ContentType,
    ): Result<Unit> {
        return safeCall {
            userDao.deleteWatchlistItemByContent(contentId, contentType.name)
            safeCall {
                tmdbUserLibrarySync.pushWatchlistState(contentId, contentType, inWatchlist = false)
            }
            Unit
        }
    }

    override suspend fun isInWatchlist(contentId: Int, contentType: ContentType): Boolean {
        return userDao.getWatchlistItemByContent(contentId, contentType.name) != null
    }

    override fun getFavorites(): Flow<List<FavoriteItem>> {
        return userDao.getAllFavoriteItems().map { entities ->
            entities.map { it.toFavoriteItem() }
        }
    }

    override suspend fun addToFavorites(contentId: Int, contentType: ContentType): Result<Unit> {
        return safeCall {
            val item =
                FavoriteItemEntity(
                    id = Uuid.random().toString(),
                    contentId = contentId,
                    contentType = contentType.name,
                    addedAt = clock.now().toEpochMilliseconds(),
                )
            userDao.insertFavoriteItem(item)
            safeCall {
                tmdbUserLibrarySync.pushFavoriteState(contentId, contentType, favorite = true)
            }
            Unit
        }
    }

    override suspend fun removeFromFavorites(
        contentId: Int,
        contentType: ContentType,
    ): Result<Unit> {
        return safeCall {
            userDao.deleteFavoriteItemByContent(contentId, contentType.name)
            safeCall {
                tmdbUserLibrarySync.pushFavoriteState(contentId, contentType, favorite = false)
            }
            Unit
        }
    }

    override suspend fun isFavorite(contentId: Int, contentType: ContentType): Boolean {
        return userDao.getFavoriteItemByContent(contentId, contentType.name) != null
    }

    override suspend fun getUserRating(contentId: Int, contentType: ContentType): Result<Float?> {
        return safeCall {
            userDao.getUserRatingByContent(contentId, contentType.name)?.rating?.toFloat()
        }
    }

    override suspend fun rateContent(
        contentId: Int,
        contentType: ContentType,
        rating: Float,
        notes: String?,
    ): Result<Unit> {
        return safeCall {
            val now = clock.now().toEpochMilliseconds()
            val existing = userDao.getUserRatingByContent(contentId, contentType.name)
            val ratingInt = rating.toInt().coerceIn(1, 10)
            if (existing != null) {
                userDao.updateUserRating(
                    existing.copy(
                        rating = ratingInt,
                        ratedAt = now,
                        notes = notes ?: existing.notes,
                    )
                )
            } else {
                userDao.insertUserRating(
                    UserRatingEntity(
                        id = Uuid.random().toString(),
                        contentId = contentId,
                        contentType = contentType.name,
                        rating = ratingInt,
                        ratedAt = now,
                        notes = notes,
                    )
                )
            }
            safeCall {
                tmdbUserLibrarySync.pushRatingState(contentId, contentType, rating.toFloat())
            }
            Unit
        }
    }

    override suspend fun updateCollectionItem(
        itemId: String,
        userNotes: String?,
        userRating: Int?,
        isWatched: Boolean,
    ): Result<Unit> {
        return safeCall {
            val entity =
                userDao.getCollectionItemById(itemId)
                    ?: throw NoSuchElementException("Collection item not found")
            val now = clock.now().toEpochMilliseconds()
            userDao.updateCollectionItem(
                entity.copy(
                    userNotes = userNotes,
                    userRating = userRating,
                    isWatched = isWatched,
                    watchedAt = if (isWatched) (entity.watchedAt ?: now) else null,
                )
            )
        }
    }

    override suspend fun removeRating(contentId: Int, contentType: ContentType): Result<Unit> {
        return safeCall {
            userDao.deleteUserRatingByContent(contentId, contentType.name)
            safeCall { tmdbUserLibrarySync.pushRatingState(contentId, contentType, null) }
            Unit
        }
    }
}

// Extension functions to convert between entities and models
private fun UserCollectionEntity.toUserCollection() =
    UserCollection(
        id = id,
        name = name,
        description = description,
        isPublic = isPublic,
        createdAt = createdAt,
        updatedAt = updatedAt,
        itemCount = itemCount,
        items = emptyList(),
    )

private fun UserCollection.toEntity() =
    UserCollectionEntity(
        id = id,
        name = name,
        description = description,
        isPublic = isPublic,
        itemCount = itemCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        items = null,
    )

private fun CollectionItemEntity.toCollectionItem() =
    CollectionItem(
        id = id,
        collectionId = collectionId,
        contentId = contentId,
        contentType = ContentType.valueOf(contentType),
        addedAt = addedAt,
        userRating = userRating,
        userNotes = userNotes,
        isWatched = isWatched,
        watchedAt = watchedAt,
    )

private fun WatchlistItemEntity.toWatchlistItem() =
    WatchlistItem(
        id = id,
        contentId = contentId,
        contentType = ContentType.valueOf(contentType),
        addedAt = addedAt,
        priority = priority,
    )

private fun FavoriteItemEntity.toFavoriteItem() =
    FavoriteItem(
        id = id,
        contentId = contentId,
        contentType = ContentType.valueOf(contentType),
        addedAt = addedAt,
    )
