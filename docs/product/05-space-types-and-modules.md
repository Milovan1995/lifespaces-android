# Space types and modules

## Model

A **space** is a user-created context, for example Voli, Auto, or Festival.
A **space template** supplies its initial fields and enabled modules. It does
not restrict later changes.

## Space capabilities

Capabilities define what items in a space can do. They are not separate item
types. For example, a **completion** capability gives items a done/not-done
state. A template enables useful capabilities by default, while a space may be
customised later. Users may select capabilities while creating a custom space
or add them afterwards.

Each capability provides its own visible controls and behaviour. For example,
the **calendar** capability provides date-related controls and calendar
visibility, while **completion** provides a done/not-done checkbox.

Capabilities can be removed. Removing one permanently deletes its associated
data after explicit user confirmation. The initial product keeps no change
history or restoration log.

Calendar visibility is global: any item with a date appears in the calendar,
whether or not its space has other capabilities.

| Template | Useful starting information |
| --- | --- |
| Shopping | Completion state, text notes, optional location |
| Maintenance | Relevant dates, notes, optional cost information |
| Event | Date and time, location, notes |
| General | Notes and optional date information |

## Custom spaces

Users can create a custom space and choose the information it needs, such as
notes, dates, prices, or email addresses.

An icon and colour are optional visual details and can be added or changed
after a space is created.

## Scope guard

The first usable version should fully support the Shopping template only. It
uses a small fixed set of useful fields. Maintenance and Event are later
templates. A fully configurable system of arbitrary fields, rules, and views
is valuable later, but is too broad to validate the core capture-and-sort flow.

Shopping entries are manually separated. The first version does not infer
multiple products from commas or free text.

The first usable version has no structured quantity or price fields. Users
write quantities and prices in the item text when needed.

A Shopping space may have one optional location field. Its name remains the
primary context, for example a space named “Voli”.

Purchased items remain visible as crossed out until the user deletes them.

Items inside a space can be manually reordered with drag and drop.

Items can also be sorted by their available characteristics, such as date.
