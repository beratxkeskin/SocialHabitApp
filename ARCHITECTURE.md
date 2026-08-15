# Architecture Specification

## 1. Architecture Goal

The Android application must remain maintainable, testable, modular and independent from specific backend implementations wherever practical.

The project uses:

- Kotlin
- Jetpack Compose
- MVVM
- Clean Architecture
- Repository Pattern
- Use Cases
- Hilt
- Kotlin Coroutines
- Flow / StateFlow

## 2. Layer Responsibilities

### Presentation Layer

Responsibilities:

- Jetpack Compose UI (Material 3)
- Screen state rendering (Loading, Success, Empty, Error)
- User interaction handling (Single-tap check-in, Nudge request)
- ViewModels exposing immutable `StateFlow<UiState>`
- Navigation events

Presentation must not:

- Access Supabase directly
- Access Firebase directly
- Contain SQL/backend logic
- Contain complex business rules

Typical flow:

View -> ViewModel -> UseCase

### Domain Layer

Contains:

- Domain models (`User`, `Profile`, `Habit`, `HabitSchedule`, `HabitMember`, `HabitInvitation`, `CheckIn`, `Nudge`)
- Repository interfaces (`HabitRepository`, `CheckInRepository`, `AuthRepository`, `NudgeRepository`, `InvitationRepository`)
- Use cases (`CalculateStreakUseCase`, `CreateHabitUseCase`, `GetHabitsUseCase`, `CheckInHabitUseCase`, `SendNudgeUseCase`, `RespondToInvitationUseCase`)
- Domain-level validation/business rules (e.g. Schedule validation, Streak derivation logic)

Domain model example for frequency:

```kotlin
sealed interface HabitSchedule {
    data object Daily : HabitSchedule
    data class SelectedDays(val daysOfWeek: Set<Int>) : HabitSchedule // 1..7
    data class WeeklyTarget(val targetDays: Int) : HabitSchedule // 1..7
}
```

Domain must not depend on:

- Android framework
- Supabase SDK
- Firebase SDK
- Room
- Retrofit

The domain layer is independently unit-testable.

### Data Layer

Contains:

- Repository implementations
- Remote data sources (Supabase Client, Edge Function caller)
- Local data sources (DataStore for user preferences, Room if offline caching is needed in future)
- DTOs (Data Transfer Objects)
- Database entities
- Mappers (DTO <-> Domain Model)

Typical flow:

Repository Implementation
  -> Remote Data Source
  -> Local Data Source
  -> External SDK/API

## 3. Dependency Direction

Dependencies must point inward:

Presentation -> Domain
Data -> Domain

Domain must not depend on Presentation or Data.

## 4. Backend Responsibilities

### Supabase

Supabase is the primary backend and single source of truth.

Use Supabase for:

- PostgreSQL (Persistent relational data: profiles, habits, habit_members, habit_invitations, checkins, nudges, user_devices)
- Authentication (Email/Password registration, login, JWT session management)
- Realtime (WebSocket changes for member check-in states and invitations)
- Storage (User avatars)
- Edge Functions (`send-nudge` privileged operation, rate limiting, and push trigger)

Do not use Firebase Firestore.

### Firebase

Firebase is used only for mobile infrastructure:

- Firebase Cloud Messaging (FCM HTTP v1 delivery triggered via Supabase Edge Function)
- Crashlytics (Crash and stability monitoring)
- Analytics (Funnel and product loop metrics)

Firebase Authentication is not used.

## 5. Local Persistence

### Room

Use Room when structured offline/local caching is needed.
Do not introduce Room until a concrete offline/cache requirement exists.

### DataStore

Use DataStore for lightweight preferences:

- Notification preferences
- Local settings

## 6. State Management

UI state uses immutable state models exposed through `StateFlow`.

Example:

```kotlin
data class HabitDetailUiState(
    val isLoading: Boolean = false,
    val habit: Habit? = null,
    val streak: Int = 0,
    val errorMessage: String? = null
)
```

ViewModels expose:

```kotlin
val uiState: StateFlow<HabitDetailUiState>
```

Avoid exposing mutable state publicly.

## 7. Error Handling

Use predictable application-level error handling.

Errors are translated from infrastructure-specific errors into domain/application errors before reaching the UI when possible.

The UI supports at minimum:

- Loading
- Success
- Empty
- Error

## 8. Dependency Injection

Use Hilt.

Repository interfaces are defined in the domain layer.
Repository implementations are bound in DI modules.

Example:

```kotlin
@Binds
abstract fun bindHabitRepository(
    impl: HabitRepositoryImpl
): HabitRepository
```

## 9. Package Structure

Initial package structure:

```text
com.example.app
|
|-- core
|   |-- common
|   |-- designsystem
|   |-- navigation
|   `-- util
|
|-- data
|   |-- remote
|   |-- local
|   |-- mapper
|   `-- repository
|
|-- domain
|   |-- model
|   |-- repository
|   `-- usecase
|
|-- feature
|   |-- auth
|   |-- home
|   |-- habit
|   |-- partner
|   |-- checkin
|   |-- profile
|   `-- statistics
|
`-- di
```

Do not create multiple Gradle modules during the MVP unless project complexity justifies it.

## 10. Testing Strategy

Priority:

1. Domain use case unit tests (`CalculateStreakUseCase`, `CheckInHabitUseCase`, etc.)
2. Repository tests
3. Mapper tests
4. ViewModel tests
5. Critical Compose UI tests

## 11. Security Principles

Never rely on the Android client for authorization.

Authorization is enforced by:

- Supabase Row Level Security (RLS)
- PostgreSQL constraints (`UNIQUE`, foreign keys)
- Supabase Edge Functions (`send-nudge` validates auth, active membership, local check-in status, and rate limit before inserting and calling FCM)

Client-side checks are only for user experience (e.g. disabling the nudge button optimistically).

## 12. Future iOS Compatibility

Backend contracts and business concepts remain platform-independent.

The future iOS client will share the identical:

- Database schema & RLS policies
- Authentication
- Edge Functions & API contracts
- Push notification backend (via FCM APNs integration)
- Business rules & frequency representations

