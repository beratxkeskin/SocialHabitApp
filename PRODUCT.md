# Product Specification

## 1. Product Overview

This product is a social habit accountability mobile application.

The main purpose is to help users maintain habits by making their daily progress visible to one or more trusted people and enabling those people to motivate each other through lightweight social interactions such as nudges.

The product is not intended to be only a personal habit tracker. Its main value comes from shared accountability.

## 2. Core Problem

People often know what habit they want to build, but they struggle to maintain consistency because:

- Motivation decreases over time.
- There is no external accountability.
- Missing a day has little immediate social consequence.
- Traditional habit trackers are mostly private and passive.
- Friends may want to support each other but lack a structured system.

The application addresses this by making progress visible within a trusted social relationship.

## 3. Core Value Proposition

> Build habits together, not alone.

Users can:

1. Create a habit.
2. Invite one or more trusted people.
3. Check in when they complete the habit.
4. See whether other members completed it that day.
5. Send a nudge when somebody has not checked in.
6. Build shared consistency over time.

## 4. Primary Product Hypothesis

If users know that a trusted partner can see whether they completed their habit and can actively remind them, they will be more likely to maintain the habit.

The MVP must validate this hypothesis before advanced features are added.

## 5. Primary Target Users

Initial target segments:

- Friends building a habit together.
- Couples sharing goals.
- University students studying together.
- Developers doing coding challenges.
- Gym partners.
- Reading partners.

Random matchmaking is not part of the MVP.

## 6. MVP Features

The first production-ready MVP includes:

### Authentication
- Register
- Login
- Logout
- Session persistence
- Basic user profile

### Habits
- Create habit (daily, selected days, weekly target)
- Edit habit
- Archive habit
- View active habits
- View habit details

### Social
- Invite another user to a habit (via `habit_invitations`)
- Accept or reject invitation
- View active habit members
- View today's member completion status

### Check-ins
- Single-tap daily check-in
- Optional short note
- Check-in history
- Immutable check-in (no undo in MVP)
- Prevent duplicate daily check-ins for the user's local calendar day

### Nudges
- Send a nudge to a habit partner who has not checked in today
- Conservative rate limiting (max 1 nudge per sender/receiver/habit/receiver local day)
- Push notification delivery via FCM HTTP v1

### Basic Retention
- Current streak (derived dynamically from check-in history)
- Weekly completion summary
- Basic calendar/history view

## 7. Explicitly Out of Scope for MVP

Do not implement these unless this document is updated:

- AI coach
- Public social feed
- Random matchmaking
- Proof photos
- Complex achievement system
- Leaderboards
- Virtual currency
- Subscription/payment system
- Wear OS application
- iOS application
- Web application
- Advanced group administration
- Marketplace
- Public profiles

## 8. Product Principles

### Accountability Over Gamification
Social accountability is the primary mechanic. Gamification should support it, not replace it.

### Private by Default
Habit progress must only be visible to users who are members of that habit.

### Low Friction
Daily check-in should require as little interaction as possible.

### Notifications Must Be Respectful
Nudges should motivate without becoming spam.

### Progressive Complexity
Start with the smallest usable product and expand only after real usage data justifies additional features.

## 9. MVP Success Metrics

Important product metrics:

- Registration completion rate
- Invite sent rate
- Invite acceptance rate
- Percentage of users with at least one partner
- Daily check-in completion rate
- Number of nudges sent
- Nudge-to-check-in conversion rate
- Day 1 retention
- Day 7 retention
- Day 30 retention
- Average active streak
- Weekly active users

## 10. Core User Loop

1. User opens the app.
2. User sees today's habits.
3. User sees their own and their partner's current status.
4. User completes the habit and checks in.
5. Partner sees the updated status.
6. If the partner has not completed the habit, the user may send a nudge.
7. Partner receives a push notification.
8. Partner returns and completes/checks in.
9. Both users build a shared history.

This loop is the most important product experience and must remain fast and reliable.
