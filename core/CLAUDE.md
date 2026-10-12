# Core Module

Shared infrastructure used by all feature modules. No feature-specific code belongs here.

## What's in this module

### Networking (`network/`)
- **`NetworkResult<T>`** — sealed class: `Success(data)` or `Error(exception, cachedData?)`

- **`networkFetch`** — simple try/catch wrapper returning `NetworkResult`
- **`DeduplicatedRequests`** — Ktor plugin that cancels duplicate in-flight calls by URL + method

### Navigation (`navigation/`)
- **`NavigationManager`** — centralized nav state via `StateFlow<NavHostController?>`
- Use `navigate(route)`, `navigateBack()`, `navigateAndClearStack(route)`

### DI (`di/CoreModule.kt`)
- Provides singleton `HttpClient` (ContentNegotiation + HttpRequestRetry + DeduplicatedRequests)
- Provides singleton `NavigationManager`

### Theme (`theme/`)
- Color, Typography, Shape, Spacing, Elevation tokens
- `{App}Theme` composable wrapping `MaterialTheme`
- Always use theme tokens, never raw values

## Koin wiring

```kotlin
val coreModule = module {
    single { HttpClient { /* retry, dedup, json */ } }
    single { NavigationManager() }
}
```

Feature modules get `HttpClient` and `NavigationManager` via `get()` from Koin.

## Rules

- Do NOT add feature-specific code here
- Do NOT import from any `features/` module
- All feature modules depend on core, never the reverse
