# PaycheckPlan Module

Feature module for the **Paycheck Plan** tab.

## File Structure

```
features/paycheckplan/src/commonMain/kotlin/.../feature/paycheckplan/
├── data/
│   ├── api/PaycheckPlanApi.kt             # Ktor HttpClient calls
│   ├── dto/PaycheckPlanDto.kt             # @Serializable DTOs
│   └── repository/PaycheckPlanRepositoryImpl.kt
├── domain/
│   ├── model/PaycheckPlanItem.kt          # Domain model
│   ├── repository/PaycheckPlanRepository.kt  # Interface
│   └── service/PaycheckPlanService.kt     # Business logic
├── presentation/
│   ├── PaycheckPlanScreen.kt              # Stateful + Stateless
│   └── PaycheckPlanViewModel.kt           # Sealed UiState
└── di/PaycheckPlanModule.kt               # Koin wiring
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
- **ViewModel**: Exposes `StateFlow<PaycheckPlanUiState>`. Uses `viewModelScope` for coroutines.
  ```kotlin
  sealed interface PaycheckPlanUiState {
      data object Loading : PaycheckPlanUiState
      data object Empty : PaycheckPlanUiState
      data class Success(val items: List<PaycheckPlanItem>) : PaycheckPlanUiState
      data class Error(val message: String) : PaycheckPlanUiState
  }
  ```
- **Screen**: Stateful wrapper (injects ViewModel via `koinViewModel()`) + stateless `PaycheckPlanContent` (takes state + callbacks). No nested Scaffold — receives `modifier` from parent.

## Koin Wiring

```kotlin
val paycheckplanModule = module {
    single { PaycheckPlanApi(get()) }                              // get() → HttpClient from core
    single<PaycheckPlanRepository> { PaycheckPlanRepositoryImpl(api = get()) }
    single { PaycheckPlanService(get()) }
    viewModel { PaycheckPlanViewModel(get()) }
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
2. Follow the same layer pattern as `paycheckplan/`
3. Register the new Koin module in `KoinInitializer.kt`
4. Add navigation route in `NavRoutes.kt` and composable destination in `App.kt`
