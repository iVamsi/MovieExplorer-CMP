package com.movieapp.core.ui.components.shimmer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.movieapp.core.ui.designsystem.motionDurationMillis

/**
 * A horizontal left-to-right shimmer brush derived from the current MaterialTheme.
 *
 * The highlight is produced by brightening each RGB channel of [surfaceVariant] by 0.2, which
 * guarantees visible contrast in both light and dark themes without extra color tokens.
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val base = MaterialTheme.colorScheme.surfaceVariant
    if (motionDurationMillis() == 0) {
        return Brush.linearGradient(listOf(base, base))
    }
    val highlight =
        Color(
            red = (base.red + 0.2f).coerceAtMost(1f),
            green = (base.green + 0.2f).coerceAtMost(1f),
            blue = (base.blue + 0.2f).coerceAtMost(1f),
            alpha = base.alpha,
        )
    val shimmerColors = listOf(base, highlight, base)

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by
        transition.animateFloat(
            initialValue = -600f,
            targetValue = 1800f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "shimmer_translate",
        )
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnim - 300f, y = 0f),
        end = Offset(x = translateAnim + 300f, y = 0f),
    )
}

@Preview(showBackground = true, name = "Shimmer – Light")
@Composable
private fun ShimmerBrushPreview() {
    MaterialTheme {
        Box(
            modifier =
                Modifier.size(width = 200.dp, height = 120.dp).background(rememberShimmerBrush())
        )
    }
}
