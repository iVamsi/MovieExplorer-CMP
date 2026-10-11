@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.movieapp.core.ui.theme

import com.movieapp.core.ui.resources.Res
import com.movieapp.core.ui.resources.roboto_condensed_semibold
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import org.jetbrains.compose.resources.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val CinematicDarkColors =
    darkColorScheme(
        primary = Color(0xFFF2B866),
        onPrimary = Color(0xFF352000),
        primaryContainer = Color(0xFF533500),
        onPrimaryContainer = Color(0xFFFFDDAA),
        secondary = Color(0xFFD9C5A7),
        onSecondary = Color(0xFF30281B),
        secondaryContainer = Color(0xFF494032),
        onSecondaryContainer = Color(0xFFF6E1C2),
        tertiary = Color(0xFFB8CDBD),
        onTertiary = Color(0xFF24352A),
        tertiaryContainer = Color(0xFF3A4C40),
        onTertiaryContainer = Color(0xFFD4E9D8),
        background = Color(0xFF101114),
        onBackground = Color(0xFFF5F1E8),
        surface = Color(0xFF101114),
        onSurface = Color(0xFFF5F1E8),
        surfaceVariant = Color(0xFF34363D),
        onSurfaceVariant = Color(0xFFC8C5BF),
        surfaceTint = Color(0xFFF2B866),
        inverseSurface = Color(0xFFF5F1E8),
        inverseOnSurface = Color(0xFF191B20),
        inversePrimary = Color(0xFF805000),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        outline = Color(0xFF96938D),
        outlineVariant = Color(0xFF494950),
        scrim = Color.Black,
        surfaceBright = Color(0xFF37393F),
        surfaceDim = Color(0xFF101114),
        surfaceContainerLowest = Color(0xFF0B0C0F),
        surfaceContainerLow = Color(0xFF17191E),
        surfaceContainer = Color(0xFF1B1D22),
        surfaceContainerHigh = Color(0xFF25272D),
        surfaceContainerHighest = Color(0xFF303239),
        primaryFixed = Color(0xFFFFDDAA),
        primaryFixedDim = Color(0xFFF2B866),
        onPrimaryFixed = Color(0xFF291800),
        onPrimaryFixedVariant = Color(0xFF614000),
        secondaryFixed = Color(0xFFF6E1C2),
        secondaryFixedDim = Color(0xFFD9C5A7),
        onSecondaryFixed = Color(0xFF211A10),
        onSecondaryFixedVariant = Color(0xFF494032),
        tertiaryFixed = Color(0xFFD4E9D8),
        tertiaryFixedDim = Color(0xFFB8CDBD),
        onTertiaryFixed = Color(0xFF102117),
        onTertiaryFixedVariant = Color(0xFF3A4C40),
    )

val CinematicLightColors =
    lightColorScheme(
        primary = Color(0xFF805000),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFDDAA),
        onPrimaryContainer = Color(0xFF352000),
        secondary = Color(0xFF625541),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFF0E1CA),
        onSecondaryContainer = Color(0xFF30281B),
        tertiary = Color(0xFF4C6353),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFD4E9D8),
        onTertiaryContainer = Color(0xFF24352A),
        background = Color(0xFFF7F4EE),
        onBackground = Color(0xFF191B20),
        surface = Color.White,
        onSurface = Color(0xFF191B20),
        surfaceVariant = Color(0xFFE8E3DA),
        onSurfaceVariant = Color(0xFF4E4B45),
        surfaceTint = Color(0xFF805000),
        inverseSurface = Color(0xFF303239),
        inverseOnSurface = Color(0xFFF5F1E8),
        inversePrimary = Color(0xFFF2B866),
        error = Color(0xFFBA1A1A),
        onError = Color.White,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        outline = Color(0xFF7D786F),
        outlineVariant = Color(0xFFCCC5BA),
        scrim = Color.Black,
        surfaceBright = Color.White,
        surfaceDim = Color(0xFFDFDBD4),
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Color(0xFFFAF7F1),
        surfaceContainer = Color.White,
        surfaceContainerHigh = Color(0xFFEDE9E2),
        surfaceContainerHighest = Color(0xFFE6E2DB),
        primaryFixed = Color(0xFFFFDDAA),
        primaryFixedDim = Color(0xFFF2B866),
        onPrimaryFixed = Color(0xFF291800),
        onPrimaryFixedVariant = Color(0xFF614000),
        secondaryFixed = Color(0xFFF6E1C2),
        secondaryFixedDim = Color(0xFFD9C5A7),
        onSecondaryFixed = Color(0xFF211A10),
        onSecondaryFixedVariant = Color(0xFF494032),
        tertiaryFixed = Color(0xFFD4E9D8),
        tertiaryFixedDim = Color(0xFFB8CDBD),
        onTertiaryFixed = Color(0xFF102117),
        onTertiaryFixedVariant = Color(0xFF3A4C40),
    )

@Composable
private fun cinematicTypography(): Typography {
    val displayFamily = FontFamily(Font(Res.font.roboto_condensed_semibold, FontWeight.SemiBold))
    val display =
    TextStyle(
        fontFamily = displayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 40.sp,
    )
    val heading = display.copy(fontSize = 28.sp, lineHeight = 34.sp)
    val title = display.copy(fontSize = 20.sp, lineHeight = 26.sp)
    val body =
    TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 24.sp)
    val label = body.copy(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp)

    return Typography(
        displayLarge = display,
        displayMedium = display,
        displaySmall = display,
        headlineLarge = heading,
        headlineMedium = heading,
        headlineSmall = heading,
        titleLarge = title,
        titleMedium = title,
        titleSmall = title,
        bodyLarge = body,
        bodyMedium = body,
        bodySmall = label,
        labelLarge = label,
        labelMedium = label,
        labelSmall = label,
        displayLargeEmphasized = display,
        displayMediumEmphasized = display,
        displaySmallEmphasized = display,
        headlineLargeEmphasized = heading,
        headlineMediumEmphasized = heading,
        headlineSmallEmphasized = heading,
        titleLargeEmphasized = title,
        titleMediumEmphasized = title,
        titleSmallEmphasized = title,
        bodyLargeEmphasized = body.copy(fontWeight = FontWeight.Medium),
        bodyMediumEmphasized = body.copy(fontWeight = FontWeight.Medium),
        bodySmallEmphasized = label,
        labelLargeEmphasized = label,
        labelMediumEmphasized = label,
        labelSmallEmphasized = label,
    )
}

val CinematicShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(20.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )

@Composable
fun CinematicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorScheme: ColorScheme = if (darkTheme) CinematicDarkColors else CinematicLightColors,
    content: @Composable () -> Unit,
) {
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = cinematicTypography(),
        shapes = CinematicShapes,
        motionScheme = MotionScheme.standard(),
        content = content,
    )
}
