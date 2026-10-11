package com.movieapp.core.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.movieapp.core.ui.designsystem.MovieAppMotion
import com.movieapp.core.ui.designsystem.motionDurationMillis

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalMovieAppSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

val LocalMovieAppAnimatedVisibilityScope =
    staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

data class CinematicSharedContentOrigin(val contentKey: Any, val sourceToken: Any)

val LocalMovieAppSharedContentOrigin =
    staticCompositionLocalOf<MutableState<CinematicSharedContentOrigin?>?> { null }

fun movieSharedContentKey(movieId: Int): String = "movie:$movieId"

fun seriesSharedContentKey(seriesId: Int): String = "series:$seriesId"

@Composable
fun rememberCinematicSharedContentClick(
    contentKey: Any,
    sourceToken: Any,
    onClick: () -> Unit,
): () -> Unit {
    val activeOrigin = LocalMovieAppSharedContentOrigin.current
    val currentOnClick = rememberUpdatedState(onClick)
    return {
        if (activeOrigin == null) {
            currentOnClick.value()
        } else {
            activeOrigin.value =
                CinematicSharedContentOrigin(
                    contentKey = contentKey,
                    sourceToken = sourceToken,
                )
            currentOnClick.value()
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.cinematicSharedBounds(contentKey: Any, sourceToken: Any? = null): Modifier {
    val sharedTransitionScope = LocalMovieAppSharedTransitionScope.current ?: return this
    val animatedVisibilityScope = LocalMovieAppAnimatedVisibilityScope.current ?: return this
    val activeOrigin = LocalMovieAppSharedContentOrigin.current?.value
    if (activeOrigin?.contentKey != contentKey) return this
    if (sourceToken != null && activeOrigin.sourceToken !== sourceToken) return this

    val sharedContentState =
        with(sharedTransitionScope) { rememberSharedContentState(key = contentKey) }
    val durationMillis = motionDurationMillis(MovieAppMotion.transitionMillis)
    return with(sharedTransitionScope) {
        this@cinematicSharedBounds.sharedBounds(
            sharedContentState = sharedContentState,
            animatedVisibilityScope = animatedVisibilityScope,
            boundsTransform = { _, _ -> tween(durationMillis = durationMillis) },
        )
    }
}
