package com.movieapp.core.domain.repository

import androidx.paging.PagingData
import com.movieapp.core.common.Result
import com.movieapp.core.model.Credits
import com.movieapp.core.model.ExternalIds
import com.movieapp.core.model.Genre
import com.movieapp.core.model.ImageResponse
import com.movieapp.core.model.Keyword
import com.movieapp.core.model.Review
import com.movieapp.core.model.TvSeries
import com.movieapp.core.model.WatchProviders
import kotlinx.coroutines.flow.Flow

interface SeriesRepository {

    /** Get trending TV series */
    fun getTrendingSeries(): Flow<PagingData<TvSeries>>

    /** First page of trending TV (daily), for search spotlight and similar snapshots. */
    fun getTrendingTvSeriesPage(): Flow<Result<List<TvSeries>>>

    /** Get popular TV series */
    fun getPopularSeries(): Flow<PagingData<TvSeries>>

    /** Get top rated TV series */
    fun getTopRatedSeries(): Flow<PagingData<TvSeries>>

    /** Get TV series airing today */
    fun getAiringToday(): Flow<PagingData<TvSeries>>

    /** Get on the air TV series */
    fun getOnTheAir(): Flow<PagingData<TvSeries>>

    /** Get TV series by ID */
    suspend fun getSeriesById(seriesId: Int): Result<TvSeries>

    /** Get TV series by ID as Flow */
    fun getSeriesByIdFlow(seriesId: Int): Flow<Result<TvSeries>>

    /** Search TV series (paginated) */
    fun searchSeries(query: String, language: String): Flow<PagingData<TvSeries>>

    /** Search TV series and return the first page as a simple list (for search screens) */
    fun searchSeriesResults(query: String): Flow<Result<List<TvSeries>>>

    /** Discover TV series by filters */
    fun discoverSeries(
        year: Int? = null,
        ratingGte: Double? = null,
        genreIds: List<Int>? = null,
        watchProviderIds: List<Int>? = null,
        watchRegion: String? = null,
    ): Flow<Result<List<TvSeries>>>

    /** Get similar TV series */
    fun getSimilarSeries(seriesId: Int, language: String): Flow<PagingData<TvSeries>>

    /** Get recommended TV series */
    fun getRecommendedSeries(seriesId: Int, language: String): Flow<PagingData<TvSeries>>

    /** Get TV series by genre */
    fun getSeriesByGenre(genreId: Int): Flow<PagingData<TvSeries>>

    /** Get watchlist TV series */
    fun getWatchlistSeries(): Flow<Result<List<TvSeries>>>

    /** Get favorite TV series */
    fun getFavoriteSeries(): Flow<Result<List<TvSeries>>>

    /** Get TV series in a user collection */
    fun getSeriesInCollection(collectionId: String): Flow<Result<List<TvSeries>>>

    /** Refresh TV series data from remote */
    suspend fun refreshSeries()

    fun getTvWatchProviders(seriesId: Int): Flow<Result<WatchProviders>>

    suspend fun getTvSeriesCredits(seriesId: Int, language: String): Result<Credits>

    fun getTvSeriesReviewsPaged(seriesId: Int, language: String): Flow<PagingData<Review>>

    suspend fun getTvSeriesKeywords(seriesId: Int): Result<List<Keyword>>

    suspend fun getTvSeriesExternalIds(seriesId: Int): Result<ExternalIds>

    suspend fun getTvSeriesImages(seriesId: Int, language: String): Result<ImageResponse>

    fun getTvGenresList(language: String): Flow<Result<List<Genre>>>
}
