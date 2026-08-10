# Links, My Calendar, shifts, and alarms plan

## Objective

Add three connected capabilities without weakening LifeSpaces' fast,
offline-first capture flow:

1. save and open web links as ordinary LifeSpaces items;
2. provide a top-level **Moj kalendar** view that combines dated items and a
   personal shift schedule;
3. provide real, exact wake-up alarms for items, shifts, and days off.

Implementation will happen in a later conversation. This document records the
agreed product behaviour, technical boundaries, delivery order, and validation
gates.

## Agreed product decisions

### One calendar for the first version

The first version has one local **Moj kalendar** per app installation. It is a
top-level view opened from a calendar icon beside the theme icon. It is not a
space and it is not an item.

Support for multiple named calendars may be added later. The first schema and
UI must not include profiles, sharing, or calendar switching before that need
is validated.

### Weekly calendar first

The first calendar UI shows one Monday-to-Sunday week at a time:

- arrows move to the previous or next week;
- a `Danas` action returns to the current week;
- today uses a visual treatment distinct from every shift colour;
- every day of the week follows the same rules, including weekends;
- the first phone prototype uses seven readable day rows rather than seven
  narrow columns;
- tapping a day opens its shift/day-off, note, and alarm actions.

This layout is deliberately testable. A seven-column grid, swipe navigation,
or an expandable month view may replace or complement it after physical-phone
feedback.

### Calendar contents

For the selected day, **Moj kalendar** shows:

- its assigned shift or explicit day-off state;
- its optional note;
- all dated items from Nesortirano;
- all dated items from every space, with space name and colour;
- active alarms and their descriptions;
- an alarm indicator beside a shift, day off, or item that has active alarms.

Selecting an existing item reuses the current item action sheet. Existing
system-calendar export remains an explicit user action for a dated item.
LifeSpaces does not add two-way Google Calendar sync in this phase.

### Shift/day states

Each calendar day has exactly one schedule state:

- `Nije uneseno`;
- a configured shift type, initially Jutarnja, Dnevna, or Noćna;
- `Slobodan dan`.

`Nije uneseno` means that the schedule is unknown and has no note or alarm
actions. `Slobodan dan` is an intentional state and may have a note and any
number of alarms. There is no minimum or maximum number of working days or
days off, and weekends have no special restriction.

Schedule entry is one day at a time in the first version: tap a day, choose one
large colour-coded option, and save immediately. Bulk selection and copying a
week are deferred until single-day entry is tested.

### Configurable shifts

Shift definitions are configurable so the calendar is useful to different
people. A shift definition contains:

- name;
- colour;
- default start time;
- default end time;
- default wake-up alarm time;
- optional per-weekday overrides for those times.

Initial example definitions are:

| Shift | Work time | Default alarm |
| --- | --- | --- |
| Jutarnja | 05:00–13:00 | 03:55 |
| Dnevna on Monday | 09:00–17:00 | 07:00 |
| Dnevna on other days | 09:30–17:30 | 07:30 |
| Noćna | 13:00–22:00 | 09:00 |

The user selects `Dnevna`; the calendar applies its Monday override
automatically. New or edited defaults affect future suggestions only. They
never silently modify an alarm that the user already confirmed.

### Alarm ownership and editing

Items, assigned shifts, and days off can each have multiple alarms. Every
alarm has:

- an exact local date and time;
- an optional description;
- a snooze duration of 5 or 10 minutes, defaulting to 10;
- active or completed state.

If no description is entered, the containing screen displays `Alarm 1`,
`Alarm 2`, and so on, ordered by trigger time and stable ID. An alarm is a
child record, not a new LifeSpaces item.

Every alarm can be edited or removed individually. Changing a shift, shift
definition, day state, item date, or other source data does not silently edit
an existing alarm. If its time no longer matches the source's current default,
the UI shows `Alarm više ne odgovara odabranoj smjeni` and offers explicit
edit or remove actions.

### Alarm defaults for ordinary items

When adding an alarm to an item:

- if the item has a future date, that date is preselected and the user chooses
  or accepts a time;
- if the item date is in the past, the alarm editor asks for a new future date
  and time without automatically changing the item date;
- if the item has no date, choosing only a time selects today when that time
  is still in the future, otherwise tomorrow;
