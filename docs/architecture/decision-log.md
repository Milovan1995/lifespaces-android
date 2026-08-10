# Architecture decision log

| Date | Decision | Reasoning |
| --- | --- | --- |
| 2026-07-26 | Build the initial app natively in Kotlin. | Android is Kotlin-first; Samsung-only targeting does not require Samsung-specific technology. |
| 2026-07-26 | Use an offline-first local Room database as the source of truth. | Fast capture and access to personal data must work without an account or network connection. |
| 2026-07-26 | Start with one app module and UI/data layers. | The approved MVP is small; multi-module Clean Architecture would add ceremony before it solves a real problem. |
| 2026-07-26 | Use AlarmManager and notifications for user reminders. | Reminders must work when the app is closed; exact-alarm access must be requested only when the user needs it. |
| 2026-07-26 | Schedule reminders only as exact alarms. | A reminder is precise or absent: if the user denies alarm or notification permission, the app does not schedule an inexact fallback. |
| 2026-07-26 | Defer export and backup until after the MVP. | Local offline storage is sufficient to validate core capture and organisation before committing to an export format or sync strategy. |
| 2026-07-26 | Make the MVP phone-first. | The first release is for Samsung phones; Compose can support tablet layouts later without changing the data model. |
| 2026-07-26 | Set minSdk 26 and compileSdk/targetSdk 36. | Android 8 gives broad Android compatibility while the Galaxy A33 runs Android 16 (API 36), which is the current build and target baseline. |
| 2026-08-10 | Store alarm intent as an ISO local date and minute-of-day in Room v2. | Exact alarms should preserve the user's intended wall-clock time across timezone reconciliation; the explicit 1-to-2 migration converts legacy reminder timestamps without destructive fallback. |
| 2026-08-10 | Represent an unentered calendar day with no `ShiftDay` row and a day off with a row whose shift type is null. | The two states remain distinct without adding a redundant state column; a non-null shift type represents a configured work day. |
