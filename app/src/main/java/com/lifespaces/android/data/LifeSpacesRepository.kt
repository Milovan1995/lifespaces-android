package com.lifespaces.android.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

object SpaceCapabilities {
    const val TEXT = "TEXT"
    const val COMPLETION = "COMPLETION"
    const val DATE = "DATE"
    const val LOCATION = "LOCATION"
    const val LINKS = "LINKS"

    val shopping = setOf(TEXT, COMPLETION, DATE, LOCATION)
    val general = setOf(TEXT, DATE)
    val links = setOf(TEXT, LINKS)

    fun defaults(template: String): Set<String> = when (template) {
        "Shopping" -> shopping
        "Links" -> links
        else -> general
    }
}

class LifeSpacesRepository(
    private val spaceDao: SpaceDao,
    private val itemDao: ItemDao,
    private val reminderDao: ReminderDao,
) {
    val spaces: Flow<List<Space>> = spaceDao.observeSpaces()
    val items: Flow<List<Item>> = itemDao.observeItems()
    val reminders: Flow<List<Reminder>> = reminderDao.observeReminders()

    val homeFeed: Flow<HomeFeed> = combine(spaces, items, spaceDao.observeCapabilities()) { spaces, items, rows ->
        val persisted = rows.groupBy(SpaceCapability::spaceId).mapValues { (_, values) ->
            values.mapTo(mutableSetOf(), SpaceCapability::capability)
        }
        HomeFeed(
            spaces = spaces,
            items = items,
            capabilities = spaces.associate { space ->
                space.id to (persisted[space.id] ?: SpaceCapabilities.defaults(space.template).let { defaults ->
                    if (space.location.isNullOrBlank()) defaults else defaults + SpaceCapabilities.LOCATION
                })
            },
        )
    }

    suspend fun createSpace(
        name: String,
        template: String = "General",
        location: String? = null,
    ): Long = createSpace(
        name,
        template,
        location,
        SpaceCapabilities.defaults(template).let { defaults ->
            if (location.isNullOrBlank()) defaults else defaults + SpaceCapabilities.LOCATION
        },
    )

    suspend fun createSpace(
        name: String,
        template: String,
        location: String?,
        capabilities: Set<String>,
        color: Long? = null,
    ): Long = spaceDao.create(
        Space(name = name, template = template, location = cleanLocation(location, capabilities), color = color),
        capabilities + SpaceCapabilities.TEXT,
    )

    suspend fun updateSpace(
        spaceId: Long,
        name: String,
        location: String?,
        color: Long?,
        capabilities: Set<String>,
        clearCompleted: Boolean = false,
        clearScheduledAt: Boolean = false,
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return
        val configured = capabilities + SpaceCapabilities.TEXT
        spaceDao.updateWithCapabilities(
            spaceId = spaceId,
            name = trimmedName,
            location = cleanLocation(location, configured),
            color = color,
            capabilities = configured,
            clearCompleted = clearCompleted,
            clearScheduledAt = clearScheduledAt,
            updatedAt = System.currentTimeMillis(),
        )
    }

    suspend fun createItem(text: String, spaceId: Long? = null): Long =
        itemDao.insert(Item(text = text, spaceId = spaceId))

    suspend fun updateItemText(itemId: Long, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        itemDao.updateText(itemId, trimmed, System.currentTimeMillis())
    }

    suspend fun moveItem(itemId: Long, spaceId: Long?) {
        itemDao.updateSpace(itemId, spaceId, System.currentTimeMillis())
    }

    suspend fun setItemCompleted(itemId: Long, completed: Boolean) {
        itemDao.updateCompleted(itemId, completed, System.currentTimeMillis())
    }

    suspend fun setItemScheduledAt(itemId: Long, scheduledAt: Long?) {
        itemDao.updateScheduledAt(itemId, scheduledAt, System.currentTimeMillis())
    }

    suspend fun deleteItem(itemId: Long) {
        val item = itemDao.getById(itemId) ?: return
        itemDao.delete(item)
    }

    suspend fun deleteSpace(spaceId: Long) {
        spaceDao.deleteWithCapabilities(spaceId, System.currentTimeMillis())
    }

    private fun cleanLocation(location: String?, capabilities: Set<String>): String? =
        location?.trim()?.takeIf { SpaceCapabilities.LOCATION in capabilities && it.isNotEmpty() }
}

data class HomeFeed(
    val spaces: List<Space>,
    val items: List<Item>,
    val capabilities: Map<Long, Set<String>> = emptyMap(),
)
