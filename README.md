# LifeSpaces

LifeSpaces is an Android-first, voice-first modular personal organiser.

The application is designed for quickly capturing notes, tasks, reminders,
events, shopping items, maintenance records, repairs, ideas, and other everyday
information.

Users organise their information into custom spaces such as:

- Work
- Home
- Car
- Shopping
- Gym
- Travel
- Events
- Maintenance
- Repairs
- Personal projects

Each space can use a different set of modules and behaviours while all
time-based items can also be displayed in a unified calendar.

## Project status

The Android MVP is under active development. The current build supports text
capture into Nesortirano, custom spaces with optional locations, moving and
editing items, Shopping completion, all-day dates, basic date sorting, and
destructive-action confirmations. Reminders, drag-and-drop ordering, and
additional templates are still in progress.

## Main product goals

- Capture information in a few seconds
- Support voice and text input
- Work offline
- Allow custom spaces and reusable templates
- Combine reminders and events in a unified calendar
- Remain useful without an account or cloud service
- Give users ownership of their data
- Provide a clean and practical Android experience

## Planned capabilities

- Custom spaces
- Space templates
- Notes
- Tasks and checklists
- Reminders and alarms
- Calendar events
- Unified calendar
- Shopping lists
- Maintenance history
- Repair tracking
- Voice-to-text input
- Inbox for unorganised captures
- Global search
- Archive and cleanup
- Data export and backup

## Documentation

Product documentation is stored under:

```text
docs/product/
```

Technical architecture documentation will be stored under:

```text
docs/architecture/
```

## Technology

The first version will be a native Android application built with Kotlin,
Jetpack Compose, Material 3, Room, Coroutines and Flow. It is offline-first;
cloud synchronisation, voice input, and AI classification are later decisions.

Architecture decisions are documented in:

```text
docs/architecture/
```

## Author

Milovan Antić

## Licence

This project is licensed under the MIT License.
