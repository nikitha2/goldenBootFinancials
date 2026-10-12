package com.goldenboot.goldenbootfinancial.core.ui.bottombar

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import com.goldenboot.goldenbootfinancial.core.theme.AppBarStyling
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Data class representing a bottom navigation item.
 *
 * Defined in core so the BottomNavigationBar has no dependency on app-level routes.
 */
data class NavBarItem(
    val route: String,
    val titleRes: StringResource,
    val iconPainter: @Composable () -> Painter
)

/**
 * Bottom navigation bar with stable parameters for optimal recomposition.
 *
 * Receives hoisted state and a navigation callback instead of NavHostController
 * to ensure the composable is skippable by the Compose compiler.
 *
 * @param items Navigation items to display.
 * @param currentRoute Currently active route (highlights the matching item).
 * @param onNavigate Called with the route when a tab is tapped.
 */
@Composable
fun BottomNavigationBar(
    items: List<NavBarItem>,
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        modifier = AppBarStyling.bottomBarModifier,
        containerColor = AppBarStyling.containerColor,
        tonalElevation = AppBarStyling.tonalElevation
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = item.iconPainter(),
                        contentDescription = stringResource(item.titleRes)
                    )
                },
                label = { Text(stringResource(item.titleRes), maxLines = 1, softWrap = false) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        onNavigate(item.route)
                    }
                }
            )
        }
    }
}

@Preview
@Composable
private fun BottomNavigationBarPreview() {
    MaterialTheme {
        NavigationBar {
            listOf("Home", "Search", "Profile").forEachIndexed { index, label ->
                NavigationBarItem(
                    icon = { Icon(painter = ColorPainter(Color.Gray), contentDescription = label) },
                    label = { Text(label) },
                    selected = index == 0,
                    onClick = {}
                )
            }
        }
    }
}
