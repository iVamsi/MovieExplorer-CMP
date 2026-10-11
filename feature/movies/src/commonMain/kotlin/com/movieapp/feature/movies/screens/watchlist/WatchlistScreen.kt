package com.movieapp.feature.movies.screens.watchlist

import com.movieapp.core.ui.resources.Res as CoreRes
import com.movieapp.core.ui.resources.action_see_all
import com.movieapp.core.ui.resources.cd_discover
import com.movieapp.core.ui.resources.cd_screen_watchlist
import com.movieapp.core.ui.resources.cd_sort
import com.movieapp.core.ui.resources.sort_by_title
import com.movieapp.feature.movies.resources.Res
import com.movieapp.feature.movies.resources.library_loading
import com.movieapp.feature.movies.resources.watchlist_discover_button
import com.movieapp.feature.movies.resources.watchlist_empty_subtitle
import com.movieapp.feature.movies.resources.watchlist_empty_title
import com.movieapp.feature.movies.resources.watchlist_screen_title
import com.movieapp.feature.movies.resources.watchlist_section_movies
import com.movieapp.feature.movies.resources.watchlist_section_tv_series
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movieapp.core.ui.components.MovieCard
import com.movieapp.core.ui.components.SeriesCard
import com.movieapp.core.ui.designsystem.MovieAppSpacing

