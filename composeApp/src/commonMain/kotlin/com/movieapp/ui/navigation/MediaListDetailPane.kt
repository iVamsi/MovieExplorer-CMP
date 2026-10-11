package com.movieapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

interface MediaDetailOpener {
    fun openMovie(id: Int)

    fun openSeries(id: Int)

    fun openActor(id: Int)
}

@Composable
fun MediaListDetailPane(
    onMovieSelected: (Int) -> Unit,
    onSeriesSelected: (Int) -> Unit,
    onActorSelected: (Int) -> Unit,
    listPane: @Composable (opener: MediaDetailOpener) -> Unit,
) {
    // shortcut: shared adaptive navigation lacks iOS parity, restore embedded detail panes when it does.
    val opener =
        remember(onMovieSelected, onSeriesSelected, onActorSelected) {
            object : MediaDetailOpener {
                override fun openMovie(id: Int) = onMovieSelected(id)

                override fun openSeries(id: Int) = onSeriesSelected(id)

                override fun openActor(id: Int) = onActorSelected(id)
            }
        }
    listPane(opener)
}
