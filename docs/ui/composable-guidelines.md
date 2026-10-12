# Composable Guidelines

## Preview Required

Every `@Composable` function should have a corresponding `@Preview` composable. Previews are placed at the bottom of the file. Always use a separate preview function (never `@Preview` directly on the composable). Preview the **stateless** content composable with representative sample data for each UI state.

```kotlin
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    DashboardContent(uiState = uiState, onRefresh = viewModel::refresh, modifier = modifier)
}

@Composable
fun DashboardContent(uiState: DashboardUiState, onRefresh: () -> Unit, modifier: Modifier = Modifier) {
    // ...
}

@Preview
@Composable
private fun DashboardContentLoadingPreview() {
    DashboardContent(uiState = DashboardUiState.Loading, onRefresh = {})
}

@Preview
@Composable
private fun DashboardContentSuccessPreview() {
    DashboardContent(
        uiState = DashboardUiState.Success(items = listOf(/* sample data */)),
        onRefresh = {}
    )
}
```

## Preview Import

Use `org.jetbrains.compose.ui.tooling.preview.Preview` — provided by `compose.components.uiToolingPreview` dependency in commonMain.

## Theme & Colors

Use your app's theme composable (e.g., `{OrgName}Theme`, not `MaterialTheme` directly). Located in the core module's theme package.

- `MaterialTheme.colorScheme` for semantic colors
- `MaterialTheme.typography` for text styles
- `MaterialTheme.shapes` for shapes

### No Hardcoded Colors

Never use `Color(0xFF...)` in composables. Always use theme tokens or a centralized color object.

```kotlin
// Bad
Text(color = Color(0xFF1B2B4B))

// Good
Text(color = MaterialTheme.colorScheme.onSurface)
```

If a design spec color doesn't have an exact match in your theme tokens, use the **closest token** rather than hardcoding the hex value. This keeps the app consistent and supports future theme changes (e.g., dark mode).

## State Hoisting

Always hoist state out of composables. Composables should be **stateless** by default — they receive state and emit events via callbacks. This follows Compose's unidirectional data flow pattern.

- **Reference**: https://developer.android.com/develop/ui/compose/state#state-hoisting
- State flows **down** as parameters, events flow **up** as callbacks
- Only the top-level or screen-level composable should own state (via `remember` / `rememberSaveable`)
- This makes composables testable, reusable, and previewable with any state

```kotlin
// Bad — state buried inside, can't test or control from parent
@Composable
fun SearchBar(onQueryChange: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    TextField(value = query, onValueChange = { query = it; onQueryChange(it) })
}

// Good — stateless, parent controls state
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(value = query, onValueChange = onQueryChange)
}
```

**When to hoist:** Any composable that holds `mutableStateOf`, `rememberSaveable`, or other state that a parent might need to read, write, or reset.

**Exception:** Transient UI-only state (e.g., animation state, focus requesters) can stay local if no parent will ever need it.

## Modifier Convention

Every public composable must accept `modifier: Modifier = Modifier` as a parameter. Place it as the **first optional parameter** after required parameters.

```kotlin
// Required params first, then modifier, then other optional params
@Composable
fun DashboardScreen(
    title: String,                      // required
    modifier: Modifier = Modifier,      // first optional
    showSubtitle: Boolean = false,      // other optional
)
```

## Material 3 Component References

When implementing M3 components, refer to the official guidelines:

- **Navigation Drawer**: https://m3.material.io/components/navigation-drawer/overview
- **Bottom Navigation**: https://m3.material.io/components/navigation-bar/overview
- **SearchBar**: https://m3.material.io/components/search/overview
