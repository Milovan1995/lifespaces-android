package com.lifespaces.android.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LifeSpacesRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: LifeSpacesRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = LifeSpacesRepository(db.spaceDao(), db.itemDao(), db.shiftDao(), db.alarmDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun createSpaceAndItem_updatesFlows() = runTest {
        val spaceId = repository.createSpace("Home", location = "Kitchen")
        val itemId = repository.createItem("Milk")
        val secondItemId = repository.createItem("Bread")

        assertEquals(1, repository.spaces.first().size)
        assertEquals("Kitchen", repository.spaces.first().single().location)
        assertEquals(2, repository.items.first().size)

        repository.moveItem(itemId, spaceId)
        repository.moveItem(secondItemId, spaceId)
        assertEquals(setOf(spaceId, spaceId), repository.items.first().map { it.spaceId }.toSet())

        repository.deleteSpace(spaceId)
        assertEquals(setOf(null), repository.items.first().map { it.spaceId }.toSet())
    }

    @Test
    fun deleteItem_removesIt() = runTest {
        val itemId = repository.createItem("Temporary")

        repository.deleteItem(itemId)

        assertEquals(0, repository.items.first().size)
    }

    @Test
    fun completion_isStoredForAnItem() = runTest {
        val itemId = repository.createItem("Milk")

        repository.setItemCompleted(itemId, true)

        assertEquals(true, repository.items.first().single().completed)
    }

    @Test
    fun updateText_trimsWhitespaceAndKeepsExistingTextForBlankInput() = runTest {
        val itemId = repository.createItem("Original")

        repository.updateItemText(itemId, "  Updated  ")
        assertEquals("Updated", repository.items.first().single().text)

        repository.updateItemText(itemId, "   ")
        assertEquals("Updated", repository.items.first().single().text)
    }

    @Test
    fun scheduledDate_canBeSetAndCleared() = runTest {
        val itemId = repository.createItem("Birthday")

        repository.setItemScheduledAt(itemId, 1_700_000_000_000)
        assertEquals(1_700_000_000_000, repository.items.first().single().scheduledAt)

        repository.setItemScheduledAt(itemId, null)
        assertEquals(null, repository.items.first().single().scheduledAt)
    }

    @Test
    fun homeFeed_derivesDefaultsForLegacySpaces() = runTest {
        val shoppingId = db.spaceDao().insert(Space(name = "Shop", template = "Shopping"))
        val generalId = db.spaceDao().insert(Space(name = "Home", template = "General", location = "Kitchen"))
        val linksId = db.spaceDao().insert(Space(name = "Reading", template = "Links"))

        val capabilities = repository.homeFeed.first().capabilities

        assertEquals(SpaceCapabilities.shopping, capabilities[shoppingId])
        assertEquals(SpaceCapabilities.general + SpaceCapabilities.LOCATION, capabilities[generalId])
        assertEquals(SpaceCapabilities.links, capabilities[linksId])
    }

    @Test
    fun configuredCapabilities_arePersistedAndAlwaysIncludeText() = runTest {
        val spaceId = repository.createSpace(
            name = "Home",
            template = "General",
            capabilities = setOf(SpaceCapabilities.LOCATION),
            location = " Kitchen ",
        )

        repository.updateSpace(spaceId, " House ", null, null, setOf(SpaceCapabilities.COMPLETION))

        val feed = repository.homeFeed.first()
        assertEquals("House", feed.spaces.single().name)
        assertEquals(setOf(SpaceCapabilities.TEXT, SpaceCapabilities.COMPLETION), feed.capabilities[spaceId])
    }

    @Test
    fun disablingCapabilities_canClearTheirItemData() = runTest {
        val spaceId = repository.createSpace("Shop", template = "Shopping")
        val itemId = repository.createItem("Milk", spaceId)
        repository.setItemCompleted(itemId, true)
        repository.setItemScheduledAt(itemId, 1_700_000_000_000)

        repository.updateSpace(
            spaceId = spaceId,
            name = "Shop",
            location = null,
            color = null,
            capabilities = setOf(SpaceCapabilities.TEXT),
            clearCompleted = true,
            clearScheduledAt = true,
        )

        val item = repository.items.first().single()
        assertNull(item.completed)
        assertNull(item.scheduledAt)
    }

    @Test
    fun spaceColor_canBeChangedAndCleared() = runTest {
        val spaceId = repository.createSpace("Home", template = "General")

        repository.updateSpace(spaceId, "Home", null, 123L, SpaceCapabilities.general)
        assertEquals(123L, repository.spaces.first().single().color)

        repository.updateSpace(spaceId, "Home", null, null, SpaceCapabilities.general)
        assertNull(repository.spaces.first().single().color)
    }

    @Test
    fun alarm_requiresExactlyOneExistingOwner() = runTest {
        val itemId = repository.createItem("Appointment")
        val dayId = db.shiftDao().insertDay(ShiftDay(localDate = "2026-08-11"))

        repository.insertAlarm(Alarm(itemId = itemId, localDate = "2026-08-11", minuteOfDay = 480))
        repository.insertAlarm(Alarm(shiftDayId = dayId, localDate = "2026-08-11", minuteOfDay = 540))

        assertEquals(2, repository.alarms.first().size)
        assertTrue(
            runCatching {
                repository.insertAlarm(Alarm(localDate = "2026-08-11", minuteOfDay = 600))
            }.exceptionOrNull() is IllegalArgumentException,
        )
        assertTrue(
            runCatching {
                repository.insertAlarm(
                    Alarm(itemId = itemId, shiftDayId = dayId, localDate = "2026-08-11", minuteOfDay = 600),
                )
            }.exceptionOrNull() is IllegalArgumentException,
        )
        assertTrue(
            runCatching {
                repository.insertAlarm(Alarm(itemId = 999, localDate = "2026-08-11", minuteOfDay = 600))
            }.exceptionOrNull() is IllegalArgumentException,
        )
    }

    @Test
    fun defaultShiftTypesAreSeededOnceWithMondayDailyOverride() = runTest {
        repository.ensureDefaultShiftTypes()
        repository.ensureDefaultShiftTypes()

        val calendar = repository.calendarFeed.first()
        val daily = calendar.shiftTypes.single { it.name == "Dnevna" }
        assertEquals(3, calendar.shiftTypes.size)
        assertEquals(1, calendar.overrides.size)
        assertEquals(1, calendar.overrides.single().weekday)
        assertEquals(daily.id, calendar.overrides.single().shiftTypeId)
        assertTrue(daily.color ushr 32 != 0L)
        assertEquals(12 * 60, calendar.shiftTypes.single { it.name == "Jutarnja" }.defaultEndMinute)
    }

    @Test
    fun shiftDayCanBeSavedUpdatedAndCleared() = runTest {
        repository.ensureDefaultShiftTypes()
        val dailyId = repository.calendarFeed.first().shiftTypes.single { it.name == "Dnevna" }.id

        repository.saveShiftDay("2026-08-10", dailyId, "  Smjena u kancelariji  ")
        repository.saveShiftDay("2026-08-10", null, "  Slobodan dan  ")
        val day = repository.calendarFeed.first().shiftDays.single()
        assertNull(day.shiftTypeId)
        assertEquals("Slobodan dan", day.note)

        repository.clearShiftDay("2026-08-10")
        assertTrue(repository.calendarFeed.first().shiftDays.isEmpty())
    }

    @Test
    fun updatingExistingWeekdayOverridesReplacesThem() = runTest {
        repository.ensureDefaultShiftTypes()
        val daily = repository.calendarFeed.first().shiftTypes.single { it.name == "Dnevna" }

        repository.updateShiftType(
            daily,
            listOf(
                ShiftWeekdayOverride(daily.id, weekday = 1, startMinute = 8 * 60, endMinute = 16 * 60, alarmMinute = 6 * 60),
                ShiftWeekdayOverride(daily.id, weekday = 2, startMinute = 10 * 60, endMinute = 18 * 60, alarmMinute = 8 * 60),
            ),
        )

        val saved = repository.calendarFeed.first().overrides.filter { it.shiftTypeId == daily.id }
        assertEquals(2, saved.size)
        assertEquals(8 * 60, saved.single { it.weekday == 1 }.startMinute)
    }
}
