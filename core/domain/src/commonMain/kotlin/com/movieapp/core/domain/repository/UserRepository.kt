package com.movieapp.core.domain.repository

import com.movieapp.core.common.Result
import com.movieapp.core.model.CollectionItem
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.FavoriteItem
import com.movieapp.core.model.UserCollection
import com.movieapp.core.model.WatchlistItem
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun getAllCollections(): Flow<List<UserCollection>>

    fun getCollectionById(collectionId: String): Flow<Result<UserCollection>>

    suspend fun createCollection(
        name: String,
        description: String?,
        isPublic: Boolean,
    ): Result<UserCollection>

    suspend fun updateCollection(collection: UserCollection): Result<Unit>

    suspend fun deleteCollection(collectionId: String): Result<Unit>

    suspend fun addToCollection(
        collectionId: String,
        contentId: Int,
        contentType: ContentType,
    ): Result<Unit>

    suspend fun removeFromCollection(
        collectionId: String,
        contentId: Int,
        contentType: ContentType,
    ): Result<Unit>

    fun getCollectionItems(collectionId: String): Flow<List<CollectionItem>>

    fun getWatchlist(): Flow<List<WatchlistItem>>

    suspend fun addToWatchlist(contentId: Int, contentType: ContentType): Result<Unit>

    suspend fun removeFromWatchlist(contentId: Int, contentType: ContentType): Result<Unit>

    suspend fun isInWatchlist(contentId: Int, contentType: ContentType): Boolean

    fun getFavorites(): Flow<List<FavoriteItem>>

    suspend fun addToFavorites(contentId: Int, contentType: ContentType): Result<Unit>

    suspend fun removeFromFavorites(contentId: Int, contentType: ContentType): Result<Unit>

    suspend fun isFavorite(contentId: Int, contentType: ContentType): Boolean

    suspend fun getUserRating(contentId: Int, contentType: ContentType): Result<Float?>

    suspend fun rateContent(
        contentId: Int,
        contentType: ContentType,
        rating: Float,
        notes: String? = null,
    ): Result<Unit>

    suspend fun updateCollectionItem(
        itemId: String,
        userNotes: String?,
        userRating: Int?,
        isWatched: Boolean,
    ): Result<Unit>

    suspend fun removeRating(contentId: Int, contentType: ContentType): Result<Unit>
}
