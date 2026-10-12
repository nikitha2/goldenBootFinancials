# Dashboard Module

Feature module for the **Dashboard** tab.

## File Structure

```
features/dashboard/src/commonMain/kotlin/.../feature/dashboard/
├── data/
│   ├── api/DashboardApi.kt             # Ktor HttpClient calls
│   ├── dto/DashboardDto.kt             # @Serializable DTOs
│   └── repository/DashboardRepositoryImpl.kt
├── domain/
│   ├── model/DashboardItem.kt          # Domain model
│   ├── repository/DashboardRepository.kt  # Interface
│   └── service/DashboardService.kt     # Business logic
├── presentation/
│   ├── DashboardScreen.kt              # Stateful + Stateless
│   └── DashboardViewModel.kt           # Sealed UiState
└── di/DashboardModule.kt               # Koin wiring
```

## Clean Architecture Layers

### Data Layer
- **DTO**: `@Serializable` data class with `toDomain()` mapper
- **API**: Ktor `HttpClient` calls (`get`, `post`, etc.)
- **Repository impl**: Uses `networkFetch` — simple try/catch returning `NetworkResult`

### Domain Layer
- **Model**: Plain Kotlin data class (no framework annotations)
- **Repository interface**: Defines contract, lives here so domain has no data-layer imports
- **Service**: Business logic — sorting, filtering, validation. Pure functions, easy to test.

### Presentation Layer
- **ViewModel**: Exposes `StateFlow<DashboardUiState>`. Uses `viewModelScope` for coroutines.
  ```kotlin
  sealed interface DashboardUiState {
      data object Loading : DashboardUiState
      data object Empty : DashboardUiState
      data class Success(val items: List<DashboardItem>) : DashboardUiState
      data class Error(val message: String) : DashboardUiState
  }
  ```
- **Screen**: Stateful wrapper (injects ViewModel via `koinViewModel()`) + stateless `DashboardContent` (takes state + callbacks). No nested Scaffold — receives `modifier` from parent.

## Koin Wiring

```kotlin
val dashboardModule = module {
    single { DashboardApi(get()) }                              // get() → HttpClient from core
    single<DashboardRepository> { DashboardRepositoryImpl(api = get()) }
    single { DashboardService(get()) }
    viewModel { DashboardViewModel(get()) }
}
```

Registered in `KoinInitializer.kt` → loaded at app startup.

## Data Flow

```
User action → Screen → ViewModel → Service → Repository → API
                                                          
Response:   Screen ← ViewModel ← Service (DTO → domain) ← Repository ← API
```

## Adding a New Feature to This Tab

Use the `/add-feature-to-tab` skill, or manually:
1. Create `data/`, `domain/`, `presentation/`, `di/` under `feature/{newFeatureName}/`
2. Follow the same layer pattern as `dashboard/`
3. Register the new Koin module in `KoinInitializer.kt`
4. Add navigation route in `NavRoutes.kt` and composable destination in `App.kt`
