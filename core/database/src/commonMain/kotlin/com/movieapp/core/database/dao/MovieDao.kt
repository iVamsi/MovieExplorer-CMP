package com.movieapp.core.database.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.movieapp.core.database.entity.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: Int): MovieEntity?

    @Query("SELECT * FROM movies WHERE id = :movieId")
    fun getMovieByIdFlow(movieId: Int): Flow<MovieEntity?>

    @Query("SELECT * FROM movies ORDER BY popularity DESC")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies ORDER BY popularity DESC")
    fun getMoviesPagingSource(): PagingSource<Int, MovieEntity>

    @Query("SELECT * FROM movies ORDER BY voteAverage DESC")
    fun getTopRatedMoviesPagingSource(): PagingSource<Int, MovieEntity>

    @Query("SELECT * FROM movies ORDER BY releaseDate DESC")
    fun getUpcomingMoviesPagingSource(): PagingSource<Int, MovieEntity>

    @Query("SELECT * FROM movies ORDER BY releaseDate DESC")
    fun getNowPlayingMoviesPagingSource(): PagingSource<Int, MovieEntity>

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%' ORDER BY popularity DESC")
    fun searchMovies(query: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%' ORDER BY popularity DESC")
    fun searchMoviesPagingSource(query: String): PagingSource<Int, MovieEntity>

    @Query("SELECT * FROM movies WHERE id IN (:movieIds) ORDER BY popularity DESC")
    fun getMoviesByIds(movieIds: List<Int>): Flow<List<MovieEntity>>

    @Query(
        "SELECT * FROM movies WHERE genreIds LIKE '%' || :genreId || '%' ORDER BY popularity DESC"
    )
    fun getMoviesByGenre(genreId: Int): Flow<List<MovieEntity>>

    @Query(
        "SELECT * FROM movies WHERE genreIds LIKE '%' || :genreId || '%' ORDER BY popularity DESC"
    )
    fun getMoviesByGenrePagingSource(genreId: Int): PagingSource<Int, MovieEntity>

    @Query(
        "SELECT * FROM movies WHERE releaseDate >= :fromDate AND releaseDate <= :toDate ORDER BY releaseDate DESC"
    )
    fun getMoviesByDateRange(fromDate: String, toDate: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE voteAverage >= :minRating ORDER BY voteAverage DESC")
    fun getMoviesByRating(minRating: Double): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE cachedAt > :timestamp ORDER BY popularity DESC")
    fun getCachedMovies(timestamp: Long): Flow<List<MovieEntity>>

    @Query("SELECT COUNT(*) FROM movies") suspend fun getMovieCount(): Int

    @Query("SELECT * FROM movies ORDER BY cachedAt DESC LIMIT :limit")
    fun getRecentMovies(limit: Int): Flow<List<MovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertMovie(movie: MovieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Update suspend fun updateMovie(movie: MovieEntity)

    @Delete suspend fun deleteMovie(movie: MovieEntity)

    @Query("DELETE FROM movies WHERE id = :movieId") suspend fun deleteMovieById(movieId: Int)

    @Query(
        """
        DELETE FROM movies
        WHERE cachedAt < :timestamp
          AND id NOT IN (SELECT contentId FROM offline_content WHERE contentType = 'MOVIE')
          AND id NOT IN (SELECT contentId FROM watchlist_items WHERE contentType = 'MOVIE')
          AND id NOT IN (SELECT contentId FROM favorite_items WHERE contentType = 'MOVIE')
          AND id NOT IN (SELECT contentId FROM collection_items WHERE contentType = 'MOVIE')
          AND id NOT IN (SELECT contentId FROM user_ratings WHERE contentType = 'MOVIE')
        """
    )
    suspend fun deleteOldMovies(timestamp: Long)

    @Query("DELETE FROM movies") suspend fun deleteAllMovies()

    @Query(
        "SELECT * FROM movies WHERE id IN (SELECT contentId FROM watchlist_items WHERE contentType = 'MOVIE') ORDER BY popularity DESC"
    )
    fun getWatchlistMovies(): Flow<List<MovieEntity>>

    @Query(
        "SELECT * FROM movies WHERE id IN (SELECT contentId FROM favorite_items WHERE contentType = 'MOVIE') ORDER BY popularity DESC"
    )
    fun getFavoriteMovies(): Flow<List<MovieEntity>>

    @Query(
        "SELECT * FROM movies WHERE id IN (SELECT contentId FROM collection_items WHERE collectionId = :collectionId AND contentType = 'MOVIE') ORDER BY popularity DESC"
    )
    fun getMoviesInCollection(collectionId: String): Flow<List<MovieEntity>>
}
