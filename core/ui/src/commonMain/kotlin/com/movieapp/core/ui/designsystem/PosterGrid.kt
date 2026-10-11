package com.movieapp.core.ui.designsystem

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.dp

/** 3 columns on phones, 5+ on tablets; matches the old grid_columns resource. */
val PosterGridCells: GridCells = GridCells.Adaptive(minSize = 112.dp)