- an optional expanded date control always allows another future date;
- before saving, the UI displays the final full date and time.

An undated item does not become dated merely because it has an alarm. It does
not appear as a dated item in **Moj kalendar** unless the user separately adds
an item date.

### Alarm defaults for shifts and days off

An assigned shift exposes `Dodaj alarm`. The editor preselects the exact
calendar date of that shift and the configured default alarm time for that
shift and weekday. The user can change the proposed date, time, description,
and snooze duration before confirming.

A day marked `Slobodan dan` also exposes `Dodaj alarm`, but has no shift-based
default time. The user chooses its time. An unentered day cannot have an alarm.

If a proposed alarm time is already in the past, LifeSpaces does not schedule
an incorrect next weekday occurrence. It opens the full date/time editor and
requires a future result.

### Ringing behaviour

These are real wake-up alarms, not ordinary notification sounds:

- the device wakes at the exact stored date and time;
- a full-screen alarm appears over the lock screen when Android permits it;
- the alarm keeps ringing until the user chooses an action;
- `Odgodi 5 min` or `Odgodi 10 min` uses the duration selected when the alarm
  was created;
- `Zaustavi` completes the alarm;
- completed alarms disappear from the main active-alarm lists;
- shift/day state and notes remain as calendar history;
- an alarm-history screen is deferred.

LifeSpaces uses the device's current default alarm sound and alarm audio
channel. This follows the system alarm tone and alarm volume exposed by
Android. Samsung Clock's private per-alarm ringtone, gradual-volume, vibration,
and other internal settings cannot be copied. LifeSpaces enables vibration;
an alarm-tone picker can be added later through Android's system picker.

## Proposed implementation plan

### Phase 1: Links and notes

Implement the smallest useful link flow without a database migration:

1. Add a `LINKS` space capability and a `Links` template whose defaults are
   text plus links.
2. Add a `Linkovi` checkbox to space creation and editing.
3. Change direct-capture wording in a link-enabled space to
   `Novi link ili bilješka`.
4. Keep URL, optional per-item label or description, or note content in the
   existing `Item.text` field. The optional label or description precedes the
   URL on its own line.
5. Recognise a safe HTTP or HTTPS URL in a bare item or its last line and expose
   `Otvori link` and `Kopiraj link` in its existing item action sheet.
6. Allow opening a valid link in Nesortirano before it is moved to a space.
7. Open links through Android `ACTION_VIEW`, handling a missing browser with a
   clear message.

Removing the Links capability does not delete item text and needs no
destructive confirmation.

Deferred link work: page previews, favicons, network metadata, separate title
and URL fields, tags, and Android Share-to-LifeSpaces.

### Phase 2: Data model and migration design

Before calendar or alarm UI, define Room version 2 and a real migration from
version 1. Remove `fallbackToDestructiveMigration()` from the production
database builder when the migration is installed.

Minimum new data:

- `ShiftType`: name, colour, default work times, and default alarm time;
- `ShiftWeekdayOverride`: shift type, weekday, and overridden times;
- `ShiftDay`: local calendar date, selected shift or day-off state, and note;
- expanded alarm records that can target either an item or a shift/day record,
  store local intended date/time, description, snooze duration, and state.

Use one shared alarm scheduling path even though alarms may have different
owners. The repository must validate that every alarm belongs to exactly one
supported owner. Do not build a generic calendar-event or arbitrary-field
engine.

Store the user's intended local date and minute-of-day, then derive the Android
trigger instant in the current timezone. This lets timezone reconciliation
preserve an intended `07:00` wall-clock alarm instead of silently turning it
into another local hour.

The migration must preserve every existing space, capability, item, date,
completion value, colour, location, and existing reminder row.

### Phase 3: Read-only My Calendar foundation

1. Add a local calendar vector icon beside the theme icon.
2. Add an in-app top-level state for Home and **Moj kalendar**, with normal
   Android back behaviour.
3. Render the current Monday-to-Sunday week with previous/next arrows and
   `Danas`.
4. Highlight today independently from shift colours.
5. Show all existing dated items under the selected day, ordered by time/date
   and then existing item order.
6. Display each item's space name and colour, or `Nesortirano`.
7. Reuse the item action sheet on selection.

