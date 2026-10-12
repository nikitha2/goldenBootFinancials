# Payroll Module

Feature module for the **Payroll & Payday** tab.

## File Structure

```
features/payroll/src/commonMain/kotlin/.../feature/payroll/
├── data/
│   ├── api/PayrollApi.kt             # Ktor HttpClient calls
│   ├── dto/PayrollDto.kt             # @Serializable DTOs
│   └── repository/PayrollRepositoryImpl.kt
├── domain/
│   ├── model/PayrollItem.kt          # Domain model
│   ├── repository/PayrollRepository.kt  # Interface
│   └── service/PayrollService.kt     # Business logic
├── presentation/
│   ├── PayrollScreen.kt              # Stateful + Stateless
│   └── PayrollViewModel.kt           # Sealed UiState
└── di/PayrollModule.kt               # Koin wiring
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
- **ViewModel**: Exposes `StateFlow<PayrollUiState>`. Uses `viewModelScope` for coroutines.
  ```kotlin
  sealed interface PayrollUiState {
      data object Loading : PayrollUiState
      data object Empty : PayrollUiState
      data class Success(val items: List<PayrollItem>) : PayrollUiState
      data class Error(val message: String) : PayrollUiState
  }
  ```
- **Screen**: Stateful wrapper (injects ViewModel via `koinViewModel()`) + stateless `PayrollContent` (takes state + callbacks). No nested Scaffold — receives `modifier` from parent.

## Koin Wiring

```kotlin
val payrollModule = module {
    single { PayrollApi(get()) }                              // get() → HttpClient from core
    single<PayrollRepository> { PayrollRepositoryImpl(api = get()) }
    single { PayrollService(get()) }
    viewModel { PayrollViewModel(get()) }
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
2. Follow the same layer pattern as `payroll/`
3. Register the new Koin module in `KoinInitializer.kt`
4. Add navigation route in `NavRoutes.kt` and composable destination in `App.kt`
