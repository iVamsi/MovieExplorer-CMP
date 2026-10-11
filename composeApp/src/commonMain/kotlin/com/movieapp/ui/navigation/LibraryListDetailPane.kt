package com.movieapp.ui.navigation

import androidx.compose.runtime.Composable
import com.movieapp.ui.library.LibraryTabsScreen

@Composable
fun LibraryListDetailPane(
    initialSection: LibrarySection,
    onSectionSelected: (LibrarySection) -> Unit,
    onCollectionClick: (String) -> Unit,
    onDiscoverClick: () -> Unit,
    onSeeAllMoviesClick: () -> Unit,
    onSeeAllSeriesClick: () -> Unit,
    onSeeAllFavoritesMoviesClick: () -> Unit,
    onSeeAllFavoritesSeriesClick: () -> Unit,
    onSeeAllActorsClick: () -> Unit,
    onMovieSelected: (Int) -> Unit,
    onSeriesSelected: (Int) -> Unit,
    onActorSelected: (Int) -> Unit,
) {
    MediaListDetailPane(
        onMovieSelected = onMovieSelected,
        onSeriesSelected = onSeriesSelected,
        onActorSelected = onActorSelected,
    ) { opener ->
        LibraryTabsScreen(
            initialSection = initialSection,
            onSectionSelected = onSectionSelected,
            onMovieClick = opener::openMovie,
            onSeriesClick = opener::openSeries,
            onCollectionClick = onCollectionClick,
            onDiscoverClick = onDiscoverClick,
            onSeeAllMoviesClick = onSeeAllMoviesClick,
            onSeeAllSeriesClick = onSeeAllSeriesClick,
            onSeeAllFavoritesMoviesClick = onSeeAllFavoritesMoviesClick,
            onSeeAllFavoritesSeriesClick = onSeeAllFavoritesSeriesClick,
            onActorClick = opener::openActor,
            onSeeAllActorsClick = onSeeAllActorsClick,
        )
    }
}

