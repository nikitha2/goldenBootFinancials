# Investments Module

Feature module for the **Investments** tab.

## File Structure

```
features/investments/src/commonMain/kotlin/.../feature/investments/
├── data/
│   ├── api/InvestmentsApi.kt             # Ktor HttpClient calls
│   ├── dto/InvestmentsDto.kt             # @Serializable DTOs
│   └── repository/InvestmentsRepositoryImpl.kt
├── domain/
│   ├── model/InvestmentsItem.kt          # Domain model
│   ├── repository/InvestmentsRepository.kt  # Interface
│   └── service/InvestmentsService.kt     # Business logic
├── presentation/
│   ├── InvestmentsScreen.kt              # Stateful + Stateless
│   └── InvestmentsViewModel.kt           # Sealed UiState
└── di/InvestmentsModule.kt               # Koin wiring
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
- **ViewModel**: Exposes `StateFlow<InvestmentsUiState>`. Uses `viewModelScope` for coroutines.
  ```kotlin
  sealed interface InvestmentsUiState {
      data object Loading : InvestmentsUiState
      data object Empty : InvestmentsUiState
      data class Success(val items: List<InvestmentsItem>) : InvestmentsUiState
      data class Error(val message: String) : InvestmentsUiState
  }
  ```
- **Screen**: Stateful wrapper (injects ViewModel via `koinViewModel()`) + stateless `InvestmentsContent` (takes state + callbacks). No nested Scaffold — receives `modifier` from parent.

## Koin Wiring

```kotlin
val investmentsModule = module {
    single { InvestmentsApi(get()) }                              // get() → HttpClient from core
    single<InvestmentsRepository> { InvestmentsRepositoryImpl(api = get()) }
    single { InvestmentsService(get()) }
    viewModel { InvestmentsViewModel(get()) }
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
2. Follow the same layer pattern as `investments/`
3. Register the new Koin module in `KoinInitializer.kt`
4. Add navigation route in `NavRoutes.kt` and composable destination in `App.kt`
