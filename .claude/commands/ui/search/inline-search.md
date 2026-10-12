---
description: Wire the InlineSearchField into the TopAppBar for inline search
---

## Your task

Wire the existing `InlineSearchField` component into the TopAppBar so search appears inline next to the title.

### Existing file

The component already exists at:
`core/src/commonMain/kotlin/com/goldenboot/goldenbootfinancial/core/ui/search/InlineSearchField.kt`

Read that file first to understand its API. Do NOT recreate it — use it as-is.

### Wiring into TopAppBar

The TopAppBar needs these parameters added:

```kotlin
isSearchVisible: Boolean,
onSearchToggle: () -> Unit,
searchQuery: String,
onSearchQueryChange: (String) -> Unit,
onSearchSubmit: (String) -> Unit
```

In the title slot, render the field inline next to the title:

```kotlin
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
}
```

### Wiring in App.kt

Add state and pass to TopAppBar:

```kotlin
var isSearchVisible by remember { mutableStateOf(false) }
var searchQuery by remember { mutableStateOf("") }
val focusManager = LocalFocusManager.current
```

Hide the search icon when the field is active:

```kotlin
actions = {
    if (!isSearchVisible) { SearchIconButton(onClick = onSearchToggle) }
}
```

Wrap scaffold content in a focus-clearing Box so tapping outside dismisses:

```kotlin
Box(
    modifier = Modifier
        .padding(paddingValues)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { focusManager.clearFocus() }
) {
    // NavHost content
}
```
