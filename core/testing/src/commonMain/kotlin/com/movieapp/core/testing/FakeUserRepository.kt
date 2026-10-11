package com.movieapp.core.testing

import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.UserRepository
import com.movieapp.core.model.CollectionItem
import com.movieapp.core.model.ContentType
import com.movieapp.core.model.FavoriteItem
import com.movieapp.core.model.UserCollection
import com.movieapp.core.model.WatchlistItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeUserRepository : UserRepository {
    val collections = MutableStateFlow(FakeData.fakeUserCollections)
    val watchlist = MutableStateFlow(FakeData.fakeWatchlistItems)
    val favorites = MutableStateFlow(FakeData.fakeFavoriteItems)
    var collectionResult: Result<UserCollection> = Result.Success(FakeData.fakeUserCollections.first())
    var collectionItems: List<CollectionItem> = FakeData.fakeUserCollections.first().items
    var userRatingResult: Result<Float?> = Result.Success(null)
    var unitResult: Result<Unit> = Result.Success(Unit)

    val createdCollections = mutableListOf<Triple<String, String?, Boolean>>()
    val updatedCollections = mutableListOf<UserCollection>()
    val deletedCollectionIds = mutableListOf<String>()
    val addedToCollection = mutableListOf<Triple<String, Int, ContentType>>()
    val removedFromCollection = mutableListOf<Triple<String, Int, ContentType>>()
    val addedToWatchlist = mutableListOf<Pair<Int, ContentType>>()
    val removedFromWatchlist = mutableListOf<Pair<Int, ContentType>>()
    val addedToFavorites = mutableListOf<Pair<Int, ContentType>>()
    val removedFromFavorites = mutableListOf<Pair<Int, ContentType>>()
    val ratings = mutableListOf<Triple<Int, ContentType, Float>>()
    val removedRatings = mutableListOf<Pair<Int, ContentType>>()
    val updatedCollectionItems = mutableListOf<String>()

    override fun getAllCollections(): Flow<List<UserCollection>> = collections
    override fun getCollectionById(collectionId: String): Flow<Result<UserCollection>> = flowOf(collectionResult)

    override suspend fun createCollection(name: String, description: String?, isPublic: Boolean): Result<UserCollection> {
        createdCollections += Triple(name, description, isPublic)
        return collectionResult
    }

    override suspend fun updateCollection(collection: UserCollection): Result<Unit> {
        updatedCollections += collection
        return unitResult
    }

    override suspend fun deleteCollection(collectionId: String): Result<Unit> {
        deletedCollectionIds += collectionId
        return unitResult
    }

    override suspend fun addToCollection(collectionId: String, contentId: Int, contentType: ContentType): Result<Unit> {
        addedToCollection += Triple(collectionId, contentId, contentType)
        return unitResult
    }

    override suspend fun removeFromCollection(
        collectionId: String,
        contentId: Int,
        contentType: ContentType,
    ): Result<Unit> {
        removedFromCollection += Triple(collectionId, contentId, contentType)
        return unitResult
    }

    override fun getCollectionItems(collectionId: String): Flow<List<CollectionItem>> = flowOf(collectionItems)
    override fun getWatchlist(): Flow<List<WatchlistItem>> = watchlist

    override suspend fun addToWatchlist(contentId: Int, contentType: ContentType): Result<Unit> {
        addedToWatchlist += contentId to contentType
        return unitResult
    }

    override suspend fun removeFromWatchlist(contentId: Int, contentType: ContentType): Result<Unit> {
        removedFromWatchlist += contentId to contentType
        return unitResult
    }

    override suspend fun isInWatchlist(contentId: Int, contentType: ContentType): Boolean =
        watchlist.value.any { it.contentId == contentId && it.contentType == contentType }

    override fun getFavorites(): Flow<List<FavoriteItem>> = favorites

    override suspend fun addToFavorites(contentId: Int, contentType: ContentType): Result<Unit> {
        addedToFavorites += contentId to contentType
        return unitResult
    }

    override suspend fun removeFromFavorites(contentId: Int, contentType: ContentType): Result<Unit> {
        removedFromFavorites += contentId to contentType
        return unitResult
    }

    override suspend fun isFavorite(contentId: Int, contentType: ContentType): Boolean =
        favorites.value.any { it.contentId == contentId && it.contentType == contentType }

    override suspend fun getUserRating(contentId: Int, contentType: ContentType): Result<Float?> = userRatingResult

    override suspend fun rateContent(contentId: Int, contentType: ContentType, rating: Float, notes: String?): Result<Unit> {
        ratings += Triple(contentId, contentType, rating)
        return unitResult
    }

    override suspend fun updateCollectionItem(
        itemId: String,
        userNotes: String?,
        userRating: Int?,
        isWatched: Boolean,
    ): Result<Unit> {
        updatedCollectionItems += itemId
        return unitResult
    }

    override suspend fun removeRating(contentId: Int, contentType: ContentType): Result<Unit> {
        removedRatings += contentId to contentType
        return unitResult
    }
}
