# Database Design

## 1. Database

Primary database:

PostgreSQL via Supabase.

Supabase PostgreSQL is the single source of truth for product data.

## 2. Initial Tables

### profiles

Stores application-level user profile information.

```text
id UUID PRIMARY KEY REFERENCES auth.users(id)
username TEXT UNIQUE NOT NULL
display_name TEXT NOT NULL
avatar_url TEXT NULL
timezone TEXT NOT NULL DEFAULT 'UTC'
created_at TIMESTAMPTZ NOT NULL DEFAULT now()
updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
```

- `timezone`: IANA timezone identifier (e.g. `Europe/Istanbul`, `America/New_York`) synced from device on app start / login.

### habits

```text
id UUID PRIMARY KEY
title TEXT NOT NULL
description TEXT NULL
frequency_type TEXT NOT NULL
target_days_per_week INT NULL
selected_days INT[] NULL
created_by UUID NOT NULL REFERENCES profiles(id)
created_at TIMESTAMPTZ NOT NULL DEFAULT now()
updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
archived_at TIMESTAMPTZ NULL
```

Frequency types:
- `daily`: Habit is expected every day (`target_days_per_week = 7`).
- `selected_days`: Habit is expected on specific days (e.g. `selected_days = ARRAY[1, 3, 5]` where 1 = Monday, 7 = Sunday).
- `weekly_target`: Habit is expected N days per week (e.g. `target_days_per_week = 3`).

### habit_members

Represents active, confirmed users participating in a habit.

```text
habit_id UUID NOT NULL REFERENCES habits(id) ON DELETE CASCADE
user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE
role TEXT NOT NULL
joined_at TIMESTAMPTZ NOT NULL DEFAULT now()

PRIMARY KEY (habit_id, user_id)
```

Possible role values:
- `owner`
- `member`

`habit_members` only stores active members. Unaccepted or rejected invitations belong in `habit_invitations`.

### habit_invitations

Manages the invitation lifecycle between users.

```text
id UUID PRIMARY KEY
habit_id UUID NOT NULL REFERENCES habits(id) ON DELETE CASCADE
invited_by UUID NOT NULL REFERENCES profiles(id)
invited_user_id UUID NOT NULL REFERENCES profiles(id)
status TEXT NOT NULL
created_at TIMESTAMPTZ NOT NULL DEFAULT now()
responded_at TIMESTAMPTZ NULL
```

Possible status values:
- `pending`
- `accepted`
- `rejected`

Accepting an invitation inserts a record into `habit_members` and updates `habit_invitations.status` to `accepted`.

### checkins

Stores daily completion records.

```text
id UUID PRIMARY KEY
habit_id UUID NOT NULL REFERENCES habits(id) ON DELETE CASCADE
user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE
checkin_date DATE NOT NULL
note TEXT NULL
created_at TIMESTAMPTZ NOT NULL DEFAULT now()

UNIQUE (habit_id, user_id, checkin_date)
```

- `checkin_date`: The user's local calendar date calculated based on the user's timezone.
- `UNIQUE (habit_id, user_id, checkin_date)` guarantees only one check-in per user/habit/local day. Check-ins are immutable in MVP.

### nudges

Stores sent nudge records.

```text
id UUID PRIMARY KEY
habit_id UUID NOT NULL REFERENCES habits(id) ON DELETE CASCADE
sender_id UUID NOT NULL REFERENCES profiles(id)
receiver_id UUID NOT NULL REFERENCES profiles(id)
created_at TIMESTAMPTZ NOT NULL DEFAULT now()
read_at TIMESTAMPTZ NULL
```

Rate limiting and push delivery are enforced via the `send-nudge` Edge Function (max 1 nudge per sender/receiver/habit/receiver local calendar day).

### user_devices

Stores push notification device registrations.

```text
id UUID PRIMARY KEY
user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE
platform TEXT NOT NULL
fcm_token TEXT NOT NULL
created_at TIMESTAMPTZ NOT NULL DEFAULT now()
updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
```

Possible platform values:
- `android`
- `ios`

## 3. Relationships

```text
auth.users
    │ (1:1)
    ▼
profiles
    │
    ├─────────────────────────────────────────┐
    │ (1:N)                                   │ (1:N)
    ▼                                         ▼
habits                                  user_devices
    │
    ├──────────────────────┬──────────────────────┬──────────────────────┐
    │ (1:N)                │ (1:N)                │ (1:N)                │ (1:N)
    ▼                      ▼                      ▼                      ▼
habit_members      habit_invitations           checkins                nudges
```

## 4. Important Constraints

Database constraints enforce basic data correctness:

- `profiles.username` must be unique.
- One check-in per user/habit/local calendar date (`UNIQUE (habit_id, user_id, checkin_date)`).
- Membership combinations must be unique (`PRIMARY KEY (habit_id, user_id)`).
- Habit creator and members must exist.

## 5. Index Strategy

```sql
CREATE INDEX idx_habit_members_user_id
ON habit_members(user_id);

CREATE INDEX idx_habit_invitations_receiver_status
ON habit_invitations(invited_user_id, status);

CREATE INDEX idx_checkins_user_habit_date
ON checkins(user_id, habit_id, checkin_date DESC);

CREATE INDEX idx_checkins_habit_date
ON checkins(habit_id, checkin_date);

CREATE INDEX idx_nudges_receiver_habit_created
ON nudges(receiver_id, habit_id, created_at DESC);
```

## 6. Row Level Security

RLS must be enabled on all user-facing tables:

### profiles
- Authenticated users may read profiles needed for collaboration.
- Users may update only their own profile.

### habits
- Only active habit members (`habit_members`) may read a habit.
- Only authorized roles (owner) may update/archive it.

### habit_members
- Only members of the relevant habit may view membership information.
- Direct INSERT/DELETE restricted to authorized flows (e.g. invitation acceptance).

### habit_invitations
- Inviter can view sent invitations.
- Invitee can view received invitations.
- Invitee can update invitation status (accept/reject).

### checkins
- Users may insert only their own check-ins.
- Users may view check-ins only for habits where they are an active member.
- Check-ins cannot be deleted or modified by other users.

### nudges
- Users may read nudges where they are sender or receiver.
- Direct insertion is gated through the `send-nudge` Edge Function.

### user_devices
- Users may manage only their own device tokens.
- Device tokens must never be publicly readable.

## 7. Privileged Operations (Edge Functions)

Privileged operations must use Supabase Edge Functions:

### `send-nudge`
Responsibilities:
1. Validate sender JWT / authentication.
2. Verify that both sender and receiver are active members of the specified habit.
3. Verify that receiver has NOT yet checked in for their local calendar day.
4. Verify daily rate limit (max 1 nudge per sender/receiver/habit/receiver local calendar day).
5. Insert nudge record into `nudges` table.
6. Dispatch push notification to receiver's registered FCM tokens via Firebase HTTP v1 API.

## 8. Data Model Rule

Never duplicate the same business entity into Firebase Firestore.

Supabase PostgreSQL remains the single source of truth.

