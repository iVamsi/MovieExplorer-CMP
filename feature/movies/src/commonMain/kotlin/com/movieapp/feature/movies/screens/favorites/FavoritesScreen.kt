package com.movieapp.feature.movies.screens.favorites

import com.movieapp.core.ui.resources.Res as CoreRes
import com.movieapp.core.ui.resources.action_see_all
import com.movieapp.core.ui.resources.cd_screen_favorites
import com.movieapp.core.ui.resources.cd_sort
import com.movieapp.core.ui.resources.sort_by_title
import com.movieapp.feature.movies.resources.Res
import com.movieapp.feature.movies.resources.favorites_discover_button
import com.movieapp.feature.movies.resources.favorites_empty_subtitle
import com.movieapp.feature.movies.resources.favorites_empty_title
import com.movieapp.feature.movies.resources.favorites_screen_title
import com.movieapp.feature.movies.resources.favorites_section_actors
import com.movieapp.feature.movies.resources.favorites_section_movies
import com.movieapp.feature.movies.resources.favorites_section_series
import com.movieapp.feature.movies.resources.library_loading
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
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
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
import com.movieapp.core.ui.components.ActorCard
import com.movieapp.core.ui.components.MovieCard
import com.movieapp.core.ui.components.SeriesCard
import com.movieapp.core.ui.designsystem.MovieAppSpacing

