# LifeSpaces: product vision

## Core idea

LifeSpaces is an Android-first personal organiser for capturing a thought,
task, reminder, or note in seconds, before it is lost. Every capture is safe
to leave unorganised initially and can be sorted into a relevant space later.

## Terminology

A **prostor** is an optional user-defined context for related items, such as
Prodavnica, Auto, or Vjenčanje. "Space" is its English translation.

A **šablon prostora** provides useful initial fields for a space but does not
lock its configuration.

## Primary user value

The product must be more dependable and faster for saving a thought than
sending a message to oneself. Organisation is important, but never blocks
capture.

## Initial product direction

- Capture first; classify later.
- Put incomplete or uncertain captures in an Inbox.
- Use spaces as optional context, not a requirement for saving an item.
- Keep the first usable version focused on everyday personal use.
- Make fast text capture sufficient for the first usable version; voice can
  follow after the core flow proves useful.
- Treat shopping as the first concrete use case to validate.
- Save every new capture to a neutral Inbox first. Classification is optional
  and happens manually afterwards.
- When sorting an Inbox item, let the user assign it to an existing space,
  create a space, or keep it independent as a note or calendar item.
- Open the app to Brzi unos and save each new item separately in Nesortirano.

## System views

**Nesortirano** is a permanent system Inbox at the top of the app. It holds
new neutral captures; it is not a user-created space or folder.

An item remains in Nesortirano until it belongs to a space. It may still have
a date and appear in the global calendar while it is unsorted.

Every item with a defined date appears in the global calendar. Only items in a
space show its context there.

Items without a time are shown as all-day items. A reminder notification may
be set on any dated item. It has at least one user-selected date and time and
may have any number of additional reminder times.

## Editing principle

Saving a capture is never final. Users can add details before or after saving,
then edit its content and move it between spaces at any time.

## Not decided yet

- The first item types supported by the Inbox.
- The smallest set of actions needed to organise an Inbox item.
