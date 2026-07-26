# MVP build plan

## Objective

Deliver a phone-first Android app that captures one text item in seconds,
stores it offline in Nesortirano, and lets the user organise it into spaces.

## Milestones

1. **Project setup**
   - Create one Kotlin Android app with Compose, Room, `minSdk` 26, and
     `compileSdk`/`targetSdk` 36.
   - Establish a small design system using Material 3.

2. **Local data and core actions**
   - Implement Space, Item, capability, and Reminder storage.
   - Add repository operations for creating, editing, moving, and permanently
     deleting items and spaces.
   - Test destructive rules: deleting a space moves its items to Nesortirano;
     deleting an item is confirmed and permanent.

3. **Capture and organisation**
   - Make Brzi unos the opening screen.
   - Save every new text entry as a separate item in Nesortirano.
   - Support creating a space, moving an item, and editing item details.

4. **Shopping template**
   - Provide the Shopping template with optional location and item completion.
   - Keep bought items crossed out until manually deleted.
   - Support manual drag-and-drop ordering and characteristic-based sorting.

5. **Calendar and reminders**
   - Show every dated item in the global calendar, including unsorted items.
   - Support all-day items and explicit time from 00:00:00 to 23:59:59.
   - Request notification and exact-alarm access before scheduling a precise
     reminder. Do not schedule a fallback when access is denied.

6. **Device validation**
   - Test the capture-to-space flow on a Samsung phone.
   - Test reminders after app restart, reboot, permission denial, and time
     changes.
   - Test database migrations before releasing any schema change.

## Non-goals

No account, cloud sync, backup, voice input, AI classification, tablet
optimisation, or Samsung SDK integration in the MVP.

## First implementation step

Choose the minimum Android version, then generate the one-module Kotlin
project and implement the Room-backed data model before building screens.
