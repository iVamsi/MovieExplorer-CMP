package com.movieapp.feature.movies.screens.watchlist

import com.movieapp.core.ui.resources.Res as CoreRes
import com.movieapp.core.ui.resources.cd_back
import com.movieapp.core.ui.resources.cd_sort
import com.movieapp.core.ui.resources.sort_by_title
import com.movieapp.feature.movies.resources.Res
import com.movieapp.feature.movies.resources.watchlist_grid_movies
import com.movieapp.feature.movies.resources.watchlist_grid_series
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
import com.movieapp.core.ui.designsystem.PosterGridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movieapp.core.ui.components.MovieCard
import com.movieapp.core.ui.components.SeriesCard

@Composable
fun WatchlistGridRoute(
    category: String,
    onMovieClick: (Int) -> Unit,
    onSeriesClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    viewModel: WatchlistViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WatchlistGridScreen(
        category = category,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onMovieClick = onMovieClick,
        onSeriesClick = onSeriesClick,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistGridScreen(
    category: String,
    uiState: WatchlistUiState,
    onEvent: (WatchlistUiEvent) -> Unit,
    onMovieClick: (Int) -> Unit,
    onSeriesClick: (Int) -> Unit,
    onBackClick: () -> Unit,
) {
    var showSortSheet by remember { mutableStateOf(false) }
    val isMovies = category.equals("movies", ignoreCase = true)
    val title =
        if (isMovies) {
            stringResource(Res.string.watchlist_grid_movies)
        } else {
            stringResource(Res.string.watchlist_grid_series)
        }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CoreRes.string.cd_back),
                        )
                    }
                },
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
        if (showSortSheet) {
            ModalBottomSheet(onDismissRequest = { showSortSheet = false }) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(CoreRes.string.sort_by_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp),
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

        LazyVerticalGrid(
            columns = PosterGridCells,
            contentPadding =
                PaddingValues(
                    top = paddingValues.calculateTopPadding() + 12.dp,
                    bottom = paddingValues.calculateBottomPadding() + 12.dp,
                    start = 12.dp,
                    end = 12.dp,
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (isMovies) {
                items(uiState.watchlistMovies, key = { it.id }) { movie ->
                    MovieCard(
                        movie = movie,
                        onClick = { onMovieClick(movie.id) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                items(uiState.watchlistTvSeries, key = { it.id }) { series ->
                    SeriesCard(
                        series = series,
                        onClick = { onSeriesClick(series.id) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
