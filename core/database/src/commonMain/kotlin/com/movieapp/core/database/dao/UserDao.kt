package com.movieapp.core.database.dao

import androidx.room.*
import com.movieapp.core.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // User Collections
    @Query("SELECT * FROM user_collections ORDER BY updatedAt DESC")
    fun getAllCollections(): Flow<List<UserCollectionEntity>>

    @Query("SELECT * FROM user_collections WHERE id = :collectionId")
    suspend fun getCollectionById(collectionId: String): UserCollectionEntity?

    @Query("SELECT * FROM user_collections WHERE id = :collectionId")
    fun getCollectionByIdFlow(collectionId: String): Flow<UserCollectionEntity?>

    @Query("SELECT * FROM user_collections WHERE isPublic = 1 ORDER BY updatedAt DESC")
    fun getPublicCollections(): Flow<List<UserCollectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: UserCollectionEntity)

    @Update suspend fun updateCollection(collection: UserCollectionEntity)

    @Delete suspend fun deleteCollection(collection: UserCollectionEntity)

    @Query("DELETE FROM user_collections WHERE id = :collectionId")
    suspend fun deleteCollectionById(collectionId: String)

    // Collection Items
    @Query(
        "SELECT * FROM collection_items WHERE collectionId = :collectionId ORDER BY addedAt DESC"
    )
    fun getCollectionItems(collectionId: String): Flow<List<CollectionItemEntity>>

    @Query(
        "SELECT * FROM collection_items WHERE collectionId = :collectionId AND contentType = :contentType ORDER BY addedAt DESC"
    )
    fun getCollectionItemsByType(
        collectionId: String,
        contentType: String,
    ): Flow<List<CollectionItemEntity>>

    @Query("SELECT * FROM collection_items WHERE id = :itemId")
    suspend fun getCollectionItemById(itemId: String): CollectionItemEntity?

    @Query(
        "SELECT * FROM collection_items WHERE contentId = :contentId AND contentType = :contentType"
    )
    suspend fun getCollectionItemByContent(
        contentId: Int,
        contentType: String,
    ): CollectionItemEntity?

    @Query("SELECT * FROM collection_items WHERE isWatched = 1 ORDER BY watchedAt DESC")
    fun getWatchedCollectionItems(): Flow<List<CollectionItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollectionItem(item: CollectionItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollectionItems(items: List<CollectionItemEntity>)

    @Update suspend fun updateCollectionItem(item: CollectionItemEntity)

    @Delete suspend fun deleteCollectionItem(item: CollectionItemEntity)

    @Query("DELETE FROM collection_items WHERE id = :itemId")
    suspend fun deleteCollectionItemById(itemId: String)

    @Query("DELETE FROM collection_items WHERE collectionId = :collectionId")
    suspend fun deleteCollectionItemsByCollectionId(collectionId: String)

    @Query(
        """
        DELETE FROM collection_items
        WHERE collectionId = :collectionId
            AND contentId = :contentId
            AND contentType = :contentType
        """
    )
    suspend fun deleteCollectionItemByCollectionAndContent(
        collectionId: String,
        contentId: Int,
        contentType: String,
    )

    @Query(
        "DELETE FROM collection_items WHERE contentId = :contentId AND contentType = :contentType"
    )
    suspend fun deleteCollectionItemByContent(contentId: Int, contentType: String)

    // Watchlist Items
    @Query("SELECT * FROM watchlist_items ORDER BY addedAt DESC")
    fun getAllWatchlistItems(): Flow<List<WatchlistItemEntity>>

    @Query("SELECT * FROM watchlist_items WHERE contentType = :contentType ORDER BY addedAt DESC")
    fun getWatchlistItemsByType(contentType: String): Flow<List<WatchlistItemEntity>>

    @Query(
        "SELECT * FROM watchlist_items WHERE contentId = :contentId AND contentType = :contentType"
    )
    suspend fun getWatchlistItemByContent(contentId: Int, contentType: String): WatchlistItemEntity?

    @Query("SELECT * FROM watchlist_items WHERE id = :itemId")
    suspend fun getWatchlistItemById(itemId: String): WatchlistItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistItem(item: WatchlistItemEntity)

    @Update suspend fun updateWatchlistItem(item: WatchlistItemEntity)

    @Delete suspend fun deleteWatchlistItem(item: WatchlistItemEntity)

    @Query("DELETE FROM watchlist_items WHERE id = :itemId")
    suspend fun deleteWatchlistItemById(itemId: String)

    @Query(
        "DELETE FROM watchlist_items WHERE contentId = :contentId AND contentType = :contentType"
    )
    suspend fun deleteWatchlistItemByContent(contentId: Int, contentType: String)

    // Favorite Items
    @Query("SELECT * FROM favorite_items ORDER BY addedAt DESC")
    fun getAllFavoriteItems(): Flow<List<FavoriteItemEntity>>

    @Query("SELECT * FROM favorite_items WHERE contentType = :contentType ORDER BY addedAt DESC")
    fun getFavoriteItemsByType(contentType: String): Flow<List<FavoriteItemEntity>>

    @Query(
        "SELECT * FROM favorite_items WHERE contentId = :contentId AND contentType = :contentType"
    )
    suspend fun getFavoriteItemByContent(contentId: Int, contentType: String): FavoriteItemEntity?

    @Query("SELECT * FROM favorite_items WHERE id = :itemId")
    suspend fun getFavoriteItemById(itemId: String): FavoriteItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteItem(item: FavoriteItemEntity)

    @Update suspend fun updateFavoriteItem(item: FavoriteItemEntity)

    @Delete suspend fun deleteFavoriteItem(item: FavoriteItemEntity)

    @Query("DELETE FROM favorite_items WHERE id = :itemId")
    suspend fun deleteFavoriteItemById(itemId: String)

    @Query("DELETE FROM favorite_items WHERE contentId = :contentId AND contentType = :contentType")
    suspend fun deleteFavoriteItemByContent(contentId: Int, contentType: String)

    // User Ratings
    @Query("SELECT * FROM user_ratings ORDER BY ratedAt DESC")
    fun getAllUserRatings(): Flow<List<UserRatingEntity>>

    @Query("SELECT * FROM user_ratings WHERE contentType = :contentType ORDER BY ratedAt DESC")
    fun getUserRatingsByType(contentType: String): Flow<List<UserRatingEntity>>

    @Query("SELECT * FROM user_ratings WHERE contentId = :contentId AND contentType = :contentType")
    suspend fun getUserRatingByContent(contentId: Int, contentType: String): UserRatingEntity?

    @Query("SELECT * FROM user_ratings WHERE id = :ratingId")
    suspend fun getUserRatingById(ratingId: String): UserRatingEntity?

    @Query(
        "SELECT AVG(rating) FROM user_ratings WHERE contentId = :contentId AND contentType = :contentType"
    )
    suspend fun getAverageRatingForContent(contentId: Int, contentType: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserRating(rating: UserRatingEntity)

    @Update suspend fun updateUserRating(rating: UserRatingEntity)

    @Delete suspend fun deleteUserRating(rating: UserRatingEntity)

    @Query("DELETE FROM user_ratings WHERE id = :ratingId")
    suspend fun deleteUserRatingById(ratingId: String)

    @Query("DELETE FROM user_ratings WHERE contentId = :contentId AND contentType = :contentType")
    suspend fun deleteUserRatingByContent(contentId: Int, contentType: String)

    // Genres
    @Query("SELECT * FROM genres WHERE type = :type ORDER BY name ASC")
    fun getGenresByType(type: String): Flow<List<GenreEntity>>

    @Query("SELECT * FROM genres WHERE id = :genreId")
    suspend fun getGenreById(genreId: Int): GenreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertGenre(genre: GenreEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenres(genres: List<GenreEntity>)

    @Delete suspend fun deleteGenre(genre: GenreEntity)

    @Query("DELETE FROM genres WHERE type = :type") suspend fun deleteGenresByType(type: String)

    @Query("DELETE FROM genres") suspend fun deleteAllGenres()
}
