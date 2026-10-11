package com.movieapp.core.database.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.movieapp.core.database.entity.TvSeriesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TvSeriesDao {

    @Query("SELECT * FROM tv_series WHERE id = :tvId")
    suspend fun getTvSeriesById(tvId: Int): TvSeriesEntity?

    @Query("SELECT * FROM tv_series WHERE id = :tvId")
    fun getTvSeriesByIdFlow(tvId: Int): Flow<TvSeriesEntity?>

    @Query("SELECT * FROM tv_series ORDER BY popularity DESC")
    fun getAllTvSeries(): Flow<List<TvSeriesEntity>>

    @Query("SELECT * FROM tv_series ORDER BY popularity DESC")
    fun getTvSeriesPagingSource(): PagingSource<Int, TvSeriesEntity>

    @Query("SELECT * FROM tv_series WHERE category = :category ORDER BY popularity DESC")
    fun getTvSeriesByCategoryPagingSource(category: String): PagingSource<Int, TvSeriesEntity>

    @Query("DELETE FROM tv_series WHERE category = :category")
    suspend fun deleteByCategory(category: String)

    @Query("SELECT * FROM tv_series WHERE name LIKE '%' || :query || '%' ORDER BY popularity DESC")
    fun searchTvSeries(query: String): Flow<List<TvSeriesEntity>>

    @Query("SELECT * FROM tv_series WHERE name LIKE '%' || :query || '%' ORDER BY popularity DESC")
    fun searchTvSeriesPagingSource(query: String): PagingSource<Int, TvSeriesEntity>

    @Query("SELECT * FROM tv_series WHERE id IN (:tvIds) ORDER BY popularity DESC")
    fun getTvSeriesByIds(tvIds: List<Int>): Flow<List<TvSeriesEntity>>

    @Query(
        "SELECT * FROM tv_series WHERE genreIds LIKE '%' || :genreId || '%' ORDER BY popularity DESC"
    )
    fun getTvSeriesByGenre(genreId: Int): Flow<List<TvSeriesEntity>>

    @Query(
        "SELECT * FROM tv_series WHERE genreIds LIKE '%' || :genreId || '%' ORDER BY popularity DESC"
    )
    fun getTvSeriesByGenrePagingSource(genreId: Int): PagingSource<Int, TvSeriesEntity>

    @Query(
        "SELECT * FROM tv_series WHERE firstAirDate >= :fromDate AND firstAirDate <= :toDate ORDER BY firstAirDate DESC"
    )
    fun getTvSeriesByDateRange(fromDate: String, toDate: String): Flow<List<TvSeriesEntity>>

    @Query("SELECT * FROM tv_series WHERE voteAverage >= :minRating ORDER BY voteAverage DESC")
    fun getTvSeriesByRating(minRating: Double): Flow<List<TvSeriesEntity>>

    @Query("SELECT * FROM tv_series WHERE cachedAt > :timestamp ORDER BY popularity DESC")
    fun getCachedTvSeries(timestamp: Long): Flow<List<TvSeriesEntity>>

    @Query("SELECT COUNT(*) FROM tv_series") suspend fun getTvSeriesCount(): Int

    @Query("SELECT * FROM tv_series ORDER BY cachedAt DESC LIMIT :limit")
    fun getRecentTvSeries(limit: Int): Flow<List<TvSeriesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTvSeries(tvSeries: TvSeriesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTvSeriesList(tvSeriesList: List<TvSeriesEntity>)

    @Update suspend fun updateTvSeries(tvSeries: TvSeriesEntity)

    @Delete suspend fun deleteTvSeries(tvSeries: TvSeriesEntity)

    @Query("DELETE FROM tv_series WHERE id = :tvId") suspend fun deleteTvSeriesById(tvId: Int)

    @Query("DELETE FROM tv_series WHERE cachedAt < :timestamp")
    suspend fun deleteOldTvSeries(timestamp: Long)

    @Query("DELETE FROM tv_series") suspend fun deleteAllTvSeries()

    @Query(
        "SELECT * FROM tv_series WHERE id IN (SELECT contentId FROM watchlist_items WHERE contentType = 'TV_SERIES') ORDER BY popularity DESC"
    )
    fun getWatchlistTvSeries(): Flow<List<TvSeriesEntity>>

    @Query(
        "SELECT * FROM tv_series WHERE id IN (SELECT contentId FROM favorite_items WHERE contentType = 'TV_SERIES') ORDER BY popularity DESC"
    )
    fun getFavoriteTvSeries(): Flow<List<TvSeriesEntity>>

    @Query(
        "SELECT * FROM tv_series WHERE id IN (SELECT contentId FROM collection_items WHERE collectionId = :collectionId AND contentType = 'TV_SERIES') ORDER BY popularity DESC"
    )
    fun getTvSeriesInCollection(collectionId: String): Flow<List<TvSeriesEntity>>
}
