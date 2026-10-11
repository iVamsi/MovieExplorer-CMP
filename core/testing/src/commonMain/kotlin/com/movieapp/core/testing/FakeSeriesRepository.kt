package com.movieapp.core.testing

import androidx.paging.PagingData
import com.movieapp.core.common.Result
import com.movieapp.core.domain.repository.SeriesRepository
import com.movieapp.core.model.Credits
import com.movieapp.core.model.ExternalIds
import com.movieapp.core.model.Genre
import com.movieapp.core.model.ImageResponse
import com.movieapp.core.model.Keyword
import com.movieapp.core.model.Review
import com.movieapp.core.model.TvSeries
import com.movieapp.core.model.WatchProviders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeSeriesRepository : SeriesRepository {
    var seriesPaged: Flow<PagingData<TvSeries>> = flowOf(PagingData.from(FakeData.fakeTvSeries))
    var seriesListResult: Result<List<TvSeries>> = Result.Success(FakeData.fakeTvSeries)
    var seriesResult: Result<TvSeries> = Result.Success(FakeData.fakeTvSeries.first())
    var reviewsPaged: Flow<PagingData<Review>> = flowOf(PagingData.empty())
    var watchProvidersResult: Result<WatchProviders> = Result.Success(WatchProviders(emptyMap()))
    var creditsResult: Result<Credits> = Result.Success(Credits())
    var keywordsResult: Result<List<Keyword>> = Result.Success(emptyList())
    var externalIdsResult: Result<ExternalIds> = Result.Success(ExternalIds())
    var imagesResult: Result<ImageResponse> = Result.Success(ImageResponse())
    var genresResult: Result<List<Genre>> = Result.Success(FakeData.fakeGenres)
    val refreshCalls = mutableListOf<Unit>()
    val discoverCalls = mutableListOf<List<Any?>>()

    override fun getTrendingSeries(): Flow<PagingData<TvSeries>> = seriesPaged
    override fun getTrendingTvSeriesPage(): Flow<Result<List<TvSeries>>> = flowOf(seriesListResult)
    override fun getPopularSeries(): Flow<PagingData<TvSeries>> = seriesPaged
    override fun getTopRatedSeries(): Flow<PagingData<TvSeries>> = seriesPaged
    override fun getAiringToday(): Flow<PagingData<TvSeries>> = seriesPaged
    override fun getOnTheAir(): Flow<PagingData<TvSeries>> = seriesPaged
    override suspend fun getSeriesById(seriesId: Int): Result<TvSeries> = seriesResult
    override fun getSeriesByIdFlow(seriesId: Int): Flow<Result<TvSeries>> = flowOf(seriesResult)
    override fun searchSeries(query: String, language: String): Flow<PagingData<TvSeries>> = seriesPaged
    override fun searchSeriesResults(query: String): Flow<Result<List<TvSeries>>> = flowOf(seriesListResult)

    override fun discoverSeries(
        year: Int?,
        ratingGte: Double?,
        genreIds: List<Int>?,
        watchProviderIds: List<Int>?,
        watchRegion: String?,
    ): Flow<Result<List<TvSeries>>> {
        discoverCalls += listOf(year, ratingGte, genreIds, watchProviderIds, watchRegion)
        return flowOf(seriesListResult)
    }

    override fun getSimilarSeries(seriesId: Int, language: String): Flow<PagingData<TvSeries>> = seriesPaged
    override fun getRecommendedSeries(seriesId: Int, language: String): Flow<PagingData<TvSeries>> = seriesPaged
    override fun getSeriesByGenre(genreId: Int): Flow<PagingData<TvSeries>> = seriesPaged
    override fun getWatchlistSeries(): Flow<Result<List<TvSeries>>> = flowOf(seriesListResult)
    override fun getFavoriteSeries(): Flow<Result<List<TvSeries>>> = flowOf(seriesListResult)
    override fun getSeriesInCollection(collectionId: String): Flow<Result<List<TvSeries>>> = flowOf(seriesListResult)

    override suspend fun refreshSeries() {
        refreshCalls += Unit
    }

    override fun getTvWatchProviders(seriesId: Int): Flow<Result<WatchProviders>> = flowOf(watchProvidersResult)
    override suspend fun getTvSeriesCredits(seriesId: Int, language: String): Result<Credits> = creditsResult
    override fun getTvSeriesReviewsPaged(seriesId: Int, language: String): Flow<PagingData<Review>> = reviewsPaged
    override suspend fun getTvSeriesKeywords(seriesId: Int): Result<List<Keyword>> = keywordsResult
    override suspend fun getTvSeriesExternalIds(seriesId: Int): Result<ExternalIds> = externalIdsResult
    override suspend fun getTvSeriesImages(seriesId: Int, language: String): Result<ImageResponse> = imagesResult
    override fun getTvGenresList(language: String): Flow<Result<List<Genre>>> = flowOf(genresResult)
}
