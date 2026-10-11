@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.movieapp.feature.search.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.movieapp.core.model.Actor
import com.movieapp.core.model.Genre
import com.movieapp.core.model.Movie
import com.movieapp.core.model.MultiSearchListRow
import com.movieapp.core.model.TvSeries
import com.movieapp.core.model.WatchProvider
import com.movieapp.core.ui.resources.Res as CoreUiRes
import com.movieapp.core.ui.resources.cd_clear
import com.movieapp.core.ui.resources.cd_filters
import com.movieapp.core.ui.resources.cd_search
import com.movieapp.core.ui.resources.label_tv_series_subsection
import com.movieapp.core.ui.components.ActorCard
import com.movieapp.core.ui.components.MovieCard
import com.movieapp.core.ui.components.SeriesCard
import com.movieapp.core.ui.designsystem.MovieAppSpacing
import com.movieapp.core.ui.designsystem.PosterGridCells
import com.movieapp.feature.search.resources.Res
import com.movieapp.feature.search.resources.search_apply
import com.movieapp.feature.search.resources.search_browse_genres
import com.movieapp.feature.search.resources.search_empty_all
import com.movieapp.feature.search.resources.search_empty_headline
import com.movieapp.feature.search.resources.search_empty_hint
import com.movieapp.feature.search.resources.search_empty_movies
import com.movieapp.feature.search.resources.search_empty_people
import com.movieapp.feature.search.resources.search_empty_tv
import com.movieapp.feature.search.resources.search_error_generic
import com.movieapp.feature.search.resources.search_error_title
import com.movieapp.feature.search.resources.search_filters_title
import com.movieapp.feature.search.resources.search_genre
import com.movieapp.feature.search.resources.search_min_rating
import com.movieapp.feature.search.resources.search_no_results
import com.movieapp.feature.search.resources.search_offline_banner
import com.movieapp.feature.search.resources.search_placeholder
import com.movieapp.feature.search.resources.search_rating_any
import com.movieapp.feature.search.resources.search_recent_clear
import com.movieapp.feature.search.resources.search_recent_title
import com.movieapp.feature.search.resources.search_release_year
import com.movieapp.feature.search.resources.search_remove_filter
import com.movieapp.feature.search.resources.search_reset
import com.movieapp.feature.search.resources.search_retry
import com.movieapp.feature.search.resources.search_section_movies
import com.movieapp.feature.search.resources.search_streaming_provider
import com.movieapp.feature.search.resources.search_streaming_provider_error
import com.movieapp.feature.search.resources.search_tab_all
import com.movieapp.feature.search.resources.search_tab_movies
import com.movieapp.feature.search.resources.search_tab_people
import com.movieapp.feature.search.resources.search_tab_tv
import com.movieapp.feature.search.resources.search_title
import com.movieapp.feature.search.resources.search_trending_movies
import com.movieapp.feature.search.resources.search_trending_tv
import com.movieapp.feature.search.resources.search_try_different
import com.movieapp.feature.search.resources.search_year_error

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onMovieClick: (Int) -> Unit,
    onSeriesClick: (Int) -> Unit,
    onActorClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is SearchSideEffect.NavigateToMovieDetail -> onMovieClick(effect.movieId)
                is SearchSideEffect.NavigateToSeriesDetail -> onSeriesClick(effect.seriesId)
                is SearchSideEffect.NavigateToActorDetail -> onActorClick(effect.actorId)
            }
        }
    }

    val emitMovieClick: (Movie) -> Unit = { movie ->
        viewModel.onEvent(SearchUiEvent.MovieClicked(movie.id))
    }
    val emitSeriesClick: (TvSeries) -> Unit = { series ->
        viewModel.onEvent(SearchUiEvent.SeriesClicked(series.id))
    }
    val emitActorClick: (Actor) -> Unit = { actor ->
        viewModel.onEvent(SearchUiEvent.ActorClicked(actor.id))
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(Res.string.search_title),
            modifier =
                Modifier.padding(
                        start = MovieAppSpacing.screenHorizontal,
                        end = MovieAppSpacing.screenHorizontal,
                        top = MovieAppSpacing.medium,
                    )
                    .semantics { heading() },
            style = MaterialTheme.typography.headlineLargeEmphasized,
        )
        SearchBar(
            query = uiState.query,
            onQueryChange = { viewModel.onEvent(SearchUiEvent.QueryChanged(it)) },
            onSearch = { viewModel.onEvent(SearchUiEvent.SearchCommitted) },
            onFilterClick = { viewModel.onEvent(SearchUiEvent.FilterSheetVisibilityChanged(true)) },
            modifier = Modifier.fillMaxWidth().padding(MovieAppSpacing.medium),
        )

        if (!uiState.filter.isEmpty()) {
            AppliedFilterChips(
                filter = uiState.filter,
                genres = uiState.discoverGenres,
                providers = uiState.watchProviders,
                onRemove = { filter ->
                    viewModel.onEvent(SearchUiEvent.FilterChanged(filter))
                },
                modifier = Modifier.padding(horizontal = MovieAppSpacing.screenHorizontal),
            )
        }

        if (uiState.isOffline) {
            SearchOfflineBanner(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = MovieAppSpacing.screenHorizontal)
                        .padding(bottom = MovieAppSpacing.small)
            )
        }

        if (uiState.isFilterSheetVisible) {
            FilterBottomSheet(
                filter = uiState.filter,
                discoverGenres = uiState.discoverGenres,
                watchProviders = uiState.watchProviders,
                isWatchProvidersLoading = uiState.isWatchProvidersLoading,
                watchProvidersError = uiState.watchProvidersError,
                onFilterChange = { viewModel.onEvent(SearchUiEvent.FilterChanged(it)) },
                onDismiss = {
                    viewModel.onEvent(SearchUiEvent.FilterSheetVisibilityChanged(false))
                },
            )
        }

        when {
            uiState.query.isNotBlank() -> {
                SearchPagingTabs(
                    modifier = Modifier.weight(1f),
                    viewModel = viewModel,
                    onMovieClick = emitMovieClick,
                    onSeriesClick = emitSeriesClick,
                    onActorClick = emitActorClick,
                )
            }

            else -> {
                val discoverRefreshing = uiState.isLoading
                PullToRefreshBox(
                    isRefreshing = discoverRefreshing,
                    onRefresh = { viewModel.onEvent(SearchUiEvent.DiscoverPullRefresh) },
                    modifier = Modifier.weight(1f),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.large),
                        contentPadding =
                            PaddingValues(
                                horizontal = MovieAppSpacing.screenHorizontal,
                                vertical = MovieAppSpacing.small,
                            ),
                    ) {
                        if (uiState.recentSearchQueries.isNotEmpty()) {
                            item(key = "recent_searches", contentType = "history") {
                                RecentSearchesSection(
                                    queries = uiState.recentSearchQueries,
                                    onQueryClick = {
                                        viewModel.onEvent(SearchUiEvent.RecentSearchClick(it))
                                    },
                                    onClear = {
                                        viewModel.onEvent(SearchUiEvent.ClearRecentSearches)
                                    },
                                )
                            }
                        }
                        if (uiState.discoverGenres.isNotEmpty()) {
                            item(key = "browse_genres", contentType = "genres") {
                                SearchGenreSection(
                                    genres = uiState.discoverGenres,
                                    onGenreClick = { genre ->
                                        viewModel.onEvent(
                                            SearchUiEvent.FilterChanged(
                                                uiState.filter.copy(genreId = genre.id)
                                            )
                                        )
                                    },
                                )
                            }
                        }
                        if (
                            !uiState.filter.isEmpty() ||
                                uiState.movies.isNotEmpty() ||
                                uiState.tvSeries.isNotEmpty()
                        ) {
                            if (uiState.movies.isNotEmpty()) {
                                item(key = "discover_movies", contentType = "media_section") {
                                    SearchSection(
                                        title =
                                            stringResource(Res.string.search_section_movies),
                                        content = {
                                            LazyRow(
                                                horizontalArrangement =
                                                    Arrangement.spacedBy(
                                                        MovieAppSpacing.itemGapCompact
                                                    )
                                            ) {
                                                items(uiState.movies, key = { it.id }) { movie ->
                                                    MovieCard(
                                                        movie = movie,
                                                        onClick = { emitMovieClick(movie) },
                                                        modifier = Modifier.width(150.dp),
                                                    )
                                                }
                                            }
                                        },
                                    )
                                }
                            }

                            if (uiState.tvSeries.isNotEmpty()) {
                                item(key = "discover_tv", contentType = "media_section") {
                                    SearchSection(
                                        title =
                                            stringResource(
                                                CoreUiRes.string.label_tv_series_subsection
                                            ),
                                        content = {
                                            LazyRow(
                                                horizontalArrangement =
                                                    Arrangement.spacedBy(
                                                        MovieAppSpacing.itemGapCompact
                                                    )
                                            ) {
                                                items(uiState.tvSeries, key = { it.id }) { series ->
                                                    SeriesCard(
                                                        series = series,
                                                        onClick = { emitSeriesClick(series) },
                                                        modifier = Modifier.width(150.dp),
                                                    )
                                                }
                                            }
                                        },
                                    )
                                }
                            }

                            if (
                                uiState.movies.isEmpty() &&
                                    uiState.tvSeries.isEmpty() &&
                                    !uiState.filter.isEmpty()
                            ) {
                                item(key = "no_results", contentType = "empty") {
                                    NoResultsState(query = "")
                                }
                            }
                        } else {
                            if (uiState.filter.isEmpty() && uiState.trendingMovies.isNotEmpty()) {
                                item(key = "trending_movies", contentType = "media_section") {
                                    SearchSection(
                                        title =
                                            stringResource(Res.string.search_trending_movies),
                                        content = {
                                            LazyRow(
                                                horizontalArrangement =
                                                    Arrangement.spacedBy(
                                                        MovieAppSpacing.itemGapCompact
                                                    )
                                            ) {
                                                items(uiState.trendingMovies, key = { it.id }) {
                                                    movie ->
                                                    MovieCard(
                                                        movie = movie,
                                                        onClick = { emitMovieClick(movie) },
                                                        modifier = Modifier.width(150.dp),
                                                    )
                                                }
                                            }
                                        },
                                    )
                                }
                            }
                            if (uiState.filter.isEmpty() && uiState.trendingTvSeries.isNotEmpty()) {
                                item(key = "trending_tv", contentType = "media_section") {
                                    SearchSection(
                                        title = stringResource(Res.string.search_trending_tv),
                                        content = {
                                            LazyRow(
                                                horizontalArrangement =
                                                    Arrangement.spacedBy(
                                                        MovieAppSpacing.itemGapCompact
                                                    )
                                            ) {
                                                items(uiState.trendingTvSeries, key = { it.id }) {
                                                    series ->
                                                    SeriesCard(
                                                        series = series,
                                                        onClick = { emitSeriesClick(series) },
                                                        modifier = Modifier.width(150.dp),
                                                    )
                                                }
                                            }
                                        },
                                    )
                                }
                            }
                            val showEmptySearchPlaceholder =
                                uiState.recentSearchQueries.isEmpty() &&
                                    uiState.trendingMovies.isEmpty() &&
                                    uiState.trendingTvSeries.isEmpty() &&
                                    !uiState.isLoading
                            if (showEmptySearchPlaceholder) {
                                item(key = "empty_state", contentType = "empty") {
                                    EmptySearchState()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchGenreSection(
    genres: List<Genre>,
    onGenreClick: (Genre) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
    ) {
        Text(
            text = stringResource(Res.string.search_browse_genres),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleLargeEmphasized,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small)) {
            items(genres, key = { "genre_${it.id}" }) { genre ->
                FilterChip(
                    selected = false,
                    onClick = { onGenreClick(genre) },
                    label = { Text(genre.name) },
                )
            }
        }
    }
}

@Composable
private fun SearchPagingTabs(
    viewModel: SearchViewModel,
    onMovieClick: (Movie) -> Unit,
    onSeriesClick: (TvSeries) -> Unit,
    onActorClick: (Actor) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val tabs =
        listOf(
            stringResource(Res.string.search_tab_all),
            stringResource(Res.string.search_tab_movies),
            stringResource(Res.string.search_tab_tv),
            stringResource(Res.string.search_tab_people),
        )
    Column(modifier = modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = tab) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = tab == index, onClick = { tab = index }, text = { Text(title) })
            }
        }
        // Hoist collectors so inactive tabs keep paging state across switches.
        val multiResults = viewModel.multiSearchPaged.collectAsLazyPagingItems()
        val movieResults = viewModel.movieSearchPaged.collectAsLazyPagingItems()
        val tvResults = viewModel.tvSearchPaged.collectAsLazyPagingItems()
        val actorResults = viewModel.actorSearchPaged.collectAsLazyPagingItems()
        when (tab) {
            0 -> {
                val multiRefreshing =
                    multiResults.loadState.refresh is LoadState.Loading &&
                        multiResults.itemCount > 0
                PullToRefreshBox(
                    isRefreshing = multiRefreshing,
                    onRefresh = { multiResults.refresh() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyVerticalGrid(
                        columns = PosterGridCells,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            count = multiResults.itemCount,
                            key =
                                multiResults.itemKey { row ->
                                    when (row) {
                                        is MultiSearchListRow.MovieRow -> "movie-${row.movie.id}"
                                        is MultiSearchListRow.TvRow -> "tv-${row.series.id}"
                                        is MultiSearchListRow.PersonRow -> "person-${row.actor.id}"
                                    }
                                },
                            contentType = { index ->
                                when (multiResults[index]) {
                                    is MultiSearchListRow.MovieRow -> "movie"
                                    is MultiSearchListRow.TvRow -> "tv"
                                    is MultiSearchListRow.PersonRow -> "person"
                                    null -> "unknown"
                                }
                            },
                        ) { i ->
                            when (val row = multiResults[i]) {
                                is MultiSearchListRow.MovieRow ->
                                    MovieCard(
                                        movie = row.movie,
                                        onClick = { onMovieClick(row.movie) },
                                        modifier = Modifier.fillMaxWidth(),
                                    )

                                is MultiSearchListRow.TvRow ->
                                    SeriesCard(
                                        series = row.series,
                                        onClick = { onSeriesClick(row.series) },
                                        modifier = Modifier.fillMaxWidth(),
                                    )

                                is MultiSearchListRow.PersonRow ->
                                    ActorCard(
                                        actor = row.actor,
                                        onClick = { onActorClick(row.actor) },
                                        modifier = Modifier.fillMaxWidth(),
                                    )

                                null -> Unit
                            }
                        }
                        if (
                            multiResults.loadState.refresh is LoadState.Loading &&
                                multiResults.itemCount == 0
                        ) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    LoadingIndicator()
                                }
                            }
                        }
                        if (
                            multiResults.loadState.refresh is LoadState.NotLoading &&
                                multiResults.loadState.append.endOfPaginationReached &&
                                multiResults.itemCount == 0
                        ) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = stringResource(Res.string.search_empty_all),
                                    modifier = Modifier.padding(MovieAppSpacing.medium),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        val multiError = multiResults.loadState.refresh
                        if (multiError is LoadState.Error && multiResults.itemCount == 0) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                PagingErrorItem(
                                    error = multiError.error,
                                    onRetry = { multiResults.retry() },
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                val movieRefreshing =
                    movieResults.loadState.refresh is LoadState.Loading &&
                        movieResults.itemCount > 0
                PullToRefreshBox(
                    isRefreshing = movieRefreshing,
                    onRefresh = { movieResults.refresh() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyVerticalGrid(
                        columns = PosterGridCells,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            count = movieResults.itemCount,
                            key = movieResults.itemKey { it.id },
                        ) { i ->
                            movieResults[i]?.let { movie ->
                                MovieCard(
                                    movie = movie,
                                    onClick = { onMovieClick(movie) },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        if (
                            movieResults.loadState.refresh is LoadState.Loading &&
                                movieResults.itemCount == 0
                        ) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    LoadingIndicator()
                                }
                            }
                        }
                        if (
                            movieResults.loadState.refresh is LoadState.NotLoading &&
                                movieResults.loadState.append.endOfPaginationReached &&
                                movieResults.itemCount == 0
                        ) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = stringResource(Res.string.search_empty_movies),
                                    modifier = Modifier.padding(MovieAppSpacing.medium),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        val movieError = movieResults.loadState.refresh
                        if (movieError is LoadState.Error && movieResults.itemCount == 0) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                PagingErrorItem(
                                    error = movieError.error,
                                    onRetry = { movieResults.retry() },
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                val tvRefreshing =
                    tvResults.loadState.refresh is LoadState.Loading && tvResults.itemCount > 0
                PullToRefreshBox(
                    isRefreshing = tvRefreshing,
                    onRefresh = { tvResults.refresh() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyVerticalGrid(
                        columns = PosterGridCells,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(count = tvResults.itemCount, key = tvResults.itemKey { it.id }) { i ->
                            tvResults[i]?.let { series ->
                                SeriesCard(
                                    series = series,
                                    onClick = { onSeriesClick(series) },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        if (
                            tvResults.loadState.refresh is LoadState.Loading &&
                                tvResults.itemCount == 0
                        ) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    LoadingIndicator()
                                }
                            }
                        }
                        if (
                            tvResults.loadState.refresh is LoadState.NotLoading &&
                                tvResults.loadState.append.endOfPaginationReached &&
                                tvResults.itemCount == 0
                        ) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = stringResource(Res.string.search_empty_tv),
                                    modifier = Modifier.padding(MovieAppSpacing.medium),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        val tvError = tvResults.loadState.refresh
                        if (tvError is LoadState.Error && tvResults.itemCount == 0) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                PagingErrorItem(
                                    error = tvError.error,
                                    onRetry = { tvResults.retry() },
                                )
                            }
                        }
                    }
                }
            }

            else -> {
                val peopleRefreshing =
                    actorResults.loadState.refresh is LoadState.Loading &&
                        actorResults.itemCount > 0
                PullToRefreshBox(
                    isRefreshing = peopleRefreshing,
                    onRefresh = { actorResults.refresh() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyVerticalGrid(
                        columns = PosterGridCells,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            count = actorResults.itemCount,
                            key = actorResults.itemKey { it.id },
                        ) { i ->
                            actorResults[i]?.let { actor ->
                                ActorCard(
                                    actor = actor,
                                    onClick = { onActorClick(actor) },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        if (
                            actorResults.loadState.refresh is LoadState.Loading &&
                                actorResults.itemCount == 0
                        ) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    LoadingIndicator()
                                }
                            }
                        }
                        if (
                            actorResults.loadState.refresh is LoadState.NotLoading &&
                                actorResults.loadState.append.endOfPaginationReached &&
                                actorResults.itemCount == 0
                        ) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = stringResource(Res.string.search_empty_people),
                                    modifier = Modifier.padding(MovieAppSpacing.medium),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        val actorError = actorResults.loadState.refresh
                        if (actorError is LoadState.Error && actorResults.itemCount == 0) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                PagingErrorItem(
                                    error = actorError.error,
                                    onRetry = { actorResults.retry() },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PagingErrorItem(error: Throwable, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(MovieAppSpacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
    ) {
        Icon(
            imageVector = Icons.Outlined.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(Res.string.search_error_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = error.message ?: stringResource(Res.string.search_error_generic),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onRetry) { Text(text = stringResource(Res.string.search_retry)) }
    }
}

private val genreMap =
    mapOf(
        28 to "Action",
        12 to "Adventure",
        16 to "Animation",
        35 to "Comedy",
        80 to "Crime",
        99 to "Documentary",
        18 to "Drama",
        10751 to "Family",
        14 to "Fantasy",
        36 to "History",
        27 to "Horror",
        10402 to "Music",
        9648 to "Mystery",
        10749 to "Romance",
        878 to "Sci-Fi",
        10770 to "TV Movie",
        53 to "Thriller",
        10752 to "War",
        37 to "Western",
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    filter: SearchFilter,
    discoverGenres: List<Genre>,
    watchProviders: List<WatchProvider>,
    isWatchProvidersLoading: Boolean,
    watchProvidersError: String?,
    onFilterChange: (SearchFilter) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        FilterSheetContent(
            initialFilter = filter,
            discoverGenres = discoverGenres,
            watchProviders = watchProviders,
            isWatchProvidersLoading = isWatchProvidersLoading,
            watchProvidersError = watchProvidersError,
            onApply = {
                onFilterChange(it)
                onDismiss()
            },
        )
    }
}

@Composable
internal fun FilterSheetContent(
    initialFilter: SearchFilter,
    discoverGenres: List<Genre>,
    watchProviders: List<WatchProvider>,
    isWatchProvidersLoading: Boolean,
    watchProvidersError: String?,
    onApply: (SearchFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by
        rememberSaveable(initialFilter, stateSaver = SearchFilterSaver) {
            mutableStateOf(initialFilter)
        }
    var yearText by
        rememberSaveable(initialFilter) {
            mutableStateOf(initialFilter.year?.toString().orEmpty())
        }
    val yearValid = isValidFilterYear(yearText)

    Column(
        modifier = modifier.fillMaxWidth().padding(MovieAppSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.sectionVertical),
    ) {
        Column(
            modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.sectionVertical),
        ) {
            Text(
                text = stringResource(Res.string.search_filters_title),
                style = MaterialTheme.typography.titleLargeEmphasized,
            )

            val anyRatingLabel = stringResource(Res.string.search_rating_any)
            Text(
                text =
                    stringResource(
                        Res.string.search_min_rating,
                        draft.ratingGte?.toString() ?: anyRatingLabel,
                    ),
                style = MaterialTheme.typography.titleSmall,
            )
            Slider(
                value = draft.ratingGte?.toFloat() ?: 0f,
                onValueChange = {
                    draft = draft.copy(ratingGte = if (it == 0f) null else it.toDouble())
                },
                valueRange = 0f..10f,
                steps = 9,
            )

            // Genres (Chips row)
            Text(
                text = stringResource(Res.string.search_genre),
                style = MaterialTheme.typography.titleSmall,
            )
            val genreOptions =
                if (discoverGenres.isNotEmpty()) {
                    discoverGenres
                } else {
                    genreMap.entries.map { (id, name) -> Genre(id = id, name = name) }
                }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small)) {
                item {
                    FilterChip(
                        selected = draft.genreId == null,
                        onClick = { draft = draft.copy(genreId = null) },
                        label = { Text(stringResource(Res.string.search_tab_all)) },
                    )
                }
                items(genreOptions, key = { it.id }) { genre ->
                    FilterChip(
                        selected = draft.genreId == genre.id,
                        onClick = { draft = draft.copy(genreId = genre.id) },
                        label = { Text(genre.name) },
                    )
                }
            }

            Text(
                text = stringResource(Res.string.search_streaming_provider),
                style = MaterialTheme.typography.titleSmall,
            )
            when {
                isWatchProvidersLoading -> {
                    LoadingIndicator(modifier = Modifier.size(32.dp))
                }
                watchProvidersError != null -> {
                    Text(
                        text = stringResource(Res.string.search_streaming_provider_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                watchProviders.isNotEmpty() -> {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small)) {
                        item {
                            FilterChip(
                                selected = draft.watchProviderIds.isEmpty(),
                                onClick = {
                                    draft = draft.copy(watchProviderIds = emptySet())
                                },
                                label = { Text(stringResource(Res.string.search_rating_any)) },
                            )
                        }
                        items(watchProviders, key = { it.providerId }) { provider ->
                            val selected = provider.providerId in draft.watchProviderIds
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    val nextProviders =
                                        draft.watchProviderIds.toMutableSet().apply {
                                            if (selected) {
                                                remove(provider.providerId)
                                            } else {
                                                add(provider.providerId)
                                            }
                                        }
                                    draft = draft.copy(watchProviderIds = nextProviders.toSet())
                                },
                                label = { Text(provider.providerName) },
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = yearText,
                onValueChange = {
                    yearText = it
                    draft = draft.copy(year = it.toIntOrNull())
                },
                label = { Text(stringResource(Res.string.search_release_year)) },
                isError = !yearValid,
                supportingText =
                    if (!yearValid) {
                        { Text(stringResource(Res.string.search_year_error)) }
                    } else {
                        null
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(
                onClick = {
                    draft = SearchFilter()
                    yearText = ""
                }
            ) {
                Text(stringResource(Res.string.search_reset))
            }
            Button(
                enabled = yearValid,
                onClick = { onApply(draft.copy(year = yearText.toIntOrNull())) },
            ) {
                Text(stringResource(Res.string.search_apply))
            }
        }
    }
}
private val SearchFilterSaver =
    listSaver<SearchFilter, Int>(
        save = { filter ->
            listOf(
                filter.year ?: -1,
                ((filter.ratingGte ?: -1.0) * 10).toInt(),
                filter.genreId ?: -1,
            ) + filter.watchProviderIds
        },
        restore = { values ->
            SearchFilter(
                year = values[0].takeIf { it >= 0 },
                ratingGte = values[1].takeIf { it >= 0 }?.div(10.0),
                genreId = values[2].takeIf { it >= 0 },
                watchProviderIds = values.drop(3).toSet(),
            )
        },
    )

@Composable
internal fun AppliedFilterChips(
    filter: SearchFilter,
    genres: List<Genre>,
    providers: List<WatchProvider>,
    onRemove: (SearchFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
    ) {
        filter.year?.let { year ->
            item(key = "year", contentType = "filter") {
                RemovableFilterChip(
                    label = year.toString(),
                    onClick = { onRemove(filter.without(SearchFilterField.YEAR)) },
                )
            }
        }
        filter.ratingGte?.let { rating ->
            item(key = "rating", contentType = "filter") {
                RemovableFilterChip(
                    label = rating.toString(),
                    onClick = { onRemove(filter.without(SearchFilterField.RATING)) },
                )
            }
        }
        filter.genreId?.let { id ->
            item(key = "genre", contentType = "filter") {
                val label = genres.firstOrNull { it.id == id }?.name ?: id.toString()
                RemovableFilterChip(
                    label = label,
                    onClick = { onRemove(filter.without(SearchFilterField.GENRE)) },
                )
            }
        }
        items(filter.watchProviderIds.sorted(), key = { "provider-$it" }) { providerId ->
            val label =
                providers.firstOrNull { it.providerId == providerId }?.providerName
                    ?: providerId.toString()
            RemovableFilterChip(
                label = label,
                onClick = {
                    onRemove(filter.copy(watchProviderIds = filter.watchProviderIds - providerId))
                },
            )
        }
    }
}

@Composable
private fun RemovableFilterChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val removeLabel = stringResource(Res.string.search_remove_filter, label)
    FilterChip(
        selected = true,
        onClick = onClick,
        label = { Text(text = label, maxLines = 2) },
        modifier = modifier,
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = removeLabel,
            )
        },
    )
}

@Composable
private fun SearchOfflineBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(MovieAppSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.CloudOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(Res.string.search_offline_banner),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RecentSearchesSection(
    queries: List<String>,
    onQueryClick: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.small),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.search_recent_title),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleSmallEmphasized,
            )
            TextButton(onClick = onClear) {
                Text(stringResource(Res.string.search_recent_clear))
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(MovieAppSpacing.small)) {
            items(queries, key = { it }) { query ->
                FilterChip(
                    selected = false,
                    onClick = { onQueryClick(query) },
                    label = {
                        Text(
                            text = query,
                            maxLines = 1,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text(stringResource(Res.string.search_placeholder)) },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = stringResource(CoreUiRes.string.cd_search),
            )
        },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = stringResource(CoreUiRes.string.cd_clear),
                        )
                    }
                }
                IconButton(onClick = onFilterClick) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = stringResource(CoreUiRes.string.cd_filters),
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge,
        keyboardOptions =
            KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
    )
}

@Composable
private fun SearchSection(
    title: String,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.itemGapCompact),
    ) {
        Text(
            text = title,
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleLargeEmphasized,
        )
        content()
    }
}

@Composable
private fun EmptySearchState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.sectionVertical),
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = stringResource(CoreUiRes.string.cd_search),
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = stringResource(Res.string.search_empty_headline),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = stringResource(Res.string.search_empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NoResultsState(query: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MovieAppSpacing.sectionVertical),
    ) {
        Text(
            text = stringResource(Res.string.search_no_results, query),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = stringResource(Res.string.search_try_different),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
