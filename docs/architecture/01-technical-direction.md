# Technical direction

## Decision

Build the first version as a native Android app in Kotlin. Samsung-only does
not justify a Samsung-specific codebase: standard Android APIs provide the
needed storage, notifications, alarms, and UI while keeping the app usable on
other Android devices later.

## Initial stack

| Need | Choice | Why |
| --- | --- | --- |
| Language | Kotlin | Android is Kotlin-first and Kotlin supports modern Android APIs and coroutines well. |
| UI | Jetpack Compose with Material 3 | Native, state-driven UI for the fast capture and list screens. |
| Screen state | ViewModel, coroutines, Flow | Keeps UI state separate from stored data and updates screens when data changes. |
| Local storage | Room over SQLite | The app is offline-first and stores related, structured personal data. |
| Reminders | AlarmManager plus notifications | A reminder must survive closing the app and device sleep. |

Use current stable Android and library versions when the project is created;
use `compileSdk` and `targetSdk` 36 (Android 16), and set `minSdk` 26 (Android
8). The Samsung Galaxy A33 on Android 16 runs this configuration.

## Architecture

Start with one Android app module and feature-oriented packages. Do not split
the project into Gradle modules or add a mandatory domain/use-case layer yet.

```text
Compose screen
  -> ViewModel
    -> repository
      -> Room database

Alarm receiver -> repository -> Room database -> notification
```

- **UI** renders state and sends user actions. It does not query the database
  directly.
- **ViewModel** owns screen state and calls the repository.
- **Repository** is the single entry point for reading and changing app data.
- **Room** is the local source of truth. The app works without an account or
  network connection.
- **Alarm receiver** reads the current reminder before showing a notification,
  so an edited or deleted reminder cannot fire stale data.

The domain layer remains optional. Add a small use-case class only when a rule
is genuinely reused or makes a ViewModel difficult to understand.

## First data model

Keep the first schema deliberately small:

- **Space**: name, template, optional location, optional icon and colour.
- **Space capability**: enabled characteristic for one space, such as
  completion.
- **Item**: text, optional space, optional calendar date/time, optional
  completion state, manual sort position, created and updated times.
- **Reminder**: one independently scheduled notification attached to an item;
  an item may have many reminders.

An Item without a space is Nesortirano. Assigning a space removes it from that
system Inbox. A date is item data, so a dated unsorted item remains in
Nesortirano and also appears in Calendar.

Do not build a generic arbitrary-field engine for the first version. Add
specific fields only when a supported template needs one.

## Reminder boundary

Request notification permission and Android “Alarms & reminders” access when
the user first enables a reminder. Every scheduled reminder uses an exact
alarm. If either permission is not granted, the app does not schedule a
reminder; it never substitutes an inexact notification.

Recurring reminders are stored as the limited product rules already agreed:
consecutive days, a weekday, or a day of the month. The scheduler creates the
next occurrence after a reminder fires rather than scheduling an unbounded
number of system alarms.

## Explicitly deferred

- Cloud sync, accounts, shared spaces, and web/desktop clients.
- Voice input and AI classification.
- Samsung SDK dependencies or Samsung-only UI behaviour.
- Generic form builders, plugin systems, and per-template Gradle modules.
- Backup/export implementation; define its format after the MVP.

## Sources

- [Android's Kotlin-first approach](https://developer.android.com/kotlin/first)
- [Guide to app architecture](https://developer.android.com/topic/architecture)
- [Build an offline-first app](https://developer.android.com/topic/architecture/data-layer/offline-first)
- [Save data with Room](https://developer.android.com/training/data-storage/room)
- [Schedule alarms](https://developer.android.com/develop/background-work/services/alarms)
- [Notification runtime permission](https://developer.android.com/develop/ui/compose/notifications/notification-permission)
