package com.movieapp.core.ui.designsystem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

object MovieAppMotion {
    const val pressMillis = 100
    const val transitionMillis = 250
}

/** Returns zero when animations are disabled; Compose applies every nonzero system scale. */
internal fun scaledMotionDurationMillis(
    durationMillis: Int,
    animatorDurationScale: Float,
): Int =
    if (animatorDurationScale <= 0f) {
        0
    } else {
        durationMillis
    }

/** Reads the platform animation duration scale when the UI composes. */
@Composable
fun motionDurationMillis(durationMillis: Int = MovieAppMotion.transitionMillis): Int {
    return scaledMotionDurationMillis(
        durationMillis = durationMillis,
        animatorDurationScale = animatorDurationScale(),
    )
}

/** System animator scale on Android; 0 when iOS Reduce Motion is on, otherwise 1. */
@Composable
internal expect fun animatorDurationScale(): Float

@Composable
fun Modifier.cinematicPressFeedback(interactionSource: MutableInteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by
        animateFloatAsState(
            targetValue = if (pressed) 0.98f else 1f,
            animationSpec = tween(motionDurationMillis(MovieAppMotion.pressMillis)),
            label = "poster press",
        )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
