# Development Plan

## Development Philosophy

Build one small, complete vertical slice at a time.

For each feature:

1. Analyze existing code.
2. Produce an implementation plan.
3. Review the plan.
4. Implement.
5. Build.
6. Run tests.
7. Review diff.
8. Manually test.
9. Commit.

Do not ask the coding agent to generate the complete application at once.

---

# Sprint 0 - Foundation

## Goals

- Finalize product scope.
- Finalize architecture.
- Define database schema.
- Create Android project.
- Configure Git repository.
- Create Supabase project.
- Create Firebase project.
- Establish basic CI.

## Deliverables

- PRODUCT.md
- ARCHITECTURE.md
- DATABASE.md
- FEATURES.md
- DEVELOPMENT_PLAN.md
- AGENTS.md
- Android project builds successfully
- Git repository exists
- Supabase project configured
- Firebase Android application configured

---

# Sprint 1 - Authentication

Implement:

- Supabase Auth configuration
- Register
- Login
- Logout
- Session persistence
- Profile creation (including device IANA timezone sync)
- Authentication navigation

Definition of Done:

- Unit-testable domain logic
- Loading/error states
- App builds
- Manual happy-path test passes

---

# Sprint 2 - Habit Management

Implement:

- Habit domain model & `HabitSchedule` (`Daily`, `SelectedDays`, `WeeklyTarget`)
- Habit repository
- CreateHabitUseCase
- GetHabitsUseCase
- UpdateHabitUseCase
- ArchiveHabitUseCase
- Habit list UI
- Habit creation UI (schedule picker)
- Habit detail UI

---

# Sprint 3 - Social Membership

Implement:

- Habit invitations domain model & repository (`habit_invitations`)
- Active habit membership repository (`habit_members`)
- Invite flow (send invite by username)
- Accept invitation
- Reject invitation
- Member list UI
- Partner status foundation

---

# Sprint 4 - Core Product Loop

This is the highest-priority product sprint.

Implement:

- Single-tap daily check-in (immutable per local day)
- Today's completion state
- Partner/member completion state
- Supabase Realtime update
- Nudge operation UI
- `send-nudge` Supabase Edge Function (auth, membership check, local check-in check, 1-nudge/day rate limit)
- FCM push notification (HTTP v1 API)
- Deep-link/open relevant habit if practical

The MVP should be considered conceptually validated only after this loop works reliably.

---

# Sprint 5 - Retention

Implement:

- Dynamic streak derivation (`CalculateStreakUseCase` based on check-in history across local calendar days)
- Basic check-in history
- Weekly summary
- Basic reminder preferences

Do not build complex gamification.

---

# Sprint 6 - Production Quality

Implement:

- Crashlytics
- Firebase Analytics
- Security review
- RLS review
- Error handling review
- Accessibility review
- Localization-ready strings
- Performance checks
- CI improvements
- Closed beta

---

# Future Backlog

Do not implement without product validation:

- Groups
- Challenges
- Proof photos
- AI coach
- Advanced analytics
- Subscriptions
- iOS
- Public social features
- Random partner matching
