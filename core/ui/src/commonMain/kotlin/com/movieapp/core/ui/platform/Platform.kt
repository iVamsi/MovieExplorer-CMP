package com.movieapp.core.ui.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

expect val supportsDynamicColor: Boolean

@Composable
expect fun dynamicColorScheme(darkTheme: Boolean): ColorScheme?

/** Returns a function that opens the system share sheet with plain text. */
@Composable
expect fun rememberShareText(): (String) -> Unit
