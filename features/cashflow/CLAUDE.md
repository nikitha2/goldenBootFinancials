# CashFlow Module

Feature module for the **Cash Flow** tab.

## File Structure

```
features/cashflow/src/commonMain/kotlin/.../feature/cashflow/
├── data/
│   ├── api/CashFlowApi.kt             # Ktor HttpClient calls
│   ├── dto/CashFlowDto.kt             # @Serializable DTOs
│   └── repository/CashFlowRepositoryImpl.kt
├── domain/
│   ├── model/CashFlowItem.kt          # Domain model
│   ├── repository/CashFlowRepository.kt  # Interface
│   └── service/CashFlowService.kt     # Business logic
├── presentation/
│   ├── CashFlowScreen.kt              # Stateful + Stateless
│   └── CashFlowViewModel.kt           # Sealed UiState
└── di/CashFlowModule.kt               # Koin wiring
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
- **ViewModel**: Exposes `StateFlow<CashFlowUiState>`. Uses `viewModelScope` for coroutines.
  ```kotlin
  sealed interface CashFlowUiState {
      data object Loading : CashFlowUiState
      data object Empty : CashFlowUiState
      data class Success(val items: List<CashFlowItem>) : CashFlowUiState
      data class Error(val message: String) : CashFlowUiState
  }
  ```
- **Screen**: Stateful wrapper (injects ViewModel via `koinViewModel()`) + stateless `CashFlowContent` (takes state + callbacks). No nested Scaffold — receives `modifier` from parent.

## Koin Wiring

```kotlin
val cashflowModule = module {
    single { CashFlowApi(get()) }                              // get() → HttpClient from core
    single<CashFlowRepository> { CashFlowRepositoryImpl(api = get()) }
    single { CashFlowService(get()) }
    viewModel { CashFlowViewModel(get()) }
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
2. Follow the same layer pattern as `cashflow/`
3. Register the new Koin module in `KoinInitializer.kt`
4. Add navigation route in `NavRoutes.kt` and composable destination in `App.kt`
