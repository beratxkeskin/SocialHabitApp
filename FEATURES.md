# Feature Specification

## 1. Authentication

### User Stories

As a user, I can:
- create an account
- log in
- log out
- remain logged in after restarting the app
- create and maintain a basic profile (display name, username, avatar, timezone)

### Acceptance Criteria

- Invalid credentials show a useful error.
- Loading state is visible.
- Session survives app restart.
- Auth state controls navigation.
- Device IANA timezone is synchronized to the user profile.

## 2. Habit Management

### User Stories

As a user, I can:
- create a habit
- give it a title
- optionally add a description
- define a frequency schedule (`daily`, `selected_days`, `weekly_target`)
- edit it
- archive it
- see my active habits

### Acceptance Criteria

- Habit title is required.
- Frequency schedule is validated (`daily`, `selected_days` with 1-7 day indices, `weekly_target` with 1-7 target days).
- Newly created habit appears in the list.
- Archived habits no longer appear in the default active list.
- Only authorized active members can access habit details.

## 3. Partner Invitation

### User Stories

As a habit owner, I can:
- invite another user by username
- see pending invitations

As an invited user, I can:
- see received invitations
- accept invitation
- reject invitation

### Acceptance Criteria

- Invitations are managed through `habit_invitations`.
- Duplicate pending invitations or duplicate memberships are prevented.
- Only authorized owners may invite.
- A rejected invitation does not grant habit access.
- Accepting an invitation creates an active `habit_members` record.

## 4. Daily Check-In

### User Stories

As a habit member, I can:
- mark today's habit as completed with a single tap
- optionally add a short note
- see whether other members checked in for their local calendar day

### Acceptance Criteria

- Single-tap completion action.
- Only one check-in per habit/user/local calendar day (`checkin_date`).
- Check-in is immutable once created for that day; no undo in MVP.
- Check-in status updates the UI immediately.
- Partner status updates in near real-time through Supabase Realtime.
- Check-in errors are handled gracefully.

## 5. Nudge

### User Stories

As a habit member, I can:
- nudge a partner who has not checked in for their local calendar day
- cause that member to receive a push notification

### Acceptance Criteria

- Sender and receiver belong to the same active habit.
- Receiver has not completed today's habit for their local calendar day.
- Conservative rate limit: max 1 nudge per sender/receiver/habit/receiver local calendar day.
- Android UI disables nudge and indicates status for UX purposes.
- Real authorization, limit enforcement, and FCM HTTP v1 delivery are executed inside the `send-nudge` Supabase Edge Function.
- Nudge record is created in PostgreSQL upon successful delivery.

## 6. Home Screen

The home screen prioritizes today's accountability loop.

It should show:

- Today's habits
- User completion status (single-tap check-in)
- Partner/member completion status
- Nudge action (active only if partner has not completed and limit not reached)
- Current streak

The UI must make it possible to understand today's state within a few seconds.

## 7. History & Retention

Users can see basic historical completion information.

Initial scope:

- Recent check-in history
- Simple weekly completion summary
- Current streak (derived dynamically by domain logic from check-in history across local calendar days)

Advanced analytics and server-side cached aggregates are not MVP requirements.

## 8. Profile and Settings

Initial profile:

- Display name
- Username
- Avatar
- Timezone (auto-synced)

Initial settings:

- Notification preferences
- Logout

## 9. Notification Types

MVP notification types:

- Habit invitation
- Invitation accepted
- Nudge received
- Optional habit reminder

Avoid excessive notification categories during MVP.

## 10. Analytics Events

Suggested events:

```text
sign_up_completed
login_completed
habit_created
habit_invite_sent
habit_invite_accepted
checkin_completed
nudge_sent
nudge_notification_opened
weekly_summary_viewed
```

Do not log sensitive user-generated content in analytics.

