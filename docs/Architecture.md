# Clean Architecture for Core Module & Dashboard & CashFlow & Payroll & PaycheckPlan & Investments & Profile Features

> Kotlin Multiplatform with Compose Multiplatform
> MVVM Pattern with Clean Architecture
> Feature-based modular structure

**Detailed Flow:**
```
Screen (Composable UI)
    ↓ observes
ViewModel (State + Events)
    ↓ calls
Service (Business Logic)
    ↓ calls
Repository (Data Access)
    ↓ uses
API Client (Network/Local Data)
    ↓
Backend API / Local Database
```

**Key Benefits:**
- ✅ **Testability** - Service layer is pure functions, easy to unit test
- ✅ **Reusability** - Same ViewModel can be used across platforms
- ✅ **Separation of Concerns** - Each layer has ONE job
- ✅ **Maintainability** - Business logic changes happen in ONE place (Service)
- ✅ **Type Safety** - Kotlin's type system prevents errors at compile time
- ✅ **Multiplatform** - commonMain code runs on Android, iOS, and Web
- ✅ **Reactive UI** - Compose automatically updates when ViewModel state changes

---

## Platform Entry Points

All UI, navigation, ViewModels, DI, and theme code lives in `commonMain` and is shared across every platform.
Each platform only needs a thin bootstrap (~5 lines) that calls the shared `App()` composable:

| Platform | Entry Point | Bootstrapping Code |
|----------|-------------|-------------------|
| **Android** | `MainActivity` + `MainApplication` | `setContent { App() }` + Koin init in `Application.onCreate()` |
| **iOS** | `MainViewController.kt` | `ComposeUIViewController { App() }` + Koin init |
| **Desktop** | `main()` in `desktopMain` | `application { Window { App() } }` |
| **Web (WASM)** | `main()` in `wasmJsMain` | `CanvasBasedWindow { App() }` |
| **Server** | Ktor `Application.module()` | Separate — REST API server, not Compose UI |

> **Key principle:** Platform-specific code handles only lifecycle bootstrapping and Koin initialization.
> All business logic, UI, and navigation remain in `commonMain`.

---

