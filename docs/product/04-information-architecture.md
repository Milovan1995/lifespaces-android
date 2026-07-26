# Information architecture

## Top-level views

- **Brzi unos**: defaultni početni tok za čuvanje nove stavke.
- **Nesortirano**: stavke koje još nemaju prostor.
- **Prostori**: korisnikovi konteksti i sadržaj unutar njih.
- **Kalendar**: sve stavke kojima je definisan datum.

## Calendar rule

A dated item appears in the global calendar whether it belongs to a space or
remains in Nesortirano. Items without a space have no space label or colour.

An item with a date but no time is an all-day calendar item. Any dated item
may have optional reminders. When reminders are enabled, the user configures
at least one reminder date and time and may add an unlimited number of
additional reminder dates and times.

Supported reminder repetition is deliberately limited to consecutive days,
one selected weekday (for example, every Monday), or one selected day of the
month. Like an alarm, a repeating reminder continues until the user edits or
disables it. Other recurrence patterns are outside the initial scope.

When a reminder fires, the user can dismiss its notification. Snooze is not a
product capability; a repeating reminder still fires at its next scheduled
time unless disabled.

The app does not choose a reminder time on the user's behalf. When a time is
entered, `00:00:00` is an explicit midnight value; valid values run through
`23:59:59`. An item is all-day only when no time is selected.
