package com.lifespaces.android.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class LifeSpacesRepository(
    private val spaceDao: SpaceDao,
    private val itemDao: ItemDao,
    private val reminderDao: ReminderDao,
) {
    val spaces: Flow<List<Space>> = spaceDao.observeSpaces()
    val items: Flow<List<Item>> = itemDao.observeItems()
    val reminders: Flow<List<Reminder>> = reminderDao.observeReminders()

    val homeFeed: Flow<HomeFeed> = combine(spaces, items) { spaces, items ->
        HomeFeed(spaces = spaces, items = items)
    }

    suspend fun createSpace(name: String, template: String = "General"): Long =
        spaceDao.insert(Space(name = name, template = template))

    suspend fun createItem(text: String, spaceId: Long? = null): Long =
        itemDao.insert(Item(text = text, spaceId = spaceId))
}

data class HomeFeed(
    val spaces: List<Space>,
    val items: List<Item>,
)