## Table of Contents
1. [Layer Diagram](#layer-diagram-top-to-bottom)
2. [Data Flow](#data-flow-request--response)
3. [Dependency Rules](#dependency-rules-critical)
4. [Folder Structure](#recommended-folder-structure)
5. [Implementation Examples](#implementation-examples)
6. [Before & After Comparison](#before--after-comparison)
7. [Testing Strategy](#testing-strategy)

---

## Layer Diagram (Top to Bottom)

```
┌────────────────────────────────────────────────────────────────┐
│                          UI LAYER                              │
│                    (What users see)                            │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  ┌──────────────┐  ┌──────────────┐                            │
│  │   Screens    │  │  Components  │                            │
│  │ (Composables)│  │ (Composables)│                            │
│  │ Dashboard    │  │  DashboardCard    │                            │
│  │ CashFlow     │  │  CashFlowCard    │                            │
│  │ Payroll      │  │  PayrollCard    │                            │
│  │ PaycheckPlan │  │  PaycheckPlanCard    │                            │
│  │ Investments  │  │  InvestmentsCard    │                            │
│  │ Profile      │  │  ProfileCard    │                            │
│  │              │  │  Button      │                            │
│  └──────┬───────┘  └──────────────┘                            │
│         │                                                      │
│         │  ✅ Screens ONLY observe ViewModel state             │
│         │                                                      │
└─────────┼──────────────────────────────────────────────────────┘
          │
          ▼
┌────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                          │
│                    (ViewModel - State Management)              │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  ┌──────────────────┐  ┌──────────────────┐                    │
│  │ DashboardViewModel  │  │  ...              │                    │
│  │ CashFlowViewModel  │  │  ...              │                    │
│  │ PayrollViewModel  │  │  ...              │                    │
│  │ PaycheckPlanViewModel  │  │  ...              │                    │
│  │ InvestmentsViewModel  │  │  ...              │                    │
│  │ ProfileViewModel  │  │  ...              │                    │
│  │                  │  │                  │                    │
│  │ - UI State       │  │ - UI State       │                    │
│  │ - Events         │  │ - Events         │                    │
│  │ - Calls Service  │  │ - Calls Service  │                    │
│  └────────┬─────────┘  └────────┬─────────┘                    │
│           │                     │                              │
│           │  ✅ ViewModels import and call Services            │
│           │                     │                              │
└───────────┼─────────────────────┼──────────────────────────────┘
            │                     │
            └──────────┬──────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                    BUSINESS LOGIC LAYER                         │
│                    (How the app works)                          │
│  (Purpose: Pure business logic - transforms data,               │
│                                  applies rules,                 │
│                                  orchestrates repositories)     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────┐  ┌──────────────────┐                     │
│  │ dashboardService      │  │  ...              │                     │
│  │ cashflowService      │  │  ...              │                     │
│  │ payrollService      │  │  ...              │                     │
│  │ paycheckplanService      │  │  ...              │                     │
│  │ investmentsService      │  │  ...              │                     │
│  │ profileService      │  │  ...              │                     │
│  │                  │  │                  │                     │
│  │ - Validation     │  │ - Calculations   │                     │
│  │ - Transformation │  │ - Orchestration  │                     │
│  │ - Calls Repo     │  │ - Calls Repo     │                     │
│  └────────┬─────────┘  └────────┬─────────┘                     │
│           │                     │                               │
│           │  ✅ Services import and call Repositories           │
│           │                     │                               │
└───────────┼─────────────────────┼───────────────────────────────┘
            │                     │
            └──────────┬──────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                    DATA LAYER                                   │
│                    (Where data comes from)                      │
│  (Purpose: Pure data access - NO business logic)                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────┐  ┌──────────────────┐                     │
│  │ dashboardRepository   │  │  ...              │                     │
│  │ cashflowRepository   │  │  ...              │                     │
│  │ payrollRepository   │  │  ...              │                     │
│  │ paycheckplanRepository   │  │  ...              │                     │
│  │ investmentsRepository   │  │  ...              │                     │
│  │ profileRepository   │  │  ...              │                     │
│  │                  │  │                  │                     │
│  │ - API calls      │  │ - Cache access   │                     │
│  │ - Error handling │  │ - Data mapping   │                     │
│  └────────┬─────────┘  └────────┬─────────┘                     │
│           │                     │                               │
│           │  ✅ Repositories call API/Database                   │
│           │                     │                               │
└───────────┼─────────────────────┼───────────────────────────────┘
            │                     │
            └──────────┬──────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                    EXTERNAL LAYER                               │
│                    (External data sources)                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           │
│  │  REST API    │  │  GraphQL     │  │  Database    │           │
│  │              │  │              │  │              │           │
│  │  Ktor Client │  │  Apollo      │  │  Room        │           │
│  └──────────────┘  └──────────────┘  └──────────────┘           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Data Flow (Request → Response)

### 1. User Interaction
```kotlin
// User taps a button in the UI
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.state.collectAsState()

    Button(onClick = { viewModel.onLoadData() }) {
        Text("Load Data")
    }
}
```

### 2. ViewModel Receives Event
```kotlin
class DashboardViewModel(
    private val service: DashboardService
) : ViewModel() {
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                val items = service.getAll()
                _uiState.value = if (items.isEmpty()) {
                    DashboardUiState.Empty
                } else {
                    DashboardUiState.Success(items)
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() { loadData() }
}

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data object Empty : DashboardUiState
    data class Success(val items: List<DashboardItem>) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}
```

### 3. Service Executes Business Logic
```kotlin
class DashboardService(
    private val dashboardRepository: DashboardRepository
) {
    suspend fun fetchData(): List<Item> {
        // Business logic: fetch, validate, transform
        val rawData = dashboardRepository.getData()
        return rawData.filter { it.isValid() }
                      .sortedBy { it.priority }
    }
}
```

### 4. Repository Fetches Data
```kotlin
class DashboardRepository(
    private val apiClient: ApiClient
) {
    suspend fun getData(): List<ItemDto> {
        return apiClient.get("/api/items")
    }
}
```

### 5. Response Flows Back
```
Repository returns DTO → Service transforms to domain model →
ViewModel updates state → Compose UI automatically re-renders
```

---

## Dependency Rules (CRITICAL)

### ✅ ALLOWED Dependencies (Inner → Outer)
```
UI Layer → ViewModel → Service → Repository → API Client
```

Each layer can only depend on the layer directly below it:
- ✅ Screen can import ViewModel
- ✅ ViewModel can import Service
- ✅ Service can import Repository
- ✅ Repository can import API Client

### ❌ FORBIDDEN Dependencies (Outer → Inner or Skip Levels)
- ❌ Screen cannot import Service directly (must go through ViewModel)
- ❌ Screen cannot import Repository
- ❌ ViewModel cannot import Repository directly (must go through Service)
- ❌ Service cannot import ViewModel
- ❌ Repository cannot import Service
- ❌ Repository cannot import ViewModel

### 🎯 The Rule: "Dependencies point INWARD only"
```
         ┌─────────┐
         │  Screen │ ──┐
         └─────────┘   │
                       ▼
       ┌───────────────┐
       │  ViewModel    │ ──┐
       └───────────────┘   │
                           ▼
         ┌──────────────┐
         │   Service    │ ──┐
         └──────────────┘   │
                            ▼
        ┌────────────────┐
        │   Repository   │ ──┐
        └────────────────┘   │
                             ▼
           ┌──────────────┐
           │  API Client  │
           └──────────────┘

All arrows point DOWN. Never reverse them!
```

### Module Dependency Graph

```
composeApp ──→ core
composeApp ──→ features/home, features/stage, ...
features/* ──→ core
core       ──→ (external libraries only)
```

**Why core is a separate module:** Gradle forbids circular dependencies.
`composeApp` imports feature screens for NavHost and Koin modules, so it depends on `features/*`.
Feature modules import `NetworkBoundResource`, `NetworkResult`, and `HttpClient` from core.
If that shared infrastructure lived in `composeApp`, every feature would depend on `composeApp`,
creating a cycle (`composeApp ↔ features`) that Gradle cannot resolve.
Core breaks this cycle — it is the shared foundation both layers can depend on.

---

## What Goes Where

| Module | Purpose | Contains | Depends on |
|--------|---------|----------|------------|
| **composeApp** | App shell — the thing users see and navigate | `App.kt` (single Scaffold), navigation (NavRoutes, AppNavHost), splash screen, platform entry points, Koin initializer, all icons + strings | core, all features |
| **core** | Shared infrastructure — things features need at compile time | Networking (HttpClient, NetworkBoundResource, NetworkResult, DeduplicatedRequests), theme tokens (Color, Type, Shape, Spacing), NavigationManager, CoreModule (Koin) | external libs only |
| **features/{tab}** | One tab's business logic + UI | Clean architecture per feature: data/ (API, DTO, Repository), domain/ (model, service, repository interface), presentation/ (Screen, ViewModel), di/ (Koin module) | core only |

**Rule of thumb:**
- If a feature module needs to import it → it goes in **core**
- If only `App.kt` uses it → it goes in **composeApp**
- If it's specific to one tab → it goes in **features/{tab}**

## Recommended Folder Structure

```
projectRoot/
├── composeApp/                    # App shell + platform entry points
│   └── src/
│       ├── commonMain/kotlin/{pkg}/
│       │   ├── App.kt            # Root composable — single Scaffold, NavHost
│       │   ├── di/KoinInitializer.kt
│       │   └── ui/
│       │       ├── navigation/
│       │       │   └── NavRoutes.kt       # Routes, currentTitle, AppNavHost
│       │       └── splash/
│       │           ├── SplashScreen.kt
│       │           └── SplashViewModel.kt
│       ├── androidMain/           # MainActivity + MainApplication (Koin init)
│       ├── iosMain/               # MainViewController (Koin init)
│       └── desktopMain/           # main() + Taskbar dock icon
├── core/                          # Shared infrastructure
│   └── src/commonMain/kotlin/{pkg}/core/
│       ├── di/CoreModule.kt       # HttpClient + NavigationManager singletons
│       ├── navigation/NavigationManager.kt
│       ├── network/
│       │   ├── NetworkBoundResource.kt    # Offline-first: fetch → save → fallback
│       │   ├── NetworkResult.kt           # Success(data) | Error(exception)
│       │   └── DeduplicatedRequests.kt    # Ktor plugin: cancel duplicate calls
│       ├── theme/                 # {OrgName}Theme, Color, Type, Shape, Spacing, Elevation
│       └── ui/                    # Shared app-level UI components
│           ├── topbar/{Org}TopAppBar.kt   # Menu + title + search + location chip
│           ├── search/InlineSearchField.kt # Inline search field for TopAppBar
│           ├── bottombar/BottomNavigationBar.kt  # NavBarItem + parameterized bottom bar
│           └── drawer/AppDrawerContent.kt # Parameterized drawer content
├── features/dashboard/              # Dashboard feature module
│   └── src/commonMain/kotlin/{pkg}/dashboard/feature/dashboard/
│       ├── data/
│       │   ├── api/DashboardApi.kt
│       │   ├── dto/DashboardDto.kt
│       │   └── repository/DashboardRepositoryImpl.kt
│       ├── domain/
│       │   ├── model/DashboardItem.kt
│       │   ├── repository/DashboardRepository.kt   # Interface
│       │   └── service/DashboardService.kt
│       ├── presentation/
│       │   ├── DashboardScreen.kt
│       │   └── DashboardViewModel.kt               # sealed interface UiState
│       └── di/DashboardModule.kt
├── features/cashflow/              # CashFlow feature module
│   └── src/commonMain/kotlin/{pkg}/cashflow/feature/cashflow/
│       ├── data/
│       │   ├── api/CashFlowApi.kt
│       │   ├── dto/CashFlowDto.kt
│       │   └── repository/CashFlowRepositoryImpl.kt
│       ├── domain/
│       │   ├── model/CashFlowItem.kt
│       │   ├── repository/CashFlowRepository.kt   # Interface
│       │   └── service/CashFlowService.kt
│       ├── presentation/
│       │   ├── CashFlowScreen.kt
│       │   └── CashFlowViewModel.kt               # sealed interface UiState
│       └── di/CashFlowModule.kt
├── features/payroll/              # Payroll feature module
│   └── src/commonMain/kotlin/{pkg}/payroll/feature/payroll/
│       ├── data/
│       │   ├── api/PayrollApi.kt
│       │   ├── dto/PayrollDto.kt
│       │   └── repository/PayrollRepositoryImpl.kt
│       ├── domain/
│       │   ├── model/PayrollItem.kt
│       │   ├── repository/PayrollRepository.kt   # Interface
│       │   └── service/PayrollService.kt
│       ├── presentation/
│       │   ├── PayrollScreen.kt
│       │   └── PayrollViewModel.kt               # sealed interface UiState
│       └── di/PayrollModule.kt
├── features/paycheckplan/              # PaycheckPlan feature module
│   └── src/commonMain/kotlin/{pkg}/paycheckplan/feature/paycheckplan/
│       ├── data/
│       │   ├── api/PaycheckPlanApi.kt
│       │   ├── dto/PaycheckPlanDto.kt
│       │   └── repository/PaycheckPlanRepositoryImpl.kt
│       ├── domain/
│       │   ├── model/PaycheckPlanItem.kt
│       │   ├── repository/PaycheckPlanRepository.kt   # Interface
│       │   └── service/PaycheckPlanService.kt
│       ├── presentation/
│       │   ├── PaycheckPlanScreen.kt
│       │   └── PaycheckPlanViewModel.kt               # sealed interface UiState
│       └── di/PaycheckPlanModule.kt
├── features/investments/              # Investments feature module
│   └── src/commonMain/kotlin/{pkg}/investments/feature/investments/
│       ├── data/
│       │   ├── api/InvestmentsApi.kt
│       │   ├── dto/InvestmentsDto.kt
│       │   └── repository/InvestmentsRepositoryImpl.kt
│       ├── domain/
│       │   ├── model/InvestmentsItem.kt
│       │   ├── repository/InvestmentsRepository.kt   # Interface
│       │   └── service/InvestmentsService.kt
│       ├── presentation/
│       │   ├── InvestmentsScreen.kt
│       │   └── InvestmentsViewModel.kt               # sealed interface UiState
│       └── di/InvestmentsModule.kt
├── features/profile/              # Profile feature module
│   └── src/commonMain/kotlin/{pkg}/profile/feature/profile/
│       ├── data/
│       │   ├── api/ProfileApi.kt
│       │   ├── dto/ProfileDto.kt
│       │   └── repository/ProfileRepositoryImpl.kt
│       ├── domain/
│       │   ├── model/ProfileItem.kt
│       │   ├── repository/ProfileRepository.kt   # Interface
│       │   └── service/ProfileService.kt
│       ├── presentation/
│       │   ├── ProfileScreen.kt
│       │   └── ProfileViewModel.kt               # sealed interface UiState
│       └── di/ProfileModule.kt
```

### Layer Responsibilities

#### 1. Presentation Layer (`presentation/`)
- **Purpose**: Render UI and manage UI state
- **Contains**: Screen composables (`{Tab}Screen.kt`), ViewModel (`{Tab}ViewModel.kt` with sealed `UiState`)
- **Can import**: Domain models, Service
- **Cannot import**: Repository, API, DTOs

#### 2. Domain Layer (`domain/`)
- **Purpose**: Business logic, data transformation
- **Contains**: Service classes (`domain/service/`), Domain models (`domain/model/`), Repository interfaces (`domain/repository/`)
- **Can import**: Repository interfaces, Domain models
- **Cannot import**: ViewModel, DTO, API

#### 3. Data Layer (`data/`)
- **Purpose**: Data access, API calls, persistence
- **Contains**: Repository implementations (`data/repository/`), API clients (`data/api/`), DTOs (`data/dto/`), Room entities (`data/local/`)
- **Can import**: API clients, DTOs, Room DAOs
- **Cannot import**: Service, ViewModel, Domain models

---

## Feature Modules

Each feature module follows the same clean architecture pattern. See the module-level \`CLAUDE.md\` for detailed file structure, Koin wiring, and data flow:

- [features/dashboard/CLAUDE.md](../features/dashboard/CLAUDE.md) — Dashboard\n- [features/cashflow/CLAUDE.md](../features/cashflow/CLAUDE.md) — CashFlow\n- [features/payroll/CLAUDE.md](../features/payroll/CLAUDE.md) — Payroll\n- [features/paycheckplan/CLAUDE.md](../features/paycheckplan/CLAUDE.md) — PaycheckPlan\n- [features/investments/CLAUDE.md](../features/investments/CLAUDE.md) — Investments\n- [features/profile/CLAUDE.md](../features/profile/CLAUDE.md) — Profile


## Implementation Examples

### Example 1: Simple Data Fetching

#### Step 1: Define Domain Model
```kotlin
// domain/model/Item.kt
data class Item(
    val id: String,
    val name: String,
    val price: Double
)
```

#### Step 2: Create Repository
```kotlin
// data/repository/ItemRepository.kt
class ItemRepository(private val apiClient: ApiClient) {
    suspend fun getItems(): List<ItemDto> {
        return apiClient.get("/api/items")
    }
}
```

#### Step 3: Create Service
```kotlin
// domain/service/ItemService.kt
class ItemService(private val itemRepository: ItemRepository) {
    suspend fun fetchItems(): List<Item> {
        val dtos = itemRepository.getItems()
        return dtos.map { dto ->
            Item(
                id = dto.id,
                name = dto.name,
                price = dto.price
            )
        }
    }
}
```

#### Step 4: Create ViewModel
```kotlin
// presentation/ItemViewModel.kt
class ItemViewModel(
    private val service: ItemService
) : ViewModel() {
    private val _uiState = MutableStateFlow<ItemUiState>(ItemUiState.Loading)
    val uiState: StateFlow<ItemUiState> = _uiState.asStateFlow()

    init { loadItems() }

    private fun loadItems() {
        viewModelScope.launch {
            _uiState.value = ItemUiState.Loading
            try {
                val items = service.fetchItems()
                _uiState.value = if (items.isEmpty()) {
                    ItemUiState.Empty
                } else {
                    ItemUiState.Success(items)
                }
            } catch (e: Exception) {
                _uiState.value = ItemUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() { loadItems() }
}

sealed interface ItemUiState {
    data object Loading : ItemUiState
    data object Empty : ItemUiState
    data class Success(val items: List<Item>) : ItemUiState
    data class Error(val message: String) : ItemUiState
}
```

#### Step 5: Create Screen
```kotlin
// presentation/ItemScreen.kt
@Composable
fun ItemScreen(
    modifier: Modifier = Modifier,
    viewModel: ItemViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is ItemUiState.Loading -> CircularProgressIndicator()
        is ItemUiState.Empty -> Text("No items found")
        is ItemUiState.Success -> LazyColumn {
            items(
                items = (uiState as ItemUiState.Success).items,
                key = { it.id }
            ) { item ->
                ItemCard(item = item)
            }
        }
        is ItemUiState.Error -> Text("Error: ${(uiState as ItemUiState.Error).message}")
    }
}
```

---

## Before & After Comparison

### ❌ BEFORE: Without Clean Architecture

```kotlin
@Composable
fun DashboardScreen() {
    var data by remember { mutableStateOf<List<Item>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        // ❌ API call directly in UI
        // ❌ Business logic in composable
        // ❌ No error handling
        // ❌ Hard to test
        val response = httpClient.get("https://api.example.com/items")
        data = response.body()
        isLoading = false
    }

    // UI rendering...
}
```

**Problems:**
- ❌ UI layer doing network calls
- ❌ No separation of concerns
- ❌ Hard to test
- ❌ Can't reuse logic
- ❌ No error handling
- ❌ Violates single responsibility

### ✅ AFTER: With Clean Architecture

```kotlin
// Repository
class DashboardRepository(private val apiClient: ApiClient) {
    suspend fun getItems(): List<ItemDto> = apiClient.get("/items")
}

// Service
class DashboardService(private val repository: DashboardRepository) {
    suspend fun fetchItems(): List<Item> {
        return repository.getItems().map { it.toDomain() }
    }
}

// ViewModel — sealed UiState, no data class
class DashboardViewModel(private val service: DashboardService) : ViewModel() {
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                val items = service.getAll()
                _uiState.value = if (items.isEmpty()) DashboardUiState.Empty
                    else DashboardUiState.Success(items)
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() { loadData() }
}

// Screen — stateful wrapper + stateless content
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // ✅ Clean UI - just rendering based on sealed state
    when (uiState) {
        is DashboardUiState.Loading -> CircularProgressIndicator()
        is DashboardUiState.Empty -> Text("No items found")
        is DashboardUiState.Success -> ItemList(items = (uiState as DashboardUiState.Success).items)
        is DashboardUiState.Error -> Text("Error: ${(uiState as DashboardUiState.Error).message}")
    }
}
```

**Benefits:**
- ✅ Each layer has ONE job
- ✅ Easy to test each layer independently
- ✅ Reusable across platforms
- ✅ Proper error handling
- ✅ Type-safe
- ✅ Maintainable

---

## Testing Strategy

### 1. Unit Tests (Business Logic)
```kotlin
class DashboardServiceTest {
    @Test
    fun `fetchItems should filter invalid items`() = runTest {
        // Given
        val repository = mock<DashboardRepository>()
        val service = DashboardService(repository)

        // When
        val result = service.fetchItems()

        // Then
        assertTrue(result.all { it.isValid })
    }
}
```

### 2. ViewModel Tests
```kotlin
class DashboardViewModelTest {
    @Test
    fun `state updates when data loads successfully`() = runTest {
        // Given
        val service = mock<DashboardService>()
        val viewModel = DashboardViewModel(service)

        // When
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.items.isNotEmpty())
    }
}
```

### 3. UI Tests
```kotlin
@Test
fun `screen displays items when loaded`() {
    composeTestRule.setContent {
        DashboardScreen()
    }

    composeTestRule.onNodeWithText("Item 1").assertIsDisplayed()
}
```

---

## Migration Checklist

When adding a new feature or refactoring existing code:

- [ ] Create domain model in `domain/model/{Name}Item.kt`
- [ ] Create repository interface in `domain/repository/{Name}Repository.kt`
- [ ] Create DTOs in `data/dto/{Name}Dto.kt`
- [ ] Create API client in `data/api/{Name}Api.kt`
- [ ] Implement repository in `data/repository/{Name}RepositoryImpl.kt`
- [ ] Implement service in `domain/service/{Name}Service.kt`
- [ ] Implement ViewModel in `presentation/{Name}ViewModel.kt` (with sealed `UiState`)
- [ ] Create Screen in `presentation/{Name}Screen.kt`
- [ ] Register dependencies in `di/{Name}Module.kt`
- [ ] Write unit tests for Service
- [ ] Write tests for ViewModel
- [ ] Write UI tests for Screen
- [ ] Update navigation if needed

---

## Summary

This architecture ensures:
- ✅ **Testability**: Each layer tested independently
- ✅ **Maintainability**: Changes isolated to specific layers
- ✅ **Scalability**: Easy to add new features
- ✅ **Type Safety**: Kotlin prevents runtime errors
- ✅ **Multiplatform**: commonMain code works everywhere
- ✅ **Team Collaboration**: Clear boundaries prevent conflicts

**Remember: Dependencies always point INWARD (UI → ViewModel → Service → Repository → API)**

---

*Generated for Core Module, Dashboard, CashFlow, Payroll, PaycheckPlan, Investments, Profile modules*
*Package: com.goldenboot.goldenbootfinancial*