This phase validates navigation and date boundaries before shift editing or
alarm permissions are introduced.

### Phase 4: Shift configuration and schedule entry

1. Seed the initial example shifts only when no shift types exist.
2. Add calendar settings for editing shift name, colour, start/end time,
   default alarm time, and optional weekday overrides.
3. Add the day sheet with Jutarnja, Dnevna, Noćna, Slobodan dan, and Poništi.
4. Save one state per local calendar date.
5. Allow one optional note for an assigned shift or day off.
6. Show work time, note, and state colour in the selected-day detail.
7. Preserve past assignments and notes when navigating backwards.

Do not add multiple calendars, recurring shift patterns, bulk assignment,
hours/pay calculations, or sharing in this phase.

### Phase 5: Alarm persistence and editor

1. Add an alarm list to item and calendar-day details.
2. Implement multiple alarms per supported owner.
3. Implement the agreed default-date rules for future-dated, past-dated, and
   undated items.
4. Implement shift-date and shift-default-time suggestions.
5. Require an explicit future date/time before saving.
6. Add optional description and 5/10-minute snooze selection.
7. Generate display-only `Alarm N` labels for blank descriptions.
8. Show mismatch warnings without mutating existing alarms.
9. Keep completed alarms out of active lists.

This phase can initially persist alarms without scheduling them, allowing the
editor and database rules to be tested independently from Android permissions.

### Phase 6: Exact alarm delivery

Use native Android APIs and no new alarm library:

1. Request notification permission when the first alarm is enabled.
2. Explain and request Android's Alarms & reminders special access when exact
   scheduling is first required.
3. Schedule user-confirmed alarms with `AlarmManager.setAlarmClock()` and a
   unique immutable `PendingIntent`.
4. Create a high-importance alarm notification channel using
   `AudioAttributes.USAGE_ALARM` and the system default alarm URI.
5. Add a receiver that reloads the current alarm and its owner from Room before
   ringing, so removed or stale alarms cannot fire.
6. Add the full-screen ringing activity with lock-screen visibility,
   description, `Odgodi`, and `Zaustavi`.
7. Reschedule snooze for now plus the selected 5 or 10 minutes.
8. Mark the alarm completed only on `Zaustavi`.
9. Reconcile future active alarms after reboot, app update, time change,
   timezone change, and exact-alarm permission grant.
10. Never substitute an inexact alarm when exact-alarm access is unavailable;
    keep the alarm visibly unscheduled and explain how to enable it.

Android's `ACTION_SET_ALARM` is not the primary implementation because it can
set a time and repeating weekdays but cannot reliably target an arbitrary
calendar date several weeks ahead. LifeSpaces therefore owns the exact dated
alarm and its edit/delete lifecycle.

### Phase 7: Integration polish

After both calendar and alarm delivery work:

- show active-alarm indicators in the weekly calendar;
- show alarm descriptions in selected-day details;
- ensure a shift/day change produces a mismatch warning but no automatic alarm
  edit;
- keep existing system-calendar export available from dated item actions;
- test whether a seven-column or expandable month view is more useful than the
  initial seven-row week without shipping both prematurely.

## Risks and safeguards

### Database migration

Risk: a faulty Room migration could erase the user's offline organiser data.

Safeguard: export the version 1 schema, implement an explicit 1-to-2 migration,
remove destructive fallback, and test migration with populated version 1 data
before installing it on the physical phone.

### Exact-alarm permissions

Risk: Android 12+ special access is denied by default on many fresh installs,
and Android 13+ also controls notification permission.

Safeguard: show alarm state as scheduled or permission-blocked, check
`canScheduleExactAlarms()` before every schedule operation, and reschedule only
after confirmed access. Never claim an alarm is active when the OS cannot
deliver it.

### Samsung battery and lock-screen behaviour

Risk: an alarm can work while the app is open but fail under Doze, after
reboot, or on the lock screen.

Safeguard: use a real alarm-clock exact alarm, reboot reconciliation, a
full-screen time-sensitive notification, and mandatory physical-device tests
with the phone locked and the app closed.

### Stale alarms

Risk: edited or deleted data may leave an old PendingIntent in the system.

