package com.movieapp.ui.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.movieapp.app.resources.Res
import com.movieapp.app.resources.library_tab_collections
import com.movieapp.app.resources.library_tab_favorites
import com.movieapp.app.resources.library_tab_watchlist
import com.movieapp.app.resources.nav_library
import com.movieapp.feature.collections.screens.collections.CollectionsScreen
import com.movieapp.feature.movies.screens.favorites.FavoritesRoute
import com.movieapp.feature.movies.screens.watchlist.WatchlistRoute
import com.movieapp.ui.navigation.LibrarySection
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryTabsScreen(
    initialSection: LibrarySection,
    onSectionSelected: (LibrarySection) -> Unit,
    onMovieClick: (Int) -> Unit,
    onSeriesClick: (Int) -> Unit,
    onCollectionClick: (String) -> Unit,
    onDiscoverClick: () -> Unit,
    onSeeAllMoviesClick: () -> Unit,
    onSeeAllSeriesClick: () -> Unit,
    onSeeAllFavoritesMoviesClick: () -> Unit,
    onSeeAllFavoritesSeriesClick: () -> Unit,
    onActorClick: (Int) -> Unit,
    onSeeAllActorsClick: () -> Unit,
) {
    var selectedSection by rememberSaveable { mutableStateOf(initialSection) }
    val pagerState =
        rememberPagerState(
            initialPage = LibrarySection.entries.indexOf(selectedSection),
            pageCount = { LibrarySection.entries.size },
        )
    val scope = rememberCoroutineScope()

    fun select(section: LibrarySection) {
        selectedSection = section
        onSectionSelected(section)
        scope.launch { pagerState.animateScrollToPage(LibrarySection.entries.indexOf(section)) }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page ->
                val section = LibrarySection.entries[page]
                if (selectedSection != section) {
                    selectedSection = section
                    onSectionSelected(section)
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(Res.string.nav_library),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        LibrarySectionTabs(
            selectedSection = LibrarySection.entries[pagerState.currentPage],
            onSectionSelected = ::select,
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f),
            beyondViewportPageCount = 1,
        ) { page ->
            when (LibrarySection.entries[page]) {
                LibrarySection.Watchlist ->
                    WatchlistRoute(
                        onMovieClick = onMovieClick,
                        onSeriesClick = onSeriesClick,
                        onDiscoverClick = onDiscoverClick,
                        onSeeAllMoviesClick = onSeeAllMoviesClick,
                        onSeeAllSeriesClick = onSeeAllSeriesClick,
                        embeddedInTab = true,
                    )
                LibrarySection.Collections ->
                    CollectionsScreen(
                        onMovieClick = onMovieClick,
                        onSeriesClick = onSeriesClick,
                        onCollectionClick = onCollectionClick,
                    )
                LibrarySection.Favorites ->
                    FavoritesRoute(
                        onMovieClick = onMovieClick,
                        onSeriesClick = onSeriesClick,
                        onActorClick = onActorClick,
                        onSeeAllMoviesClick = onSeeAllFavoritesMoviesClick,
                        onSeeAllSeriesClick = onSeeAllFavoritesSeriesClick,
                        onSeeAllActorsClick = onSeeAllActorsClick,
                        onDiscoverClick = onDiscoverClick,
                        embeddedInTab = true,
                    )
            }
        }
    }
}

@Composable
internal fun LibrarySectionTabs(
    selectedSection: LibrarySection,
    onSectionSelected: (LibrarySection) -> Unit,
) {
    PrimaryScrollableTabRow(
        selectedTabIndex = LibrarySection.entries.indexOf(selectedSection),
        edgePadding = 0.dp,
    ) {
        LibrarySection.entries.forEach { section ->
            Tab(
                selected = selectedSection == section,
                onClick = { onSectionSelected(section) },
                text = {
                    Text(
                        text =
                            stringResource(
                                when (section) {
                                    LibrarySection.Watchlist -> Res.string.library_tab_watchlist
                                    LibrarySection.Collections -> Res.string.library_tab_collections
                                    LibrarySection.Favorites -> Res.string.library_tab_favorites
                                }
                            ),
                        maxLines = 1,
                        softWrap = false,
                    )
                },
            )
        }
    }
}

