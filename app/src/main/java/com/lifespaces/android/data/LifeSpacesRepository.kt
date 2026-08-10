package com.lifespaces.android.data

import androidx.compose.ui.graphics.Color
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
    private val shiftDao: ShiftDao,
    private val alarmDao: AlarmDao,
) {
    val spaces: Flow<List<Space>> = spaceDao.observeSpaces()
    val items: Flow<List<Item>> = itemDao.observeItems()
    val alarms: Flow<List<Alarm>> = alarmDao.observeAlarms()
    val calendarFeed: Flow<CalendarFeed> = combine(
        shiftDao.observeTypes(),
        shiftDao.observeOverrides(),
        shiftDao.observeDays(),
    ) { types, overrides, days -> CalendarFeed(types, overrides, days) }

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

    suspend fun insertAlarm(alarm: Alarm): Long {
        require((alarm.itemId == null) xor (alarm.shiftDayId == null)) {
            "Alarm mora imati tačno jednog vlasnika."
        }
        val ownerExists = alarm.itemId?.let { itemDao.getById(it) != null }
            ?: (shiftDao.getDayById(requireNotNull(alarm.shiftDayId)) != null)
        require(ownerExists) { "Vlasnik alarma ne postoji." }
        return alarmDao.insert(alarm)
    }

    suspend fun ensureDefaultShiftTypes() {
        if (shiftDao.typeCount() != 0) {
            shiftDao.getTypes().forEach { type ->
                val color = if (type.color ushr 32 == 0L) Color(type.color.toInt()).value.toLong() else type.color
                val endMinute = if (type.name == "Jutarnja" && type.defaultEndMinute == 13 * 60) 12 * 60 else type.defaultEndMinute
                if (color != type.color || endMinute != type.defaultEndMinute) {
                    shiftDao.updateType(type.copy(color = color, defaultEndMinute = endMinute))
                }
            }
            return
        }
        shiftDao.insertType(
            ShiftType(name = "Jutarnja", color = Color(0xFFE67E3D).value.toLong(), defaultStartMinute = 5 * 60, defaultEndMinute = 12 * 60, defaultAlarmMinute = 3 * 60 + 55),
        )
        val dailyId = shiftDao.insertType(
            ShiftType(name = "Dnevna", color = Color(0xFF4C8DFF).value.toLong(), defaultStartMinute = 9 * 60 + 30, defaultEndMinute = 17 * 60 + 30, defaultAlarmMinute = 7 * 60 + 30),
        )
        shiftDao.insertOverride(
            ShiftWeekdayOverride(dailyId, weekday = 1, startMinute = 9 * 60, endMinute = 17 * 60, alarmMinute = 7 * 60),
        )
        shiftDao.insertType(
            ShiftType(name = "Noćna", color = Color(0xFF7C6DE8).value.toLong(), defaultStartMinute = 13 * 60, defaultEndMinute = 22 * 60, defaultAlarmMinute = 9 * 60),
        )
    }

    suspend fun updateShiftType(shiftType: ShiftType, overrides: List<ShiftWeekdayOverride>) {
        require(shiftType.name.isNotBlank()) { "Naziv smjene je obavezan." }
        shiftDao.updateType(shiftType.copy(name = shiftType.name.trim()))
        (1..7).forEach { weekday ->
            overrides.firstOrNull { it.weekday == weekday }?.let { shiftDao.insertOverride(it) }
                ?: shiftDao.deleteOverride(shiftType.id, weekday)
        }
    }

    suspend fun saveShiftDay(localDate: String, shiftTypeId: Long?, note: String?) {
        val current = shiftDao.getDayByDate(localDate)
        val cleanedNote = note?.trim()?.takeIf(String::isNotEmpty)
        val day = ShiftDay(id = current?.id ?: 0, localDate = localDate, shiftTypeId = shiftTypeId, note = cleanedNote)
        if (current == null) shiftDao.insertDay(day) else shiftDao.updateDay(day)
    }

    suspend fun clearShiftDay(localDate: String) = shiftDao.deleteDayByDate(localDate)

    private fun cleanLocation(location: String?, capabilities: Set<String>): String? =
        location?.trim()?.takeIf { SpaceCapabilities.LOCATION in capabilities && it.isNotEmpty() }
}

data class HomeFeed(
    val spaces: List<Space>,
    val items: List<Item>,
    val capabilities: Map<Long, Set<String>> = emptyMap(),
)

data class CalendarFeed(
    val shiftTypes: List<ShiftType> = emptyList(),
    val overrides: List<ShiftWeekdayOverride> = emptyList(),
    val shiftDays: List<ShiftDay> = emptyList(),
)
