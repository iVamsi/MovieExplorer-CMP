package com.movieapp.core.ui.designsystem

import androidx.compose.runtime.Composable
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

@Composable
internal actual fun animatorDurationScale(): Float = if (UIAccessibilityIsReduceMotionEnabled()) 0f else 1f
