# UI Concepts

Key Compose and UI architecture concepts used in this project.

## State Hoisting

Moving state **up** from a child composable to its parent. The child becomes stateless — it receives state as parameters and emits changes as callbacks.

- State flows **down** as parameters
- Events flow **up** as callbacks
- State lives in the **lowest common ancestor** that needs it

```kotlin
// Stateless — receives state, emits events
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(value = query, onValueChange = onQueryChange)
}

// Parent owns the state
@Composable
fun Header() {
    var query by remember { mutableStateOf("") }
    SearchBar(query = query, onQueryChange = { query = it })
}
```

**Benefits:** testable, reusable, previewable, parent can coordinate state across children.

**Exception:** Transient UI-only state (animations, focus requesters) can stay local if no parent needs it.

**Reference:** https://developer.android.com/develop/ui/compose/state#state-hoisting

## Unidirectional Data Flow (UDF)

The pattern that state hoisting enables. Data moves in one direction through the UI tree:

```
State (ViewModel/Parent) -> UI (Composable) -> Events (callbacks) -> State update -> UI re-renders
```

This makes the UI predictable — there's a single source of truth for each piece of state.

**Reference:** https://developer.android.com/develop/ui/compose/architecture#udf

## Composition over Inheritance

Build UI by combining small, focused composables rather than inheriting from base classes. Each composable does one thing well.

```kotlin
// Small, focused composables
@Composable fun TabIcon(...) { /* icon with selected/unselected tint */ }
@Composable fun TabLabel(...) { /* text with selected/unselected style */ }

// Composed together
NavigationBarItem(
    icon = { TabIcon(...) },
    label = { TabLabel(...) },
)
```

## Slot APIs

Composables that accept other composables as parameters (lambda content), letting the caller decide what to render in specific "slots."

```kotlin
@Composable
fun AppScaffold(
    topBar: @Composable () -> Unit,     // slot
    bottomBar: @Composable () -> Unit,  // slot
    content: @Composable () -> Unit,    // slot
)
```

**Reference:** https://developer.android.com/develop/ui/compose/layouts/basics#slot-based-layouts

## Modifier Chain

Modifiers are applied in order — each one wraps the previous. Order matters.

```kotlin
// padding is OUTSIDE the background (padding first, then bg fills remaining space)
Modifier.padding(16.dp).background(Color.Red)

// padding is INSIDE the background (bg fills full area, then padding insets content)
Modifier.background(Color.Red).padding(16.dp)
```

Every public composable should accept `modifier: Modifier = Modifier` as a parameter for caller flexibility.

**Reference:** https://developer.android.com/develop/ui/compose/modifiers

## Recomposition

When state changes, Compose re-executes only the composables that read that state. This is why:

- State should be read as **late** as possible (close to where it's used)
- Lambdas (e.g., `onClick = { }`) help defer reads and reduce recomposition scope
- `remember` caches values across recompositions
- `derivedStateOf` avoids unnecessary recompositions for computed values

**Reference:** https://developer.android.com/develop/ui/compose/mental-model#recomposition

## Stable & Immutable Types

Compose skips recomposition of a composable if **all its parameters are unchanged**. But it can only do this if it knows the types are **stable** — meaning Compose can reliably compare old vs. new values.

### What's stable by default?
- Primitives (`Int`, `String`, `Boolean`, `Float`)
- `enum` classes
- Function types (lambdas)
- `@Immutable` or `@Stable` annotated classes

### What's NOT stable?
- Standard `List`, `Map`, `Set` (mutable interfaces — Compose can't guarantee contents haven't changed)
- Classes with `var` properties
- Classes from external libraries Compose hasn't analyzed

### When to act
- **Don't prematurely optimize** — only annotate types when profiling shows unnecessary recomposition
- Use `@Immutable` when a data class is truly immutable (all `val`, no mutable collections)
- Use `@Stable` when a class has mutable state but notifies Compose of changes
- Prefer `kotlinx.collections.immutable` (`ImmutableList`, `ImmutableMap`) over standard collections for composable parameters

**Reference:** https://developer.android.com/develop/ui/compose/performance/stability