Safeguard: use stable unique alarm identities, explicitly cancel when editing
or removing, and make the receiver re-read Room before producing sound.

### Date and timezone errors

Risk: using today's weekday or only an absolute timestamp can schedule an
alarm on the wrong date or wrong local hour.

Safeguard: test far-future dates, use explicit local dates from items or shift
days, validate future time, and reconcile the stored local date/time after a
timezone change.

### Full-screen alarm policy

Risk: full-screen intents are intrusive and platform-restricted.

Safeguard: use them only for alarms explicitly created by the user, declare the
required permission, and provide a normal high-priority notification fallback
only for the presentation when Android keeps an unlocked device in the current
screen. Do not use full-screen behaviour for ordinary app messages.

## Validation

### Links

- paste and save HTTPS, HTTP, `www`, and ordinary note text;
- reject unsafe or unsupported schemes as openable web links;
- open a link from Nesortirano and a Links-enabled space;
- copy only the URL from a bare or described link item;
- confirm ordinary notes have no misleading open action;
- verify missing-browser handling.

### Calendar and shifts

- verify Monday-to-Sunday boundaries across month and year changes;
- navigate backward/forward and return to today;
- verify today's highlight in light and dark themes;
- assign every state on weekdays and weekends;
- distinguish Slobodan dan from Nije uneseno;
- change and clear assignments and notes;
- verify Monday and other-day overrides;
- edit shift definitions without changing existing alarms;
- confirm all dated Inbox and space items appear on the correct local date.

### Alarm rules

- future-dated item defaults to its exact date;
- past-dated item requires a new future alarm date;
- undated item chooses today or tomorrow from the selected time;
- shift alarm defaults to the shift day's exact date and weekday-specific time;
- day-off alarm allows manual time;
- multiple alarms display stable descriptions or Alarm N fallbacks;
- changing source data leaves alarms unchanged and shows mismatch;
- edit and remove affect only the chosen alarm.

### Physical Samsung alarm delivery

- grant and deny notification permission;
- grant, revoke, and re-grant exact-alarm access;
- ring with the app closed and phone locked;
- verify system alarm sound, alarm volume, and vibration;
- snooze for both 5 and 10 minutes;
- stop and confirm the alarm leaves active lists;
- reboot and confirm future alarms are restored;
- change time and timezone and verify intended local date/time;
- delete an item/day/alarm and confirm no stale sound occurs;
- test Doze/battery-saving behaviour;
- document Android's force-stop limitation if the OS suppresses alarms until
  the user manually opens the app again.

All Gradle work must be sequential because of the development machine's memory
limit:

1. `testDebugUnitTest` with `--no-daemon --max-workers=1`;
2. after it fully exits, `assembleDebug` with the same limits;
3. install the APK through ADB;
4. perform the focused physical-phone checks for that phase.

## Non-goals for the first version

- multiple named calendars or people on one installation;
- cloud sync or shared schedules;
- automatic two-way Google or Samsung Calendar sync;
- bulk shift assignment or copying whole weeks;
- recurring shift-pattern generation;
- pay, overtime, or hour reports;
- alarm history screen;
- importing private Samsung Clock alarm settings;
- per-alarm ringtone selection or gradual volume;
- link previews, metadata, or automatic page-title fetching;
- generic arbitrary calendar-event or form-builder architecture.

## First step in the next implementation conversation

Implement Phase 1, Links and notes, because it needs no migration, permission,
background component, or new dependency. Validate and install it independently.
Only after accepting that phone flow should work begin the version 2 migration
for **Moj kalendar**, shifts, and alarms.

## Official Android references

- [Calendar intents](https://developer.android.com/identity/providers/calendar-provider)
- [AlarmClock intent contract](https://developer.android.com/reference/android/provider/AlarmClock.html)
- [Schedule exact alarms](https://developer.android.com/develop/background-work/services/alarms)
- [AlarmManager API](https://developer.android.com/reference/android/app/AlarmManager.html)
- [Full-screen time-sensitive notifications](https://developer.android.com/develop/ui/compose/notifications/create-notification)
- [RingtoneManager default alarm sound](https://developer.android.com/reference/android/media/RingtoneManager)
- [Alarm audio attributes](https://developer.android.com/reference/android/media/AudioAttributes)
