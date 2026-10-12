# GoldenbootFinancial — Development Guidelines

## Architecture

MVVM + Clean Architecture per feature module. Shared code lives in `commonMain`; platform-specific code in `androidMain`, `iosMain`, `desktopMain`, `wasmJsMain`.

```
Screen (Composable) → ViewModel (StateFlow) → Service (business logic) → Repository → API / Room
```

### Modules

| Module | Path | Purpose |
|--------|------|---------|
| **composeApp** | `composeApp/` | App entry points, navigation host, single Scaffold |
| **core** | `core/` | HttpClient, NavigationManager, theme, NetworkBoundResource |
| **dashboard** | `features/dashboard/` | Dashboard feature tab |
| **cashflow** | `features/cashflow/` | CashFlow feature tab |
| **payroll** | `features/payroll/` | Payroll feature tab |
| **paycheckplan** | `features/paycheckplan/` | PaycheckPlan feature tab |
| **investments** | `features/investments/` | Investments feature tab |
| **profile** | `features/profile/` | Profile feature tab |

See each module's own `CLAUDE.md` for module-specific details:
- [core/CLAUDE.md](core/CLAUDE.md)
- [features/dashboard/CLAUDE.md](features/dashboard/CLAUDE.md)
- [features/cashflow/CLAUDE.md](features/cashflow/CLAUDE.md)
- [features/payroll/CLAUDE.md](features/payroll/CLAUDE.md)
- [features/paycheckplan/CLAUDE.md](features/paycheckplan/CLAUDE.md)
- [features/investments/CLAUDE.md](features/investments/CLAUDE.md)
- [features/profile/CLAUDE.md](features/profile/CLAUDE.md)

### Dependency Rules

- **Presentation → Domain → Data** (never reverse)
- Feature modules depend on `core` but never on each other
- Domain layer has zero framework imports (no Ktor, no Room, no Compose)
- Repository interfaces live in `domain/`; implementations in `data/`

### Why Core Is a Separate Module

Core exists to break a circular dependency. Gradle forbids circular module dependencies:

```
composeApp → features/*   (imports feature screens for NavHost + Koin modules)
features/* → core          (imports NetworkBoundResource, NetworkResult, HttpClient)
core       → nothing
```

If core's code moved into composeApp, every feature would need `implementation(projects.composeApp)`,
but composeApp already depends on every feature module — creating an impossible cycle.
Core is the shared foundation that both composeApp and feature modules can depend on without loops.

## Platform Entry Points

All UI, navigation, ViewModels, and DI live in `commonMain`. Each platform has a thin bootstrap:

| Platform | Entry | Notes |
|----------|-------|-------|
| **Android** | `MainApplication` + `MainActivity` | Koin init in `Application.onCreate()` |
| **iOS** | `MainViewController.kt` | Koin init before `ComposeUIViewController` |
| **Desktop** | `main()` in `desktopMain` | Dock icon via `Taskbar.setIconImage()` |
| **Web** | `main()` in `wasmJsMain` | `CanvasBasedWindow { App() }` |
| **Server** | Ktor `Application.module()` | REST API, not Compose UI |

## Compose Best Practices

- **Single Scaffold**: One `Scaffold` in `MainScreen` with shared `TopAppBar`. Tab screens must NOT have their own Scaffold.
- **Stateful + Stateless**: Every screen has a stateful wrapper (injects ViewModel) and a stateless `Content` composable (takes state + callbacks).
- **`remember`**: Cache expensive calculations — don't recompute on every recomposition.
- **`derivedStateOf`**: Use when derived values change less often than source state.
- **Lazy keys**: Always provide `key` in `items()` calls.
- **No backwards writes**: Never write state after reading it in the same composition.

## String Resources

All user-facing text uses `stringResource(Res.string.xxx)` — never hardcode strings.

- Defined in `composeApp/src/commonMain/composeResources/values/strings.xml`
- Content descriptions also use string resources (e.g., `Res.string.cd_menu`)
- For non-composable contexts, use `StringResource` type

## Theme

Custom theme at `core/.../theme/`:
- `Color.kt` — light/dark color schemes
- `Type.kt` — typography scale
- `Shape.kt` — corner radius tokens
- `Spacing.kt` — spacing scale (xs → xl)
- `Elevation.kt` — elevation levels
- `Theme.kt` — `GoldenbootFinancialTheme` composable wrapping `MaterialTheme`

Always use theme tokens (`MaterialTheme.colorScheme.primary`, `GoldenbootFinancialTheme.spacing.md`) instead of raw values.

## Build & Run

```bash
# Android
./gradlew :composeApp:assembleDebug
```

```bash
# iOS
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

```bash
# Desktop
./gradlew :composeApp:run --no-configuration-cache
```

```bash
# Web
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

```bash
# Server
./gradlew :server:run
```

## Documentation

- [Architecture Details](docs/Architecture.md)
- [Environment Setup](core/doc/EnvironmentSetup.md)
