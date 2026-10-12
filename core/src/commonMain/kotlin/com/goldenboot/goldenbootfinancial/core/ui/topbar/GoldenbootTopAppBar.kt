package com.goldenboot.goldenbootfinancial.core.ui.topbar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.goldenboot.goldenbootfinancial.core.theme.AppBarStyling
import goldenbootfinancial.core.generated.resources.Res
import goldenbootfinancial.core.generated.resources.ic_header_menu
import goldenbootfinancial.core.generated.resources.cd_menu
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import goldenbootfinancial.core.generated.resources.ic_header_search
import goldenbootfinancial.core.generated.resources.cd_search
import com.goldenboot.goldenbootfinancial.core.ui.search.InlineSearchField
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Top app bar with configurable navigation icon, title, and actions.
 *
 * When search is enabled, the inline search field appears next to the title text.
 * Each element is its own composable for readability and reuse.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoldenbootTopAppBar(
    title: String,
    onMenuClick: () -> Unit,
    isSearchVisible: Boolean,
    onSearchToggle: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    showMenuIcon: Boolean = true
) {
    TopAppBar(
        modifier = AppBarStyling.topBarModifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppBarStyling.containerColor,
            scrolledContainerColor = AppBarStyling.containerColor
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(title, fontWeight = FontWeight.Bold)
                if (isSearchVisible) {
                    Spacer(modifier = Modifier.width(8.dp))
                    InlineSearchField(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                        onDismiss = {
                            onSearchSubmit(searchQuery)
                            onSearchToggle()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        navigationIcon = { if (showMenuIcon) MenuIconButton(onClick = onMenuClick) },
        actions = {
            if (!isSearchVisible) { SearchIconButton(onClick = onSearchToggle) }
            Spacer(modifier = Modifier.width(8.dp))
        },
    )
}

@Composable
private fun MenuIconButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(Res.drawable.ic_header_menu),
            contentDescription = stringResource(Res.string.cd_menu)
        )
    }
}

@Composable
private fun SearchIconButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(Res.drawable.ic_header_search),
            contentDescription = stringResource(Res.string.cd_search)
        )
    }
}

@Preview
@Composable
private fun GoldenbootTopAppBarPreview() {
    MaterialTheme {
        GoldenbootTopAppBar(
            title = "Home",
            onMenuClick = {},
            isSearchVisible = false,
            onSearchToggle = {},
            searchQuery = "",
            onSearchQueryChange = {},
            onSearchSubmit = {}
        )
    }
}

@Preview
@Composable
private fun GoldenbootTopAppBarSearchActivePreview() {
    MaterialTheme {
        GoldenbootTopAppBar(
            title = "Home",
            onMenuClick = {},
            isSearchVisible = true,
            onSearchToggle = {},
            searchQuery = "search text",
            onSearchQueryChange = {},
            onSearchSubmit = {}
        )
    }
}
