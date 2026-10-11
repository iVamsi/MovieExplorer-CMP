package com.movieapp.core.domain.repository

import androidx.paging.PagingData
import com.movieapp.core.common.Result
import com.movieapp.core.model.Credits
import com.movieapp.core.model.ExternalIds
import com.movieapp.core.model.Genre
import com.movieapp.core.model.ImageResponse
import com.movieapp.core.model.Keyword
import com.movieapp.core.model.Movie
import com.movieapp.core.model.MultiSearchListRow
import com.movieapp.core.model.Review
import com.movieapp.core.model.WatchProviders
import kotlinx.coroutines.flow.Flow

interface MovieRepository {

    fun getPopularMovies(page: Int = 1): Flow<Result<List<Movie>>>

    fun getPopularMoviesPaged(): Flow<PagingData<Movie>>

    fun getTopRatedMovies(page: Int = 1): Flow<Result<List<Movie>>>

    fun getTopRatedMoviesPaged(): Flow<PagingData<Movie>>

    fun getUpcomingMovies(page: Int = 1): Flow<Result<List<Movie>>>

    fun getUpcomingMoviesPaged(): Flow<PagingData<Movie>>

    fun getNowPlayingMovies(page: Int = 1): Flow<Result<List<Movie>>>

    fun getNowPlayingMoviesPaged(): Flow<PagingData<Movie>>

    fun getMovieDetails(movieId: Int): Flow<Result<Movie>>

    fun getMovieById(movieId: Int): Flow<Result<Movie>>

    fun searchMovies(query: String): Flow<Result<List<Movie>>>

    fun searchMoviesPaged(query: String, language: String, region: String?): Flow<PagingData<Movie>>

    fun multiSearchPaged(
        query: String,
        language: String,
        region: String?,
    ): Flow<PagingData<MultiSearchListRow>>

    fun discoverMovies(
        year: Int? = null,
        ratingGte: Double? = null,
        genreIds: List<Int>? = null,
        watchProviderIds: List<Int>? = null,
        watchRegion: String? = null,
    ): Flow<Result<List<Movie>>>

    fun getMoviesByGenre(genreId: Int): Flow<Result<List<Movie>>>

    fun getTrendingMovies(): Flow<Result<List<Movie>>>

    fun getWatchlistMovies(): Flow<Result<List<Movie>>>

    fun getFavoriteMovies(): Flow<Result<List<Movie>>>

    fun getMoviesInCollection(collectionId: String): Flow<Result<List<Movie>>>

    suspend fun refreshMovies()

    fun getSimilarMoviesPaged(movieId: Int, language: String): Flow<PagingData<Movie>>

    fun getRecommendedMoviesPaged(movieId: Int, language: String): Flow<PagingData<Movie>>

    fun getMovieWatchProviders(movieId: Int): Flow<Result<WatchProviders>>

    suspend fun getMovieCredits(movieId: Int, language: String): Result<Credits>

    fun getMovieReviewsPaged(movieId: Int, language: String): Flow<PagingData<Review>>

    suspend fun getMovieKeywords(movieId: Int): Result<List<Keyword>>

    suspend fun getMovieExternalIds(movieId: Int): Result<ExternalIds>

    suspend fun getMovieImages(movieId: Int, language: String): Result<ImageResponse>

    fun getMovieGenresList(language: String): Flow<Result<List<Genre>>>
}