@Composable
fun WatchlistRoute(
    onMovieClick: (Int) -> Unit,
    onSeriesClick: (Int) -> Unit,
    onDiscoverClick: () -> Unit,
    onSeeAllMoviesClick: () -> Unit,
    onSeeAllSeriesClick: () -> Unit,
    embeddedInTab: Boolean = false,
    viewModel: WatchlistViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnMovieClick by rememberUpdatedState(onMovieClick)
    val currentOnSeriesClick by rememberUpdatedState(onSeriesClick)
    val currentOnDiscoverClick by rememberUpdatedState(onDiscoverClick)
    val currentOnSeeAllMoviesClick by rememberUpdatedState(onSeeAllMoviesClick)
    val currentOnSeeAllSeriesClick by rememberUpdatedState(onSeeAllSeriesClick)
    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is WatchlistSideEffect.NavigateToMovieDetail -> currentOnMovieClick(effect.id)
                is WatchlistSideEffect.NavigateToSeriesDetail -> currentOnSeriesClick(effect.id)
                WatchlistSideEffect.NavigateToDiscover -> currentOnDiscoverClick()
                WatchlistSideEffect.NavigateToSeeAllMovies -> currentOnSeeAllMoviesClick()
                WatchlistSideEffect.NavigateToSeeAllSeries -> currentOnSeeAllSeriesClick()
            }
        }
    }

    WatchlistScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        embeddedInTab = embeddedInTab,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    uiState: WatchlistUiState,
    onEvent: (WatchlistUiEvent) -> Unit,
    embeddedInTab: Boolean = false,
) {
    var showSortSheet by remember { mutableStateOf(false) }

    val sortFilterRow: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { showSortSheet = true }) {
                Icon(
                    Icons.Filled.FilterList,
                    contentDescription = stringResource(CoreRes.string.cd_sort),
                )
            }
        }
    }

    val sortSheet: @Composable () -> Unit = {
        if (showSortSheet) {
            ModalBottomSheet(onDismissRequest = { showSortSheet = false }) {
                Column(modifier = Modifier.padding(MovieAppSpacing.medium)) {
                    Text(
                        stringResource(CoreRes.string.sort_by_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = MovieAppSpacing.medium),
                    )
                    WatchlistSortOption.entries.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        ) {
                            RadioButton(
                                selected = uiState.currentSortOption == option,
                                onClick = {
                                    onEvent(WatchlistUiEvent.SortOptionSelected(option))
                                    showSortSheet = false
                                },
                            )
                            Text(
                                text = stringResource(option.titleRes),
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    val listContent: @Composable (Modifier) -> Unit = { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding =
                PaddingValues(
                    horizontal = MovieAppSpacing.screenHorizontal,
                    vertical = MovieAppSpacing.small,
                ),
            verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.sectionVertical),
        ) {
            if (uiState.error != null) {
                item(key = "watchlist_error", contentType = "status") {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            if (
                uiState.isLoading &&
                    uiState.watchlistMovies.isEmpty() &&
                    uiState.watchlistTvSeries.isEmpty()
            ) {
                item(key = "watchlist_loading", contentType = "status") {
                    Text(
                        text = stringResource(Res.string.library_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item(key = "watchlist_movies_heading", contentType = "heading") {
                WatchlistSection(
                    title = stringResource(Res.string.watchlist_section_movies),
                    isEmpty = uiState.watchlistMovies.isEmpty(),
                    onSeeAllClick = { onEvent(WatchlistUiEvent.SeeAllMoviesClicked) },
                )
            }

            if (uiState.watchlistMovies.isNotEmpty()) {
                item(key = "watchlist_movies", contentType = "shelf") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.itemGapCompact)
                    ) {
                        items(uiState.watchlistMovies, key = { it.id }) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = { onEvent(WatchlistUiEvent.MovieClicked(movie.id)) },
                                modifier = Modifier.width(MovieAppSpacing.posterWidth),
                            )
                        }
                    }
                }
            }

            item(key = "watchlist_series_heading", contentType = "heading") {
                WatchlistSection(
                    title = stringResource(Res.string.watchlist_section_tv_series),
                    isEmpty = uiState.watchlistTvSeries.isEmpty(),
                    onSeeAllClick = { onEvent(WatchlistUiEvent.SeeAllSeriesClicked) },
                )
            }

            if (uiState.watchlistTvSeries.isNotEmpty()) {
                item(key = "watchlist_series", contentType = "shelf") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.itemGapCompact)
                    ) {
                        items(uiState.watchlistTvSeries, key = { it.id }) { series ->
                            SeriesCard(
                                series = series,
                                onClick = {
                                    onEvent(WatchlistUiEvent.SeriesClicked(series.id))
                                },
                                modifier = Modifier.width(MovieAppSpacing.posterWidth),
                            )
                        }
                    }
                }
            }

            val hasNoWatchlistItems =
                uiState.watchlistMovies.isEmpty() && uiState.watchlistTvSeries.isEmpty()
            if (!uiState.isLoading && uiState.error == null && hasNoWatchlistItems) {
                item(key = "watchlist_empty", contentType = "empty") {
                    EmptyWatchlistState(
                        onDiscoverClick = { onEvent(WatchlistUiEvent.DiscoverClicked) }
                    )
                }
            }
        }
    }

    if (embeddedInTab) {
        Column(modifier = Modifier.fillMaxSize()) {
            sortFilterRow()
            sortSheet()
            listContent(Modifier.weight(1f).fillMaxWidth())
        }
    } else {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0),
                    title = { Text(stringResource(Res.string.watchlist_screen_title)) },
                    actions = {
                        IconButton(onClick = { showSortSheet = true }) {
                            Icon(
                                Icons.Filled.FilterList,
                                contentDescription = stringResource(CoreRes.string.cd_sort),
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            sortSheet()
            listContent(Modifier.fillMaxSize().padding(paddingValues))
        }
    }
}

@Composable
private fun WatchlistSection(title: String, isEmpty: Boolean, onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        if (!isEmpty) {
            TextButton(onClick = onSeeAllClick) {
                Text(stringResource(CoreRes.string.action_see_all))
            }
        }
    }
}

@Composable
private fun EmptyWatchlistState(onDiscoverClick: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.sectionVertical),
    ) {
        Icon(
            Icons.Filled.PlaylistAdd,
            contentDescription = stringResource(CoreRes.string.cd_screen_watchlist),
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = stringResource(Res.string.watchlist_empty_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = stringResource(Res.string.watchlist_empty_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(onClick = onDiscoverClick, modifier = Modifier.padding(top = 8.dp)) {
            Icon(
                Icons.Filled.Explore,
                contentDescription = stringResource(CoreRes.string.cd_discover),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(Res.string.watchlist_discover_button))
        }
    }
}
