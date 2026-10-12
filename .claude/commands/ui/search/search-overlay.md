---
description: Generate a full-screen Material 3 SearchOverlay as an alternative to inline search
---

## Your task

Generate a full-screen SearchOverlay component using Material 3 SearchBar.

This project currently uses an **inline search field** in the TopAppBar. This command generates a **full-screen overlay** alternative that shows a search input with suggestion results.

### What to generate

Create the file at:
`core/src/commonMain/kotlin/com/goldenboot/goldenbootfinancial/core/ui/search/SearchOverlay.kt`

```kotlin
package com.goldenboot.goldenbootfinancial.core.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import ${RES_PACKAGE}.Res
import ${RES_PACKAGE}.ic_header_search
import ${RES_PACKAGE}.cd_search
import ${RES_PACKAGE}.search_placeholder

/**
 * Full-screen search overlay using Material 3 SearchBar.
 *
 * Alternative to the inline search field. Shows a modal overlay
 * with search input, suggestions list, and result handling.
 *
 * @param onSearch Called when the user submits a search query.
 * @param onDismiss Called when the user closes the overlay.
 * @param searchResults List of suggestion strings to display.
 * @param onResultClick Called when a suggestion is tapped.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchOverlay(
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit,
    searchResults: List<String> = emptyList(),
    onResultClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(true) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = { searchQuery ->
                        onSearch(searchQuery)
                        expanded = false
                    },
                    expanded = expanded,
                    onExpandedChange = { isExpanded ->
                        expanded = isExpanded
                        if (!isExpanded) onDismiss()
                    },
                    placeholder = { Text(stringResource(Res.string.search_placeholder)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_header_search),
                            contentDescription = stringResource(Res.string.cd_search)
                        )
                    }
                )
            },
            expanded = expanded,
            onExpandedChange = { isExpanded ->
                expanded = isExpanded
                if (!isExpanded) onDismiss()
            }
        ) {
            if (searchResults.isNotEmpty()) {
                LazyColumn {
                    items(items = searchResults, key = { it }) { result ->
                        ListItem(
                            headlineContent = { Text(result) },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            modifier = Modifier
                                .clickable {
                                    query = result
                                    onResultClick(result)
                                    expanded = false
                                }
                                .fillMaxWidth()
                        )
                    }
                }
            } else if (query.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.search_no_results),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SearchOverlayPreview() {
    MaterialTheme {
        SearchOverlay(
            onSearch = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun SearchOverlayWithResultsPreview() {
    MaterialTheme {
        SearchOverlay(
            onSearch = {},
            onDismiss = {},
            searchResults = listOf("Result 1", "Result 2", "Result 3")
        )
    }
}
```

### Important

- Replace `${RES_PACKAGE}` with the actual resource package: `{projectName_lowercase}.core.generated.resources` (check the existing `InlineSearchField.kt` in the same directory for the correct import)
- The `search_no_results` string resource may need to be added to `core/src/commonMain/composeResources/values/strings.xml`
- This component is **additive** — it does not replace the inline search. Both can coexist.

### Wiring into App.kt (if replacing inline search)

If the user wants to use SearchOverlay instead of inline search, update App.kt:

1. Add state: `var isSearchActive by remember { mutableStateOf(false) }`
2. Show overlay in scaffold body:
```kotlin
if (isSearchActive) {
    SearchOverlay(
        onSearch = { query -> /* handle */ isSearchActive = false },
        onDismiss = { isSearchActive = false }
    )
}
```
3. Change TopAppBar search toggle to set `isSearchActive = true`
