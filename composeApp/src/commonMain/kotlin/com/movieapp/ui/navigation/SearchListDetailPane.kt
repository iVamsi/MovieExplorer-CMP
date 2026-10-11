package com.movieapp.ui.navigation

import androidx.compose.runtime.Composable
import com.movieapp.feature.search.screens.search.SearchScreen

@Composable
fun SearchListDetailPane(
    onMovieSelected: (Int) -> Unit,
    onSeriesSelected: (Int) -> Unit,
    onActorSelected: (Int) -> Unit,
) {
    MediaListDetailPane(
        onMovieSelected = onMovieSelected,
        onSeriesSelected = onSeriesSelected,
        onActorSelected = onActorSelected,
    ) { opener ->
        SearchScreen(
            onMovieClick = { id -> opener.openMovie(id) },
            onSeriesClick = { id -> opener.openSeries(id) },
            onActorClick = { id -> opener.openActor(id) },
        )
    }
}

