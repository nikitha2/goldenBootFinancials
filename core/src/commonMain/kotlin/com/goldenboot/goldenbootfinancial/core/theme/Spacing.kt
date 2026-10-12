package com.goldenboot.goldenbootfinancial.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Spacing design tokens
data class GoldenbootSpacingValues(
    val none: Dp = 0.dp,
    val xs: Dp = 2.dp,
    val sm: Dp = 4.dp,
    val md: Dp = 8.dp,
    val lg: Dp = 12.dp,
    val xl: Dp = 16.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
    val xxxxl: Dp = 40.dp,
    val xxxxxl: Dp = 48.dp,
)

val LocalSpacing = staticCompositionLocalOf { GoldenbootSpacingValues() }

val GoldenbootSpacing: GoldenbootSpacingValues
    @Composable
    get() = LocalSpacing.current
