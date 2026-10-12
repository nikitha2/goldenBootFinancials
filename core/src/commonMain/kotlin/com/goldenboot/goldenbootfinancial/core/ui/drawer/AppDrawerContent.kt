package com.goldenboot.goldenbootfinancial.core.ui.drawer

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.goldenboot.goldenbootfinancial.core.theme.AppBarStyling
import goldenbootfinancial.core.generated.resources.Res
import goldenbootfinancial.core.generated.resources.cd_menu
import goldenbootfinancial.core.generated.resources.ic_header_menu
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * One drawer destination.
 *
 * @param label Full text shown when the drawer is expanded.
 * @param route Navigation route.
 * @param shortLabel Compact text shown under the icon when the drawer is collapsed.
 * @param icon Icon painter, shown in both expanded and collapsed states.
 */
data class DrawerItem(
    val label: String,
    val route: String,
    val shortLabel: String = label,
    val icon: (@Composable () -> Painter)? = null
)

private val ExpandedWidth = 280.dp
private val CollapsedWidth = 88.dp

/**
 * Drawer content for the navigation drawer.
 *
 * Stateless — receives items and delegates clicks to the caller.
 * A [permanent] drawer can be collapsed to an icon rail ([expanded] = false);
 * expanded shows larger text next to each icon, collapsed shows small labels under them.
 *
 * @param title Header text shown at the top of the drawer.
 * @param items Destinations, in display order.
 * @param currentRoute Currently active route (highlights the matching item).
 * @param onItemClick Called with the route when a drawer item is tapped.
 * @param logoutLabel Text of the Log out row pinned above the version.
 * @param onLogoutClick Called when Log out is tapped.
 * @param versionText Footer text such as "v1.0.0 · TEST".
 * @param permanent True renders an always-visible sheet for wide screens; false a modal sheet.
 * @param expanded Only used when [permanent]: true = full width, false = icon rail.
 * @param onToggleExpanded Shows a toggle button in the header when non-null.
 * @param logoutIcon Icon for the Log out row.
 */
@Composable
fun AppDrawerContent(
    title: String,
    items: List<DrawerItem>,
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    logoutLabel: String,
    onLogoutClick: () -> Unit,
    versionText: String,
    permanent: Boolean = false,
    expanded: Boolean = true,
    onToggleExpanded: (() -> Unit)? = null,
    logoutIcon: (@Composable () -> Painter)? = null
) {
    val collapsed = permanent && !expanded

    val sheetContent: @Composable ColumnScope.() -> Unit = {
        if (collapsed) {
            CollapsedDrawerBody(
                items, currentRoute, onItemClick, logoutLabel, onLogoutClick,
                versionText, onToggleExpanded, logoutIcon
            )
        } else {
            ExpandedDrawerBody(
                title, items, currentRoute, onItemClick, logoutLabel, onLogoutClick,
                versionText, onToggleExpanded, logoutIcon
            )
        }
    }

    if (permanent) {
        val width = animateDpAsState(if (collapsed) CollapsedWidth else ExpandedWidth)
        // Slightly offset from the page so the drawer's edge is visible in light and dark.
        val sheetColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
            .compositeOver(MaterialTheme.colorScheme.surface)
        PermanentDrawerSheet(
            modifier = Modifier.width(width.value),
            drawerContainerColor = sheetColor,
            content = sheetContent
        )
    } else {
        ModalDrawerSheet(drawerShape = AppBarStyling.drawerShape, content = sheetContent)
    }
}

@Composable
private fun ToggleButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(contentColor = AppBarStyling.drawerIconColor)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_header_menu),
            contentDescription = stringResource(Res.string.cd_menu)
        )
    }
}

@Composable
private fun ColumnScope.ExpandedDrawerBody(
    title: String,
    items: List<DrawerItem>,
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    logoutLabel: String,
    onLogoutClick: () -> Unit,
    versionText: String,
    onToggleExpanded: (() -> Unit)?,
    logoutIcon: (@Composable () -> Painter)?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        if (onToggleExpanded != null) ToggleButton(onToggleExpanded)
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }

    items.forEach { item ->
        val selected = currentRoute == item.route
        NavigationDrawerItem(
            icon = item.icon?.let { icon -> { Icon(icon(), contentDescription = null) } },
            label = {
                Text(
                    item.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                )
            },
            selected = selected,
            colors = AppBarStyling.drawerItemColors,
            onClick = { onItemClick(item.route) }
        )
    }

    Spacer(Modifier.weight(1f))
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    NavigationDrawerItem(
        icon = logoutIcon?.let { icon -> { Icon(icon(), contentDescription = null) } },
        label = { Text(logoutLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium) },
        selected = false,
        colors = AppBarStyling.drawerItemColors,
        onClick = onLogoutClick
    )
    Text(
        text = versionText,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun ColumnScope.CollapsedDrawerBody(
    items: List<DrawerItem>,
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    logoutLabel: String,
    onLogoutClick: () -> Unit,
    versionText: String,
    onToggleExpanded: (() -> Unit)?,
    logoutIcon: (@Composable () -> Painter)?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        if (onToggleExpanded != null) ToggleButton(onToggleExpanded)
        items.forEach { item ->
            NavigationRailItem(
                selected = currentRoute == item.route,
                onClick = { onItemClick(item.route) },
                icon = { item.icon?.let { icon -> Icon(icon(), contentDescription = item.label) } },
                label = { CollapsedLabel(item.shortLabel) },
                alwaysShowLabel = true,
                colors = railItemColors()
            )
        }
    }

    Spacer(Modifier.weight(1f))
    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
    NavigationRailItem(
        selected = false,
        onClick = onLogoutClick,
        icon = { logoutIcon?.let { icon -> Icon(icon(), contentDescription = logoutLabel) } },
        label = { CollapsedLabel(logoutLabel) },
        alwaysShowLabel = true,
        colors = railItemColors(),
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        // "v1.0.0 · TEST" -> two short lines so it fits the narrow rail.
        text = versionText.replace(" · ", "\n"),
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
    )
}

@Composable
private fun railItemColors() = NavigationRailItemDefaults.colors(
    selectedIconColor = AppBarStyling.drawerIconColor,
    unselectedIconColor = AppBarStyling.drawerIconColor,
    selectedTextColor = MaterialTheme.colorScheme.onSurface,
    unselectedTextColor = MaterialTheme.colorScheme.onSurface
)

@Composable
private fun CollapsedLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Preview
@Composable
private fun AppDrawerContentPreview() {
    MaterialTheme {
        AppDrawerContent(
            title = "Menu",
            items = listOf(
                DrawerItem("Home", "home"),
                DrawerItem("Search", "search"),
                DrawerItem("Profile", "profile")
            ),
            currentRoute = "home",
            onItemClick = {},
            logoutLabel = "Log out",
            onLogoutClick = {},
            versionText = "v1.0.0 · TEST"
        )
    }
}
