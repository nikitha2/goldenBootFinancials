# Profile Module

Feature module for the **Profile** tab.

## File Structure

```
features/profile/src/commonMain/kotlin/.../feature/profile/
├── data/
│   ├── api/ProfileApi.kt             # Ktor HttpClient calls
│   ├── dto/ProfileDto.kt             # @Serializable DTOs
│   └── repository/ProfileRepositoryImpl.kt
├── domain/
│   ├── model/ProfileItem.kt          # Domain model
│   ├── repository/ProfileRepository.kt  # Interface
│   └── service/ProfileService.kt     # Business logic
├── presentation/
│   ├── ProfileScreen.kt              # Stateful + Stateless
│   └── ProfileViewModel.kt           # Sealed UiState
└── di/ProfileModule.kt               # Koin wiring
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
- **ViewModel**: Exposes `StateFlow<ProfileUiState>`. Uses `viewModelScope` for coroutines.
  ```kotlin
  sealed interface ProfileUiState {
      data object Loading : ProfileUiState
      data object Empty : ProfileUiState
      data class Success(val items: List<ProfileItem>) : ProfileUiState
      data class Error(val message: String) : ProfileUiState
  }
  ```
- **Screen**: Stateful wrapper (injects ViewModel via `koinViewModel()`) + stateless `ProfileContent` (takes state + callbacks). No nested Scaffold — receives `modifier` from parent.

## Koin Wiring

```kotlin
val profileModule = module {
    single { ProfileApi(get()) }                              // get() → HttpClient from core
    single<ProfileRepository> { ProfileRepositoryImpl(api = get()) }
    single { ProfileService(get()) }
    viewModel { ProfileViewModel(get()) }
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
2. Follow the same layer pattern as `profile/`
3. Register the new Koin module in `KoinInitializer.kt`
4. Add navigation route in `NavRoutes.kt` and composable destination in `App.kt`