@Composable
fun FavoritesRoute(
    onMovieClick: (Int) -> Unit,
    onSeriesClick: (Int) -> Unit,
    onActorClick: (Int) -> Unit,
    onSeeAllMoviesClick: () -> Unit,
    onSeeAllSeriesClick: () -> Unit,
    onSeeAllActorsClick: () -> Unit,
    onDiscoverClick: () -> Unit = {},
    embeddedInTab: Boolean = false,
    viewModel: FavoritesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnMovieClick by rememberUpdatedState(onMovieClick)
    val currentOnSeriesClick by rememberUpdatedState(onSeriesClick)
    val currentOnActorClick by rememberUpdatedState(onActorClick)
    val currentOnSeeAllMoviesClick by rememberUpdatedState(onSeeAllMoviesClick)
    val currentOnSeeAllSeriesClick by rememberUpdatedState(onSeeAllSeriesClick)
    val currentOnSeeAllActorsClick by rememberUpdatedState(onSeeAllActorsClick)
    val currentOnDiscoverClick by rememberUpdatedState(onDiscoverClick)
    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is FavoritesSideEffect.NavigateToMovieDetail -> currentOnMovieClick(effect.id)
                is FavoritesSideEffect.NavigateToSeriesDetail -> currentOnSeriesClick(effect.id)
                is FavoritesSideEffect.NavigateToActorDetail -> currentOnActorClick(effect.id)
                FavoritesSideEffect.NavigateToDiscover -> currentOnDiscoverClick()
                FavoritesSideEffect.NavigateToSeeAllMovies -> currentOnSeeAllMoviesClick()
                FavoritesSideEffect.NavigateToSeeAllSeries -> currentOnSeeAllSeriesClick()
                FavoritesSideEffect.NavigateToSeeAllActors -> currentOnSeeAllActorsClick()
            }
        }
    }

    FavoritesScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        embeddedInTab = embeddedInTab,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    uiState: FavoritesUiState,
    onEvent: (FavoritesUiEvent) -> Unit,
    embeddedInTab: Boolean = false,
) {
    var showSortSheet by remember { mutableStateOf(false) }
    val hasNoFavorites =
        uiState.favoriteMovies.isEmpty() &&
            uiState.favoriteTvSeries.isEmpty() &&
            uiState.favoriteActors.isEmpty()

    val sortControl: @Composable () -> Unit = {
        IconButton(onClick = { showSortSheet = true }) {
            Icon(
                Icons.Filled.FilterList,
                contentDescription = stringResource(CoreRes.string.cd_sort),
            )
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
                item(key = "favorites_error", contentType = "status") {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            if (
                uiState.isLoading && hasNoFavorites
            ) {
                item(key = "favorites_loading", contentType = "status") {
                    Text(
                        text = stringResource(Res.string.library_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item(key = "favorite_movies_heading", contentType = "heading") {
                FavoritesSection(
                    title = stringResource(Res.string.favorites_section_movies),
                    isEmpty = uiState.favoriteMovies.isEmpty(),
                    onSeeAllClick = { onEvent(FavoritesUiEvent.SeeAllMoviesClicked) },
                )
            }

            if (uiState.favoriteMovies.isNotEmpty()) {
                item(key = "favorite_movies", contentType = "shelf") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.itemGapCompact)
                    ) {
                        items(uiState.favoriteMovies, key = { it.id }) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = { onEvent(FavoritesUiEvent.MovieClicked(movie.id)) },
                                modifier = Modifier.width(MovieAppSpacing.posterWidth),
                            )
                        }
                    }
                }
            }
            item(key = "favorite_series_heading", contentType = "heading") {
                FavoritesSection(
                    title = stringResource(Res.string.favorites_section_series),
                    isEmpty = uiState.favoriteTvSeries.isEmpty(),
                    onSeeAllClick = { onEvent(FavoritesUiEvent.SeeAllSeriesClicked) },
                )
            }

            if (uiState.favoriteTvSeries.isNotEmpty()) {
                item(key = "favorite_series", contentType = "shelf") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.itemGapCompact)
                    ) {
                        items(uiState.favoriteTvSeries, key = { it.id }) { series ->
                            SeriesCard(
                                series = series,
                                onClick = {
                                    onEvent(FavoritesUiEvent.SeriesClicked(series.id))
                                },
                                modifier = Modifier.width(MovieAppSpacing.posterWidth),
                            )
                        }
                    }
                }
            }
            item(key = "favorite_actors_heading", contentType = "heading") {
                FavoritesSection(
                    title = stringResource(Res.string.favorites_section_actors),
                    isEmpty = uiState.favoriteActors.isEmpty(),
                    onSeeAllClick = { onEvent(FavoritesUiEvent.SeeAllActorsClicked) },
                )
            }

            if (uiState.favoriteActors.isNotEmpty()) {
                item(key = "favorite_actors", contentType = "shelf") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.itemGapCompact)
                    ) {
                        items(uiState.favoriteActors, key = { it.id }) { actor ->
                            ActorCard(
                                actor = actor,
                                onClick = { onEvent(FavoritesUiEvent.ActorClicked(actor.id)) },
                                modifier = Modifier.width(MovieAppSpacing.posterWidth),
                            )
                        }
                    }
                }
            }
            if (
                !uiState.isLoading && uiState.error == null && hasNoFavorites
            ) {
                item(key = "favorites_empty", contentType = "empty") {
                    EmptyFavoritesState(
                        onDiscoverClick = { onEvent(FavoritesUiEvent.DiscoverClicked) }
                    )
                }
            }
        }
    }

    if (embeddedInTab) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                sortControl()
            }
            listContent(Modifier.weight(1f).fillMaxWidth())
        }
    } else {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0),
                    title = { Text(stringResource(Res.string.favorites_screen_title)) },
                    actions = { sortControl() },
                )
            },
        ) { paddingValues ->
            listContent(Modifier.fillMaxSize().padding(paddingValues))
        }
    }

    if (showSortSheet) {
        FavoritesSortSheet(
            currentSortOption = uiState.currentSortOption,
            onOptionSelected = {
                onEvent(FavoritesUiEvent.SortOptionSelected(it))
                showSortSheet = false
            },
            onDismiss = { showSortSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesSortSheet(
    currentSortOption: FavoritesSortOption,
    onOptionSelected: (FavoritesSortOption) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(MovieAppSpacing.medium)) {
            Text(
                stringResource(CoreRes.string.sort_by_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = MovieAppSpacing.medium),
            )
            FavoritesSortOption.entries.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = MovieAppSpacing.small),
                ) {
                    RadioButton(
                        selected = currentSortOption == option,
                        onClick = { onOptionSelected(option) },
                    )
                    Text(
                        text = stringResource(option.titleRes),
                        modifier = Modifier.padding(start = MovieAppSpacing.small),
                    )
                }
            }
            Spacer(modifier = Modifier.height(MovieAppSpacing.extraLarge))
        }
    }
}

@Composable
private fun FavoritesSection(title: String, isEmpty: Boolean, onSeeAllClick: () -> Unit) {
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
private fun EmptyFavoritesState(onDiscoverClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.sectionVertical),
    ) {
        Icon(
            Icons.Filled.FavoriteBorder,
            contentDescription = stringResource(CoreRes.string.cd_screen_favorites),
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = stringResource(Res.string.favorites_empty_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = stringResource(Res.string.favorites_empty_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Button(onClick = onDiscoverClick) {
            Icon(Icons.Filled.Explore, contentDescription = null)
            Spacer(modifier = Modifier.width(MovieAppSpacing.small))
            Text(stringResource(Res.string.favorites_discover_button))
        }
    }
}
