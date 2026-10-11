package com.movieapp.core.ui.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Hero/backdrop scrims for media imagery.
 *
 * - [heroDetailBackdropScrimBrush]: Movie & TV detail backdrop behind the glass info card.
 * - [heroScrimBrush]: Theme-colored bottom-weighted fade (e.g. actor hero behind glass card).
 */
private const val DetailHeroGradientStartY = 280f

/** Pair these values for text placed directly over artwork in either app theme. */
object MovieAppHeroColors {
    val surface = Color(0xFF101114)
    val foreground = Color(0xFFF5F1E8)
    val onSurface = foreground
    val accent = Color(0xFFF2B866)
}

fun cinematicHeroScrimBrush(): Brush =
    Brush.verticalGradient(
        colors = listOf(MovieAppHeroColors.surface.copy(alpha = 0.24f), MovieAppHeroColors.surface)
    )

/** Bottom fade over backdrop image on movie/TV detail heroes (glass card region). */
@Composable
fun heroDetailBackdropScrimBrush(
    startY: Float = DetailHeroGradientStartY,
    bottomAlpha: Float = 0.92f,
    accentColor: Color? = null,
): Brush {
    val bottom = MaterialTheme.colorScheme.surface.copy(alpha = bottomAlpha.coerceIn(0.92f, 1f))
    val colors =
        if (accentColor == null) {
            listOf(Color.Transparent, bottom)
        } else {
            listOf(Color.Transparent, accentColor.copy(alpha = 0.28f), bottom)
        }
    return Brush.verticalGradient(colors = colors, startY = startY)
}

@Composable
fun heroScrimBrush(scrimBottomAlpha: Float = 0.72f): Brush {
    val surface = MaterialTheme.colorScheme.surface
    val bottom = surface.copy(alpha = scrimBottomAlpha.coerceIn(0.92f, 1f))
    return Brush.verticalGradient(colors = listOf(Color.Transparent, bottom), startY = 120f)
}
