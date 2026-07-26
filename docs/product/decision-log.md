# Product decision log

| Date | Decision | Reasoning |
| --- | --- | --- |
| 2026-07-25 | Make “capture now, organise later” the primary product job. | The user's main need is preserving a thought immediately; forcing a space or structure at capture time adds friction and risks loss. |
| 2026-07-25 | Do not require voice input in the first usable version. | Fast text capture can validate the core behaviour sooner and keeps voice recognition from defining the initial scope. |
| 2026-07-25 | Validate shopping as the first concrete use case. | Shopping items are the user's most frequent self-messages and give the Inbox a clear, repeatable outcome. |
| 2026-07-25 | Send all new captures to a neutral Inbox first. | A capture may resemble a shopping item but belong elsewhere. Manual sorting avoids incorrect assumptions and keeps saving friction-free. |
| 2026-07-25 | Allow Inbox items to join an existing space, create a new space, or remain independent. | Some items belong to an ongoing context such as a shop, while one-off notes and calendar items should not require an artificial group. |
| 2026-07-25 | Use “prostor” as the local user-facing term for a grouping. | It describes a flexible context without implying that all items must be a list or folder. “Space” is the English translation. |
| 2026-07-25 | Use space templates for preselected fields and modules. | Templates make common cases useful quickly while leaving each space customisable. A custom space can start with selected information types. |
| 2026-07-25 | Keep templates small in the first usable version. | A general arbitrary-fields system would delay validation of capture and Inbox sorting. |
| 2026-07-25 | Fully support Shopping as the first space template. | It is the most frequent real-world capture case and offers a narrow way to validate the Inbox-to-space flow. |
| 2026-07-25 | Require manual separation of shopping entries. | Each manually entered item remains explicit; the MVP avoids unreliable parsing of commas or free text. |
| 2026-07-25 | Open the app to Brzi unos and save one item per `+` action. | The home flow must optimise immediate capture and make every saved item independently sortable. |
| 2026-07-25 | Keep new entries in the permanent Nesortirano Inbox. | It gives neutral, visible storage for captures until the user adds detail or chooses a destination. |
| 2026-07-25 | Allow optional details during capture and unrestricted later editing. | Users can add context when they have it, without making it a requirement; moving or changing an item must remain possible. |
| 2026-07-25 | Remove an item from Nesortirano only after it is assigned to a space. | A date adds scheduling, not ownership. Dated independent entries remain visible for later organisation. |
| 2026-07-25 | Show every dated item in the global calendar. | The calendar is based on time, not membership. Unsorted items remain useful even without a space label. |
| 2026-07-25 | Treat a date without time as an all-day calendar item. | Users can record a date without inventing a precise time. |
| 2026-07-25 | Let every dated item have an optional reminder notification. | A reminder is useful across notes, events, and space-specific items, but should not block simple calendar use. |
| 2026-07-25 | Do not assign a default reminder time. | A reminder time is user intent; the time control starts at 00:00:00 when the user chooses to enter one. |
| 2026-07-25 | Treat 00:00:00 as midnight when a time is selected. | No-time and midnight must remain distinct: only absent time makes an item all-day. |
| 2026-07-25 | Make reminders optional, separately scheduled, and unlimited in count. | A dated item can have no reminder; if enabled, it has at least one user-selected notification date and time, independent of the item's scheduled time. |
| 2026-07-25 | Limit reminder repetition to consecutive days, a weekday, or a day of the month. | These cover the stated needs without creating a complex recurrence-rule builder. |
| 2026-07-25 | Let repeating reminders run until manually changed or disabled. | This matches familiar alarm behaviour and avoids requiring an artificial end condition. |
| 2026-07-25 | Do not include snooze for reminders. | Dismissal is sufficient for the first product flow; recurring reminders continue at their next scheduled time. |
| 2026-07-25 | Model done/not-done as a space capability. | Completion is useful in some spaces, such as Shopping, but should not force every item to behave like a task. |
| 2026-07-25 | Exclude structured quantities and prices from the initial Shopping template. | Plain text covers the need without turning a simple list into an inventory or budgeting feature. |
| 2026-07-25 | Add one optional location field to a Shopping space. | The space name identifies the shopping context; location adds useful detail without a complex store model. |
