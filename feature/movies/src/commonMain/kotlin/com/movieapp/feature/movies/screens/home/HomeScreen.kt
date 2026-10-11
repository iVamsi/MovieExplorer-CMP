@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.movieapp.feature.movies.screens.home

import com.movieapp.core.ui.resources.Res as CoreRes
import com.movieapp.core.ui.resources.action_retry
import com.movieapp.feature.movies.resources.Res
import com.movieapp.feature.movies.resources.home_category_all
import com.movieapp.feature.movies.resources.home_category_movies
import com.movieapp.feature.movies.resources.home_category_tv
import com.movieapp.feature.movies.resources.home_discover_eyebrow
import com.movieapp.feature.movies.resources.home_discover_title
import com.movieapp.feature.movies.resources.home_nothing_to_show
import com.movieapp.feature.movies.resources.home_offline_hint
import com.movieapp.feature.movies.resources.home_section_now_playing
import com.movieapp.feature.movies.resources.home_section_popular_movies
import com.movieapp.feature.movies.resources.home_section_popular_series
import com.movieapp.feature.movies.resources.home_section_top_rated_movies
import com.movieapp.feature.movies.resources.home_section_top_rated_series
import com.movieapp.feature.movies.resources.home_section_trending_series
import com.movieapp.feature.movies.resources.home_upcoming_button
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.movieapp.core.model.Movie
import com.movieapp.core.model.TvSeries
import com.movieapp.core.ui.components.MovieCard
import com.movieapp.core.ui.components.MovieCardSkeleton
import com.movieapp.core.ui.components.SectionHeader
import com.movieapp.core.ui.components.SeriesCard
import com.movieapp.core.ui.components.SeriesCardSkeleton
import com.movieapp.core.ui.components.shimmer.rememberShimmerBrush
import com.movieapp.core.ui.designsystem.MovieAppSpacing
import com.movieapp.feature.movies.screens.browseall.ContentCategory
import kotlinx.coroutines.flow.Flow

