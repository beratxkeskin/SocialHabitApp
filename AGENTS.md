# Coding Agent Instructions

This file defines mandatory engineering rules for AI coding agents working in this repository.

## 1. Product Context

This repository contains a social habit accountability mobile application.

The primary product loop is:

Habit -> Daily Check-in -> Partner Status -> Nudge -> Push Notification -> Check-in

Protect this loop from unnecessary complexity.

## 2. Required Technology

Use:

- Kotlin
- Jetpack Compose
- Material 3
- MVVM
- Clean Architecture
- Hilt
- Coroutines
- Flow / StateFlow
- Supabase
- Firebase Cloud Messaging
- Firebase Crashlytics
- Firebase Analytics

Do not replace these technologies without explicit approval.

## 3. Backend Rules

Supabase is the primary backend.

Use Supabase for:

- PostgreSQL
- Auth
- Realtime
- Storage
- Edge Functions

Firebase may only be used for:

- FCM
- Crashlytics
- Analytics

Do not introduce Firestore.

Do not introduce Firebase Authentication.

## 4. Architecture Rules

Follow:

Presentation -> Domain
Data -> Domain

Domain must remain framework-independent.

UI code must not call Supabase or Firebase directly.

ViewModels must not contain direct database/network SDK calls.

Repository interfaces belong in Domain.

Repository implementations belong in Data.

Business logic should be implemented in use cases or appropriate domain components.

## 5. Compose Rules

Prefer stateless composables where practical.

Screens should receive state and callbacks.

Do not place repository or backend logic inside composables.

Provide Preview functions for reusable visual components where practical.

Use Material 3.

Keep strings localization-ready.

## 6. State Rules

Expose immutable UI state via StateFlow.

Do not expose MutableStateFlow publicly.

Represent loading, error and success states explicitly.

Avoid unnecessary global state.

## 7. Security Rules

Never assume client-side authorization is sufficient.

Database authorization must be enforced by Supabase RLS.

Never place server secrets or service-role keys in the Android application.

Never commit secrets.

Privileged operations must use secure backend mechanisms such as Edge Functions.

## 8. Dependency Rules

Do not add a new library unless:

1. Existing project dependencies cannot reasonably solve the problem.
2. The reason is explained before implementation.
3. The dependency is actively maintained and appropriate.

Avoid dependency inflation.

## 9. Scope Rules

Do not implement features outside the current sprint.

Do not introduce:

- AI coach
- leaderboards
- subscriptions
- random matchmaking
- public social feed
- complex gamification
- iOS code
- web frontend

unless explicitly requested.

## 10. Implementation Workflow

Before making a non-trivial change:

1. Inspect the relevant codebase.
2. Explain the proposed implementation plan.
3. Identify files to create/change.
4. Identify architectural implications.
5. Identify tests to add.

Do not perform broad refactors unrelated to the requested feature.

After implementation:

1. Run relevant tests.
2. Run build/compile checks.
3. Summarize changed files.
4. Report unresolved issues.
5. Do not claim success if tests/build were not run.

## 11. Testing Rules

Prioritize tests for:

- Use cases
- Domain validation
- Repository behavior
- Mappers
- ViewModels
- Security-sensitive behavior

Avoid testing trivial getters/setters.

## 12. Git Discipline

Keep changes focused.

Do not modify unrelated files.

Do not delete existing working code unless required.

Prefer small commits/features over massive rewrites.

## 13. Engineering Principles

Follow:

- SOLID
- DRY where duplication is meaningful
- KISS
- YAGNI

Do not over-engineer the MVP.

Readable simple code is preferred over clever abstractions.

## 14. When Uncertain

If implementation choices materially affect:

- architecture
- security
- database schema
- public API contracts
- core product scope

stop and propose alternatives before making the change.
