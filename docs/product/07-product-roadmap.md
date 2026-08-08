# LifeSpaces product roadmap

## Objective

Evolve LifeSpaces from a fast offline capture tool into a dependable personal
organiser without weakening its simplest flow: save an item immediately and
organise it later.

The next work should improve finding, reviewing, and capturing existing data
before adding more complex automation. Each phase should be independently
useful, tested on the physical Samsung phone, and committed separately.

## Current foundation

The application already provides:

- quick text capture into the permanent Nesortirano Inbox;
- direct item capture inside a space;
- expandable spaces with item counts and colour-coded item cards;
- configurable space capabilities for completion, dates, and location;
- Shopping and General templates as editable starting configurations;
- item editing, moving, deletion, completion, all-day dates, and date sorting;
- export of a dated item through the system calendar application;
- light and dark themes;
- an Android home-screen widget for quick capture into Nesortirano or one
  selected space;
- offline Room storage with repository and database tests.

This foundation should remain usable without an account, internet connection,
or external service.

## Assumptions and open decisions

- Search and Today/Upcoming are read-only views over existing item data and do
  not require a database schema change.
- Android Share should initially accept plain text only. Links already arrive
  as text and do not need a separate content model.
- Recurrence and reminders do require persistent data and therefore a Room
  migration. Their exact item model must be approved before implementation.
- Reminder delivery uses Android notifications and exact alarms, as documented
  in the technical direction. It must clearly explain missing permissions and
  must not silently fall back to an imprecise alarm.
- Backup/export comes before import/restore. The export format must be versioned
  before users can depend on it.
- A larger list widget is optional. The existing small quick-capture widget
  remains the default because it solves the primary capture job with less home
  screen space.

## Proposed plan

### Phase 1: Global search

Add one search entry point in the app bar and filter the already observed
items in memory by their text and space name.

Required behaviour:

- search across Nesortirano and every user space;
- show the matching item text and its space context;
- open the existing item action sheet when a result is selected;
- treat matching as case-insensitive;
- show a clear empty state when no item matches;
- leave the normal home layout unchanged when search is closed.

Keep the first version deliberately small: no search history, tags, ranking,
fuzzy matching, or database full-text index. Add database-backed search only
if real data volume makes in-memory filtering measurably slow.

### Phase 2: Today and Upcoming

Add a compact time-based overview for dated items. This is not yet a full
calendar screen.

Required behaviour:

- Today contains all items scheduled for the current local date;
- Upcoming contains future dated items ordered chronologically;
- items from Nesortirano remain visible alongside items from spaces;
- space name and colour provide context without changing the item itself;
- selecting an item opens the same item action sheet used elsewhere;
- completed items remain visible but visually neutral.

The date boundary must use the phone's current timezone and update correctly
after a date or timezone change. A separate month calendar should only follow
after this simpler overview proves insufficient.

### Phase 3: Android Share to LifeSpaces

Register LifeSpaces as a target for Android's standard text sharing flow so a
link or selected text can be captured from another application.

Required behaviour:

- accept `ACTION_SEND` with `text/plain`;
- display the received text for confirmation before saving;
- default the destination to Nesortirano;
- optionally let the user choose an existing space;
- never save duplicate data merely because the Activity is recreated;
- preserve the normal direct-capture flow when the app opens normally.

Do not add browser extensions, URL previews, metadata fetching, or background
network access in this phase.

### Phase 4: Recurring items

Introduce a small recurrence model only after the first three phases are
stable. Recurrence describes when an item repeats; it does not itself deliver
a notification.

Initial supported rules:

- consecutive days;
- one selected weekday every week;
- one selected day of the month.

Before implementation, decide whether completing an occurrence advances the
same item or creates a new occurrence. That choice affects history, sorting,
and the database model and must not be hidden inside the UI implementation.

Required engineering work:

- document the recurrence rule and lifecycle;
- add a versioned Room migration and migration test;
- expose recurrence only for spaces that enable the relevant capability;
- show the next occurrence in the item details;
- handle invalid monthly dates explicitly, such as the 31st in February.

More complex RRULE editing, arbitrary intervals, and end conditions remain
outside the first recurrence version.

### Phase 5: Reminders and notifications

Allow any dated item to have one or more user-selected reminder times. This
phase depends on a settled dated-item and recurrence model.

Required behaviour:

- request notification permission only when the user enables a reminder;
- request exact-alarm access only when it is needed;
- schedule reminders with AlarmManager and restore them after reboot;
- read current repository data before displaying a notification so edited or
  deleted reminders cannot fire with stale content;
- reschedule the next occurrence after a repeating reminder fires;
- allow dismissal but do not add snooze in the first version;
- clearly show when a reminder is saved but cannot be scheduled because a
  required permission is missing.

Validation must include app restart, device reboot, permission denial, changed
time/timezone, edited reminders, and deleted items. This is the highest-risk
phase because incorrect scheduling can make the organiser unreliable.

### Phase 6: Backup and export

Give users a portable copy of their local data before considering cloud sync.

First version:

- export all spaces, capabilities, colours, items, dates, completion state,
  recurrence rules, and reminders into one versioned file;
- use Android's Storage Access Framework so the user chooses the destination;
- avoid storage permissions and avoid writing to an implicit folder;
- document which app version and export schema created the file;
- verify that export does not modify the local database.

Import/restore should be a separate phase because conflict handling and
partial failure can cause data loss. It begins only after the export format is
stable and has fixture-based tests.

### Phase 7: Larger space-list widget

Add an optional 4x2 widget only if the small quick-capture widget is useful but
users also need home-screen visibility of current items.

Possible first scope:

- show one selected space or Nesortirano;
- display a short list of open items;
- retain a prominent quick-add action;
- open the app when an item is selected;
- show a useful empty state;
- update after item, space, or completion changes.

The widget should use standard RemoteViews and existing app data. Interactive
checkboxes, scrolling collections, multiple layouts, and visual configuration
belong to later iterations only if the static list proves useful.

## Delivery order

1. Global search.
2. Today and Upcoming.
3. Android Share to LifeSpaces.
4. Recurring items.
5. Reminders and notifications.
6. Backup/export.
7. Optional larger widget.

The order deliberately places low-risk views and native Android entry points
before schema migrations and background scheduling. Each phase gets its own
implementation, physical-device review, commit, and push.

## Validation

For each implementation phase:

1. Review the relevant product and architecture decisions before coding.
2. Implement the smallest useful version without adding a library unless the
   Android platform and existing dependencies cannot cover the need.
3. Run unit tests and the debug build sequentially with one Gradle worker to
   stay within the development machine's memory limit.
4. Install the new APK on the Samsung SM-A336B and exercise the complete flow.
5. Confirm light and dark theme readability and verify existing quick capture,
   space expansion, item actions, and widget capture still work.
6. Commit and push only after physical-device feedback is accepted.

Schema-changing phases additionally require an explicit Room migration test.
Reminder work additionally requires the device-state checks listed in Phase 5.

## Non-goals

This roadmap does not include:

- cloud synchronisation or mandatory user accounts;
- shared spaces or multi-user collaboration;
- voice input or AI classification;
- a generic arbitrary-field or plugin system;
- a web or desktop client;
- automatic Google Calendar synchronisation;
- a full month calendar before Today/Upcoming is validated.

## First step

Implement global search as a UI-only feature over the existing `HomeFeed`
items. Start by adding a search action to the app bar, then reuse the existing
item card and item action sheet for results. This gives immediate value without
a schema migration, new permission, service, or dependency.
