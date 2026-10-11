package com.movieapp.core.testing

import androidx.paging.PagingData
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.MovieRepository
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
import kotlinx.coroutines.flow.flowOf

class FakeMovieRepository : MovieRepository {
    var moviesResult: Result<List<Movie>> = Result.Success(FakeData.fakeMovies)
    var popularMoviesPagedFlow: Flow<PagingData<Movie>> = flowOf(PagingData.from(FakeData.fakeMovies))
    var topRatedMoviesPagedFlow: Flow<PagingData<Movie>> = flowOf(PagingData.from(FakeData.fakeMovies))
    var upcomingMoviesPagedFlow: Flow<PagingData<Movie>> = flowOf(PagingData.from(FakeData.fakeMovies))
    var nowPlayingMoviesPagedFlow: Flow<PagingData<Movie>> = flowOf(PagingData.from(FakeData.fakeMovies))
    var movieDetailsResult: Result<Movie> = Result.Success(FakeData.fakeMovies.first())
    var searchMoviesPaged: Flow<PagingData<Movie>> = flowOf(PagingData.from(FakeData.fakeMovies))
    var multiSearchPaged: Flow<PagingData<MultiSearchListRow>> = flowOf(PagingData.empty())
    var similarMoviesPaged: Flow<PagingData<Movie>> = flowOf(PagingData.from(FakeData.fakeMovies))
    var recommendedMoviesPaged: Flow<PagingData<Movie>> = flowOf(PagingData.from(FakeData.fakeMovies))
    var reviewsPaged: Flow<PagingData<Review>> = flowOf(PagingData.empty())
    var watchProvidersResult: Result<WatchProviders> = Result.Success(WatchProviders(emptyMap()))
    var creditsResult: Result<Credits> = Result.Success(Credits())
    var keywordsResult: Result<List<Keyword>> = Result.Success(emptyList())
    var externalIdsResult: Result<ExternalIds> = Result.Success(ExternalIds())
    var imagesResult: Result<ImageResponse> = Result.Success(ImageResponse())
    var genresResult: Result<List<Genre>> = Result.Success(FakeData.fakeGenres)
    val refreshCalls = mutableListOf<Unit>()
    val discoverCalls = mutableListOf<List<Any?>>()

    override fun getPopularMovies(page: Int): Flow<Result<List<Movie>>> = flowOf(moviesResult)
    override fun getPopularMoviesPaged(): Flow<PagingData<Movie>> = popularMoviesPagedFlow
    override fun getTopRatedMovies(page: Int): Flow<Result<List<Movie>>> = flowOf(moviesResult)
    override fun getTopRatedMoviesPaged(): Flow<PagingData<Movie>> = topRatedMoviesPagedFlow
    override fun getUpcomingMovies(page: Int): Flow<Result<List<Movie>>> = flowOf(moviesResult)
    override fun getUpcomingMoviesPaged(): Flow<PagingData<Movie>> = upcomingMoviesPagedFlow
    override fun getNowPlayingMovies(page: Int): Flow<Result<List<Movie>>> = flowOf(moviesResult)
    override fun getNowPlayingMoviesPaged(): Flow<PagingData<Movie>> = nowPlayingMoviesPagedFlow
    override fun getMovieDetails(movieId: Int): Flow<Result<Movie>> = flowOf(movieDetailsResult)
    override fun getMovieById(movieId: Int): Flow<Result<Movie>> = flowOf(movieDetailsResult)
    override fun searchMovies(query: String): Flow<Result<List<Movie>>> = flowOf(moviesResult)

    override fun searchMoviesPaged(query: String, language: String, region: String?): Flow<PagingData<Movie>> =
        searchMoviesPaged

    override fun multiSearchPaged(
        query: String,
        language: String,
        region: String?,
    ): Flow<PagingData<MultiSearchListRow>> = multiSearchPaged

    override fun discoverMovies(
        year: Int?,
        ratingGte: Double?,
        genreIds: List<Int>?,
        watchProviderIds: List<Int>?,
        watchRegion: String?,
    ): Flow<Result<List<Movie>>> {
        discoverCalls += listOf(year, ratingGte, genreIds, watchProviderIds, watchRegion)
        return flowOf(moviesResult)
    }

    override fun getMoviesByGenre(genreId: Int): Flow<Result<List<Movie>>> = flowOf(moviesResult)
    override fun getTrendingMovies(): Flow<Result<List<Movie>>> = flowOf(moviesResult)
    override fun getWatchlistMovies(): Flow<Result<List<Movie>>> = flowOf(moviesResult)
    override fun getFavoriteMovies(): Flow<Result<List<Movie>>> = flowOf(moviesResult)
    override fun getMoviesInCollection(collectionId: String): Flow<Result<List<Movie>>> = flowOf(moviesResult)

    override suspend fun refreshMovies() {
        refreshCalls += Unit
    }

    override fun getSimilarMoviesPaged(movieId: Int, language: String): Flow<PagingData<Movie>> = similarMoviesPaged

    override fun getRecommendedMoviesPaged(movieId: Int, language: String): Flow<PagingData<Movie>> =
        recommendedMoviesPaged

    override fun getMovieWatchProviders(movieId: Int): Flow<Result<WatchProviders>> = flowOf(watchProvidersResult)
    override suspend fun getMovieCredits(movieId: Int, language: String): Result<Credits> = creditsResult
    override fun getMovieReviewsPaged(movieId: Int, language: String): Flow<PagingData<Review>> = reviewsPaged
    override suspend fun getMovieKeywords(movieId: Int): Result<List<Keyword>> = keywordsResult
    override suspend fun getMovieExternalIds(movieId: Int): Result<ExternalIds> = externalIdsResult
    override suspend fun getMovieImages(movieId: Int, language: String): Result<ImageResponse> = imagesResult
    override fun getMovieGenresList(language: String): Flow<Result<List<Genre>>> = flowOf(genresResult)
}
