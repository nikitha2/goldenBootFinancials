package com.goldenboot.goldenbootfinancial.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp

object AppBarStyling {
    val shadowElevation: Dp
        @Composable
        get() = GoldenbootSpacing.sm

    val tonalElevation: Dp
        @Composable
        get() = GoldenbootSpacing.none

    val containerColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.background

    val topBarShape: Shape
        @Composable
        get() = RoundedCornerShape(bottomStart = GoldenbootSpacing.sm, bottomEnd = GoldenbootSpacing.sm)

    val bottomBarShape: Shape
        @Composable
        get() = RoundedCornerShape(topStart = GoldenbootSpacing.sm, topEnd = GoldenbootSpacing.sm)

    val drawerShape: Shape
        @Composable
        get() = RoundedCornerShape(topEnd = GoldenbootSpacing.sm, bottomEnd = GoldenbootSpacing.sm)

    val topBarModifier: Modifier
        @Composable
        get() = Modifier.shadow(shadowElevation, topBarShape).clip(topBarShape)

    val bottomBarModifier: Modifier
        @Composable
        get() = Modifier.shadow(shadowElevation, bottomBarShape).clip(bottomBarShape)

    /** Drawer icon tint: white in dark mode, the normal text colour in light mode. */
    val drawerIconColor: Color
        @Composable
        get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color.White
        else MaterialTheme.colorScheme.onSurface

    val drawerItemColors: NavigationDrawerItemColors
        @Composable
        get() = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unselectedContainerColor = Color.Transparent,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
            selectedIconColor = drawerIconColor,
            unselectedIconColor = drawerIconColor
        )
}
