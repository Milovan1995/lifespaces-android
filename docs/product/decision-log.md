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
| 2026-07-26 | Make space icons and colours optional after-creation customisation. | Space creation should require only the minimum context; appearance can be refined later. |
| 2026-07-26 | Let users add space capabilities during creation or later. | Users can start quickly while retaining the ability to shape a space as its needs become clear. |
| 2026-07-26 | Delete associated data when a capability is removed, with confirmation. | The product will not retain historical capability data in its first version; irreversible deletion must be clear and deliberate. |
| 2026-07-26 | Move items to Nesortirano when their space is deleted. | Deleting a container must not discard its items; users can reassign or edit them afterwards. |
| 2026-07-26 | Require confirmation before deleting a space. | The action changes the user's organisation even though items are preserved. |
| 2026-07-26 | Permanently delete an individual item after confirmation. | An archive or trash stage is not needed in the initial product; deletion must be explicit. |
| 2026-07-26 | Keep completed Shopping items visible and crossed out. | Completion communicates status without hiding information; the user deletes an item when it is no longer useful. |
| 2026-07-26 | Allow manual drag-and-drop order within a space. | Users need a simple way to arrange active items without adding sorting rules or priorities. |
| 2026-07-26 | Let users sort items by their characteristics. | Item-level data such as date should support useful ordering in addition to manual ordering. |
| 2026-08-15 | Defer in-app speech-to-text until a practical strictly offline engine is validated. | The tiny Whisper experiment on the Galaxy A33 was too slow and inaccurate for quick capture; a dictated note must not silently send personal audio to a remote recognition service. |
| 2026-08-15 | Store voice notes as private AAC/M4A attachments to items. | This provides offline voice capture without transcription; each note has an optional label and captured date/time, is limited to five minutes, and all notes together to 100 MiB, with no automatic deletion. |
