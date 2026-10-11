@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.movieapp.feature.movies.screens.browseall

import com.movieapp.core.ui.resources.Res as CoreRes
import com.movieapp.core.ui.resources.action_retry
import com.movieapp.core.ui.resources.browse_empty_message
import com.movieapp.core.ui.resources.browse_empty_title
import com.movieapp.core.ui.resources.browse_load_error
import com.movieapp.core.ui.resources.cd_back
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import com.movieapp.core.ui.designsystem.PosterGridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import org.koin.core.parameter.parametersOf
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.movieapp.core.ui.components.AppEmptyState
import com.movieapp.core.ui.components.AppInlineErrorState
import com.movieapp.core.ui.components.MovieCard
import com.movieapp.core.ui.components.MovieCardSkeleton
import com.movieapp.core.ui.components.SeriesCard
import com.movieapp.core.ui.components.SeriesCardSkeleton
import com.movieapp.core.ui.designsystem.MovieAppSpacing

@Composable
fun BrowseAllRoute(
    category: String,
    onMovieClick: (Int) -> Unit,
    onSeriesClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    viewModel: BrowseAllViewModel = koinViewModel { parametersOf(category) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val items = viewModel.items.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is BrowseAllSideEffect.NavigateToMovieDetail -> onMovieClick(effect.movieId)
                is BrowseAllSideEffect.NavigateToSeriesDetail -> onSeriesClick(effect.seriesId)
                BrowseAllSideEffect.NavigateBack -> onBackClick()
            }
        }
    }

    BrowseAllScreen(uiState = uiState, items = items, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseAllScreen(
    uiState: BrowseAllUiState,
    items: LazyPagingItems<BrowseItem>,
    onEvent: (BrowseAllUiEvent) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = {
                    Text(
                        stringResource(uiState.category.displayTitle),
                        style = MaterialTheme.typography.titleLargeEmphasized,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(BrowseAllUiEvent.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CoreRes.string.cd_back),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        when {
            items.loadState.refresh is LoadState.Loading && items.itemCount == 0 -> {
                val isSeries = uiState.category.isSeries
                LazyVerticalGrid(
                    columns = PosterGridCells,
                    modifier =
                        Modifier.fillMaxSize()
                            .padding(horizontal = MovieAppSpacing.screenHorizontal),
                    contentPadding =
                        PaddingValues(
                            top = paddingValues.calculateTopPadding() + MovieAppSpacing.small,
                            bottom = paddingValues.calculateBottomPadding() + MovieAppSpacing.small,
                            start = 0.dp,
                            end = 0.dp,
                        ),
                    horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.itemGapCompact),
                    verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.itemGapCompact),
                ) {
                    items(
                        count = 12,
                        key = { "initial_skeleton_$it" },
                        contentType = { "skeleton" },
                    ) {
                        if (isSeries) {
                            SeriesCardSkeleton()
                        } else {
                            MovieCardSkeleton()
                        }
                    }
                }
            }

            items.loadState.refresh is LoadState.Error && items.itemCount == 0 -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.sectionVertical),
                    ) {
                        Text(
                            text = stringResource(CoreRes.string.browse_load_error),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Button(onClick = { items.retry() }) {
                            Text(stringResource(CoreRes.string.action_retry))
                        }
                    }
                }
            }

            else -> {
                val listRefreshing =
                    items.loadState.refresh is LoadState.Loading && items.itemCount > 0
                PullToRefreshBox(
                    isRefreshing = listRefreshing,
                    onRefresh = { items.refresh() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyVerticalGrid(
                        columns = PosterGridCells,
                        contentPadding =
                            PaddingValues(
                                top = paddingValues.calculateTopPadding() + MovieAppSpacing.medium,
                                bottom =
                                    paddingValues.calculateBottomPadding() + MovieAppSpacing.medium,
                                start = MovieAppSpacing.screenHorizontal,
                                end = MovieAppSpacing.screenHorizontal,
                            ),
                        horizontalArrangement =
                            Arrangement.spacedBy(MovieAppSpacing.itemGapCompact),
                        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.itemGapCompact),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (items.loadState.refresh is LoadState.Error) {
                            item(
                                key = "refresh_error",
                                contentType = "error",
                                span = { GridItemSpan(maxLineSpan) },
                            ) {
                                AppInlineErrorState(
                                    message = stringResource(CoreRes.string.browse_load_error),
                                    onRetry = items::retry,
                                )
                            }
                        } else if (
                            items.itemCount == 0 && items.loadState.refresh is LoadState.NotLoading
                        ) {
                            item(
                                key = "empty",
                                contentType = "empty",
                                span = { GridItemSpan(maxLineSpan) },
                            ) {
                                AppEmptyState(
                                    title = stringResource(CoreRes.string.browse_empty_title),
                                    message = stringResource(CoreRes.string.browse_empty_message),
                                )
                            }
                        }

                        items(
                            count = items.itemCount,
                            key =
                                items.itemKey { item ->
                                    when (item) {
                                        is BrowseItem.MovieItem -> "movie_${item.movie.id}"
                                        is BrowseItem.SeriesItem -> "series_${item.series.id}"
                                    }
                                },
                            contentType =
                                items.itemContentType { item ->
                                    when (item) {
                                        is BrowseItem.MovieItem -> "movie"
                                        is BrowseItem.SeriesItem -> "series"
                                    }
                                },
                        ) { index ->
                            items[index]?.let { browseItem ->
                                when (browseItem) {
                                    is BrowseItem.MovieItem ->
                                        MovieCard(
                                            movie = browseItem.movie,
                                            onClick = {
                                                onEvent(
                                                    BrowseAllUiEvent.MovieClicked(
                                                        browseItem.movie.id
                                                    )
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                        )

                                    is BrowseItem.SeriesItem ->
                                        SeriesCard(
                                            series = browseItem.series,
                                            onClick = {
                                                onEvent(
                                                    BrowseAllUiEvent.SeriesClicked(
                                                        browseItem.series.id
                                                    )
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                }
                            }
                        }

                        if (items.loadState.append is LoadState.Loading) {
                            val isSeries = uiState.category.isSeries
                            items(
                                count = 3,
                                key = { "append_skeleton_$it" },
                                contentType = { "skeleton" },
                            ) {
                                if (isSeries) {
                                    SeriesCardSkeleton()
                                } else {
                                    MovieCardSkeleton()
                                }
                            }
                        } else if (items.loadState.append is LoadState.Error) {
                            item(
                                key = "append_error",
                                contentType = "error",
                                span = { GridItemSpan(maxLineSpan) },
                            ) {
                                AppInlineErrorState(
                                    message = stringResource(CoreRes.string.browse_load_error),
                                    onRetry = items::retry,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