@Composable
fun HomeScreen(
    onMovieClick: (Int) -> Unit = {},
    onSeriesClick: (Int) -> Unit = {},
    onSeeAllClick: (ContentCategory) -> Unit = {},
    onUpcomingClick: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is HomeSideEffect.NavigateToMovieDetail -> onMovieClick(effect.movieId)
                is HomeSideEffect.NavigateToSeriesDetail -> onSeriesClick(effect.seriesId)
                is HomeSideEffect.NavigateToSeeAll -> onSeeAllClick(effect.category)
            }
        }
    }

    HomeContent(
        uiState = uiState,
        nowPlayingMovies = viewModel.nowPlayingMovies,
        popularMoviesFlow = viewModel.popularMovies,
        topRatedMoviesFlow = viewModel.topRatedMovies,
        trendingSeriesFlow = viewModel.trendingSeries,
        popularSeriesFlow = viewModel.popularSeries,
        topRatedSeriesFlow = viewModel.topRatedSeries,
        onUpcomingClick = onUpcomingClick,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalLayoutApi::class,
)
@Composable
internal fun HomeContent(
    uiState: HomeUiState,
    nowPlayingMovies: Flow<PagingData<Movie>>,
    popularMoviesFlow: Flow<PagingData<Movie>>,
    topRatedMoviesFlow: Flow<PagingData<Movie>>,
    trendingSeriesFlow: Flow<PagingData<TvSeries>>,
    popularSeriesFlow: Flow<PagingData<TvSeries>>,
    topRatedSeriesFlow: Flow<PagingData<TvSeries>>,
    onUpcomingClick: () -> Unit = {},
    onEvent: (HomeUiEvent) -> Unit,
) {
    val nowPlaying = nowPlayingMovies.collectAsLazyPagingItems()
    val popular = popularMoviesFlow.collectAsLazyPagingItems()
    val topRated = topRatedMoviesFlow.collectAsLazyPagingItems()
    val trending = trendingSeriesFlow.collectAsLazyPagingItems()
    val popularSeries = popularSeriesFlow.collectAsLazyPagingItems()
    val topRatedSeries = topRatedSeriesFlow.collectAsLazyPagingItems()

    val isAnyLoading =
        uiState.isLoading ||
            nowPlaying.loadState.refresh is LoadState.Loading ||
            popular.loadState.refresh is LoadState.Loading ||
            topRated.loadState.refresh is LoadState.Loading ||
            trending.loadState.refresh is LoadState.Loading ||
            popularSeries.loadState.refresh is LoadState.Loading ||
            topRatedSeries.loadState.refresh is LoadState.Loading

    val allEmpty =
        nowPlaying.itemCount == 0 &&
            popular.itemCount == 0 &&
            topRated.itemCount == 0 &&
            trending.itemCount == 0 &&
            popularSeries.itemCount == 0 &&
            topRatedSeries.itemCount == 0

    fun shouldShowSectionEmpty(categoryVisible: Boolean, itemCount: Int) =
        !allEmpty && categoryVisible && itemCount == 0

    val homeRefreshing =
        listOf(nowPlaying, popular, topRated, trending, popularSeries, topRatedSeries).any {
            it.loadState.refresh is LoadState.Loading && it.itemCount > 0
        }

    // Paging can temporarily expose an empty snapshot during refresh. Hold the
    // resolved item so the current hero remains visible until refresh reselects it.
    var selectedSpotlight by remember(uiState.category) { mutableStateOf<SpotlightItem?>(null) }
    var spotlightRefreshPending by remember(uiState.category) { mutableStateOf(false) }
    var spotlightRefreshStarted by remember(uiState.category) { mutableStateOf(false) }
    val spotlight =
        selectSpotlight(
            category = uiState.category,
            nowPlaying = nowPlaying.itemSnapshotList.items,
            popularMovies = popular.itemSnapshotList.items,
            trendingSeries = trending.itemSnapshotList.items,
            existingKey = selectedSpotlight?.key,
        )
    LaunchedEffect(
        uiState.category,
        spotlight?.key,
        selectedSpotlight?.key,
        spotlightRefreshPending,
    ) {
        if (selectedSpotlight == null && !spotlightRefreshPending) {
            selectedSpotlight = spotlight
        }
    }
    LaunchedEffect(isAnyLoading, spotlightRefreshPending, spotlightRefreshStarted) {
        if (spotlightRefreshPending && isAnyLoading) {
            spotlightRefreshStarted = true
        } else if (spotlightRefreshPending && spotlightRefreshStarted && !isAnyLoading) {
            selectedSpotlight =
                selectSpotlight(
                    category = uiState.category,
                    nowPlaying = nowPlaying.itemSnapshotList.items,
                    popularMovies = popular.itemSnapshotList.items,
                    trendingSeries = trending.itemSnapshotList.items,
                )
            spotlightRefreshPending = false
            spotlightRefreshStarted = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = homeRefreshing,
            onRefresh = {
                spotlightRefreshPending = true
                spotlightRefreshStarted = false
                onEvent(HomeUiEvent.Refresh)
                nowPlaying.refresh()
                popular.refresh()
                topRated.refresh()
                trending.refresh()
                popularSeries.refresh()
                topRatedSeries.refresh()
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(MovieAppSpacing.medium),
                verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.large),
            ) {
                item(key = "home_header", contentType = "header") {
                    Column(verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.small)) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
                        ) {
                            Column {
                                Text(
                                    text = stringResource(Res.string.home_discover_eyebrow),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = stringResource(Res.string.home_discover_title),
                                    style = MaterialTheme.typography.headlineLargeEmphasized,
                                )
                            }
                            Button(onClick = onUpcomingClick) {
                                Text(
                                    text = stringResource(Res.string.home_upcoming_button),
                                    maxLines = 1,
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small)) {
                            HomeCategory.entries.forEach { category ->
                                val label =
                                    when (category) {
                                        HomeCategory.ALL ->
                                            stringResource(Res.string.home_category_all)
                                        HomeCategory.MOVIES ->
                                            stringResource(Res.string.home_category_movies)
                                        HomeCategory.TV -> stringResource(Res.string.home_category_tv)
                                    }
                                FilterChip(
                                    selected = uiState.category == category,
                                    onClick = { onEvent(HomeUiEvent.CategorySelected(category)) },
                                    label = { Text(label) },
                                )
                            }
                        }
                    }
                }

                val displayedSpotlight = selectedSpotlight ?: spotlight
                if (displayedSpotlight != null) {
                    item(
                        key = "spotlight_${uiState.category}_${displayedSpotlight.key}",
                        contentType = "spotlight",
                    ) {
                        val title: String
                        val year: String
                        val category: String
                        val clickEvent: HomeUiEvent
                        when (displayedSpotlight) {
                            is SpotlightItem.MovieItem -> {
                                title = displayedSpotlight.movie.title
                                year = displayedSpotlight.movie.releaseDate.orEmpty().take(4)
                                category = stringResource(Res.string.home_category_movies)
                                clickEvent = HomeUiEvent.MovieClicked(displayedSpotlight.movie.id)
                            }
                            is SpotlightItem.SeriesItem -> {
                                title = displayedSpotlight.series.name
                                year = displayedSpotlight.series.firstAirDate.orEmpty().take(4)
                                category = stringResource(Res.string.home_category_tv)
                                clickEvent = HomeUiEvent.SeriesClicked(displayedSpotlight.series.id)
                            }
                        }
                        CinematicSpotlight(
                            title = title,
                            backdropPath = displayedSpotlight.backdropPath,
                            year = year,
                            category = category,
                            onClick = { onEvent(clickEvent) },
                        )
                    }
                }

                if (uiState.category != HomeCategory.TV && nowPlaying.itemCount > 0) {
                    item(key = "now_playing_header", contentType = "section_header") {
                        SectionHeader(
                            title = stringResource(Res.string.home_section_now_playing),
                            onSeeAllClick = {
                                onEvent(HomeUiEvent.SeeAllClicked(ContentCategory.NOW_PLAYING))
                            },
                        )
                    }
                    item(key = "now_playing_shelf", contentType = "movie_shelf") {
                        LazyRow(
                            horizontalArrangement =
                                Arrangement.spacedBy(MovieAppSpacing.itemGapCompact)
                        ) {
                            items(
                                count = nowPlaying.itemCount,
                                key = nowPlaying.itemKey { "movie_${it.id}" },
                            ) { index ->
                                nowPlaying[index]?.let { movie ->
                                    LandscapeMovieCard(
                                        movie = movie,
                                        onClick = { onEvent(HomeUiEvent.MovieClicked(movie.id)) },
                                    )
                                }
                            }
                        }
                    }
                }
                if (
                    uiState.category != HomeCategory.TV &&
                        nowPlaying.itemCount == 0 &&
                        nowPlaying.loadState.refresh is LoadState.Error
                ) {
                    item(key = "now_playing_error", contentType = "error") {
                        HomeFeedError(
                            stringResource(Res.string.home_section_now_playing),
                            nowPlaying::retry,
                        )
                    }
                }
                if (
                    shouldShowSectionEmpty(
                        uiState.category != HomeCategory.TV,
                        nowPlaying.itemCount,
                    ) &&
                        nowPlaying.loadState.refresh is LoadState.NotLoading
                ) {
                    item(key = "now_playing_empty", contentType = "empty") {
                        HomeFeedEmpty(stringResource(Res.string.home_section_now_playing))
                    }
                }

                if (uiState.category != HomeCategory.TV && popular.itemCount > 0) {
                    item(key = "popular_movies_header", contentType = "section_header") {
                        SectionHeader(
                            title = stringResource(Res.string.home_section_popular_movies),
                            onSeeAllClick = {
                                onEvent(HomeUiEvent.SeeAllClicked(ContentCategory.POPULAR_MOVIES))
                            },
                        )
                    }
                    item(key = "popular_movies_shelf", contentType = "movie_shelf") {
                        LazyRow(
                            horizontalArrangement =
                                Arrangement.spacedBy(MovieAppSpacing.itemGapCompact)
                        ) {
                            items(
                                count = popular.itemCount,
                                key = popular.itemKey { "movie_${it.id}" },
                            ) { index ->
                                popular[index]?.let { movie ->
                                    MovieCard(
                                        movie = movie,
                                        onClick = { onEvent(HomeUiEvent.MovieClicked(movie.id)) },
                                        modifier = Modifier.width(MovieAppSpacing.posterWidth),
                                    )
                                }
                            }
                        }
                    }
                }
                if (
                    uiState.category != HomeCategory.TV &&
                        popular.itemCount == 0 &&
                        popular.loadState.refresh is LoadState.Error
                ) {
                    item(key = "popular_movies_error", contentType = "error") {
                        HomeFeedError(
                            stringResource(Res.string.home_section_popular_movies),
                            popular::retry,
                        )
                    }
                }
                if (
                    shouldShowSectionEmpty(
                        uiState.category != HomeCategory.TV,
                        popular.itemCount,
                    ) &&
                        popular.loadState.refresh is LoadState.NotLoading
                ) {
                    item(key = "popular_movies_empty", contentType = "empty") {
                        HomeFeedEmpty(stringResource(Res.string.home_section_popular_movies))
                    }
                }

                if (uiState.category != HomeCategory.TV && topRated.itemCount > 0) {
                    item(key = "top_rated_movies_header", contentType = "section_header") {
                        SectionHeader(
                            title = stringResource(Res.string.home_section_top_rated_movies),
                            onSeeAllClick = {
                                onEvent(HomeUiEvent.SeeAllClicked(ContentCategory.TOP_RATED_MOVIES))
                            },
                        )
                    }
                    item(key = "top_rated_movies_shelf", contentType = "movie_shelf") {
                        LazyRow(
                            horizontalArrangement =
                                Arrangement.spacedBy(MovieAppSpacing.itemGapCompact)
                        ) {
                            items(
                                count = topRated.itemCount,
                                key = topRated.itemKey { "movie_${it.id}" },
                            ) { index ->
                                topRated[index]?.let { movie ->
                                    RankedMovieCard(
                                        rank = index + 1,
                                        movie = movie,
                                        onClick = { onEvent(HomeUiEvent.MovieClicked(movie.id)) },
                                    )
                                }
                            }
                        }
                    }
                }
                if (
                    uiState.category != HomeCategory.TV &&
                        topRated.itemCount == 0 &&
                        topRated.loadState.refresh is LoadState.Error
                ) {
                    item(key = "top_rated_movies_error", contentType = "error") {
                        HomeFeedError(
                            stringResource(Res.string.home_section_top_rated_movies),
                            topRated::retry,
                        )
                    }
                }
                if (
                    shouldShowSectionEmpty(
                        uiState.category != HomeCategory.TV,
                        topRated.itemCount,
                    ) &&
                        topRated.loadState.refresh is LoadState.NotLoading
                ) {
                    item(key = "top_rated_movies_empty", contentType = "empty") {
                        HomeFeedEmpty(stringResource(Res.string.home_section_top_rated_movies))
                    }
                }

                if (uiState.category != HomeCategory.MOVIES && trending.itemCount > 0) {
                    item(key = "trending_series_header", contentType = "section_header") {
                        SectionHeader(
                            title = stringResource(Res.string.home_section_trending_series),
                            onSeeAllClick = {
                                onEvent(HomeUiEvent.SeeAllClicked(ContentCategory.TRENDING_SERIES))
                            },
                        )
                    }
                    item(key = "trending_series_shelf", contentType = "series_shelf") {
                        LazyRow(
                            horizontalArrangement =
                                Arrangement.spacedBy(MovieAppSpacing.itemGapCompact)
                        ) {
                            items(
                                count = trending.itemCount,
                                key = trending.itemKey { "series_${it.id}" },
                            ) { index ->
                                trending[index]?.let { series ->
                                    LandscapeSeriesCard(
                                        series = series,
                                        onClick = { onEvent(HomeUiEvent.SeriesClicked(series.id)) },
                                    )
                                }
                            }
                        }
                    }
                }
                if (
                    uiState.category != HomeCategory.MOVIES &&
                        trending.itemCount == 0 &&
                        trending.loadState.refresh is LoadState.Error
                ) {
                    item(key = "trending_series_error", contentType = "error") {
                        HomeFeedError(
                            stringResource(Res.string.home_section_trending_series),
                            trending::retry,
                        )
                    }
                }
                if (
                    shouldShowSectionEmpty(
                        uiState.category != HomeCategory.MOVIES,
                        trending.itemCount,
                    ) &&
                        trending.loadState.refresh is LoadState.NotLoading
                ) {
                    item(key = "trending_series_empty", contentType = "empty") {
                        HomeFeedEmpty(stringResource(Res.string.home_section_trending_series))
                    }
                }

                if (uiState.category != HomeCategory.MOVIES && popularSeries.itemCount > 0) {
                    item(key = "popular_series_header", contentType = "section_header") {
                        SectionHeader(
                            title = stringResource(Res.string.home_section_popular_series),
                            onSeeAllClick = {
                                onEvent(HomeUiEvent.SeeAllClicked(ContentCategory.POPULAR_SERIES))
                            },
                        )
                    }
                    item(key = "popular_series_shelf", contentType = "series_shelf") {
                        LazyRow(
                            horizontalArrangement =
                                Arrangement.spacedBy(MovieAppSpacing.itemGapCompact)
                        ) {
                            items(
                                count = popularSeries.itemCount,
                                key = popularSeries.itemKey { "series_${it.id}" },
                            ) { index ->
                                popularSeries[index]?.let { series ->
                                    SeriesCard(
                                        series = series,
                                        onClick = { onEvent(HomeUiEvent.SeriesClicked(series.id)) },
                                        modifier = Modifier.width(MovieAppSpacing.posterWidth),
                                    )
                                }
                            }
                        }
                    }
                }
                if (
                    uiState.category != HomeCategory.MOVIES &&
                        popularSeries.itemCount == 0 &&
                        popularSeries.loadState.refresh is LoadState.Error
                ) {
                    item(key = "popular_series_error", contentType = "error") {
                        HomeFeedError(
                            stringResource(Res.string.home_section_popular_series),
                            popularSeries::retry,
                        )
                    }
                }
                if (
                    shouldShowSectionEmpty(
                        uiState.category != HomeCategory.MOVIES,
                        popularSeries.itemCount,
                    ) &&
                        popularSeries.loadState.refresh is LoadState.NotLoading
                ) {
                    item(key = "popular_series_empty", contentType = "empty") {
                        HomeFeedEmpty(stringResource(Res.string.home_section_popular_series))
                    }
                }

                if (uiState.category != HomeCategory.MOVIES && topRatedSeries.itemCount > 0) {
                    item(key = "top_rated_series_header", contentType = "section_header") {
                        SectionHeader(
                            title = stringResource(Res.string.home_section_top_rated_series),
                            onSeeAllClick = {
                                onEvent(HomeUiEvent.SeeAllClicked(ContentCategory.TOP_RATED_SERIES))
                            },
                        )
                    }
                    item(key = "top_rated_series_shelf", contentType = "series_shelf") {
                        LazyRow(
                            horizontalArrangement =
                                Arrangement.spacedBy(MovieAppSpacing.itemGapCompact)
                        ) {
                            items(
                                count = topRatedSeries.itemCount,
                                key = topRatedSeries.itemKey { "series_${it.id}" },
                            ) { index ->
                                topRatedSeries[index]?.let { series ->
                                    RankedSeriesCard(
                                        rank = index + 1,
                                        series = series,
                                        onClick = { onEvent(HomeUiEvent.SeriesClicked(series.id)) },
                                    )
                                }
                            }
                        }
                    }
                }
                if (
                    uiState.category != HomeCategory.MOVIES &&
                        topRatedSeries.itemCount == 0 &&
                        topRatedSeries.loadState.refresh is LoadState.Error
                ) {
                    item(key = "top_rated_series_error", contentType = "error") {
                        HomeFeedError(
                            stringResource(Res.string.home_section_top_rated_series),
                            topRatedSeries::retry,
                        )
                    }
                }
                if (
                    shouldShowSectionEmpty(
                        uiState.category != HomeCategory.MOVIES,
                        topRatedSeries.itemCount,
                    ) &&
                        topRatedSeries.loadState.refresh is LoadState.NotLoading
                ) {
                    item(key = "top_rated_series_empty", contentType = "empty") {
                        HomeFeedEmpty(stringResource(Res.string.home_section_top_rated_series))
                    }
                }

                if (!isAnyLoading && allEmpty) {
                    item(key = "home_empty", contentType = "empty") {
                        Column(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .padding(vertical = MovieAppSpacing.spacious),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = stringResource(Res.string.home_nothing_to_show),
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Spacer(modifier = Modifier.height(MovieAppSpacing.small))
                            Text(
                                text = uiState.error ?: stringResource(Res.string.home_offline_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(MovieAppSpacing.sectionVertical))
                            Button(
                                onClick = {
                                    nowPlaying.refresh()
                                    popular.refresh()
                                    topRated.refresh()
                                    trending.refresh()
                                    popularSeries.refresh()
                                    topRatedSeries.refresh()
                                }
                            ) {
                                Text(stringResource(CoreRes.string.action_retry))
                            }
                        }
                    }
                }
            }
        }

        if (isAnyLoading && allEmpty) {
            HomeShimmerContent()
        }
    }
}

@Composable
private fun HomeShimmerContent() {
    LazyColumn(
        modifier =
            Modifier.fillMaxSize().testTag("home_loading_skeleton").padding(MovieAppSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.large),
    ) {
        item(key = "home_header_skeleton", contentType = "header_skeleton") {
            Column(verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.small)) {
                Box(
                    modifier =
                        Modifier.width(MovieAppSpacing.posterWidth)
                            .height(MovieAppSpacing.extraLarge)
                            .clip(MaterialTheme.shapes.small)
                            .background(rememberShimmerBrush())
                )
                Row(horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small)) {
                    repeat(3) {
                        Box(
                            modifier =
                                Modifier.width(MovieAppSpacing.personWidth)
                                    .height(MovieAppSpacing.minTouchTarget)
                                    .clip(MaterialTheme.shapes.large)
                                    .background(rememberShimmerBrush())
                        )
                    }
                }
            }
        }
        item(key = "spotlight_skeleton", contentType = "spotlight") {
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(MovieAppSpacing.detailHeroSkeletonHeightDp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(rememberShimmerBrush())
            )
        }
        repeat(3) { index ->
            item(key = "movie_shelf_skeleton_$index", contentType = "movie_shelf_skeleton") {
                Box(
                    modifier =
                        Modifier.width(MovieAppSpacing.personWidth)
                            .height(MovieAppSpacing.large)
                            .clip(MaterialTheme.shapes.small)
                            .background(rememberShimmerBrush())
                )
                Spacer(modifier = Modifier.height(MovieAppSpacing.small))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.itemGapCompact)
                ) {
                    items(5, key = { "movie_skeleton_$it" }) {
                        MovieCardSkeleton(modifier = Modifier.width(MovieAppSpacing.posterWidth))
                    }
                }
            }
        }
        repeat(2) { index ->
            item(key = "series_shelf_skeleton_$index", contentType = "series_shelf_skeleton") {
                Box(
                    modifier =
                        Modifier.width(MovieAppSpacing.personWidth)
                            .height(MovieAppSpacing.large)
                            .clip(MaterialTheme.shapes.small)
                            .background(rememberShimmerBrush())
                )
                Spacer(modifier = Modifier.height(MovieAppSpacing.small))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.itemGapCompact)
                ) {
                    items(5, key = { "series_skeleton_$it" }) {
                        SeriesCardSkeleton(modifier = Modifier.width(MovieAppSpacing.posterWidth))
                    }
                }
            }
        }
    }
}
