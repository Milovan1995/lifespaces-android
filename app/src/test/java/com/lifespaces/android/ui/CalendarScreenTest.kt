package com.lifespaces.android.ui

import com.lifespaces.android.data.Item
import com.lifespaces.android.data.ShiftDay
import com.lifespaces.android.data.ShiftType
import com.lifespaces.android.data.ShiftWeekdayOverride
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarScreenTest {
    @Test
    fun mondayToSundayHandlesYearBoundary() {
        val week = calendarWeekDays(LocalDate.of(2027, 1, 1))

        assertEquals(LocalDate.of(2026, 12, 28), week.first())
        assertEquals(LocalDate.of(2027, 1, 3), week.last())
    }

    @Test
    fun movesWholeWeeksFromMonday() {
        val weekStart = LocalDate.of(2026, 12, 28)

        assertEquals(LocalDate.of(2027, 1, 4), moveCalendarWeek(weekStart, 1))
        assertEquals(LocalDate.of(2026, 12, 21), moveCalendarWeek(weekStart, -1))
    }

    @Test
    fun groupsDatedItemsByLocalDateAndLeavesUndatedOut() {
        val first = Item(id = 1, text = "Prva", scheduledAt = LocalDate.of(2026, 8, 10).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        val second = Item(id = 2, text = "Druga", scheduledAt = LocalDate.of(2026, 8, 10).atTime(18, 0).toInstant(ZoneOffset.UTC).toEpochMilli())
        val grouped = datedItemsByDate(listOf(first, Item(id = 3, text = "Bez datuma"), second), ZoneOffset.UTC)

        assertEquals(listOf(first, second), grouped[LocalDate.of(2026, 8, 10)])
        assertEquals(1, grouped.size)
    }

    @Test
    fun suggestsConfiguredShiftAlarmIncludingMondayOverride() {
        val daily = ShiftType(1, "Dnevna", 0, 570, 1050, 450)
        val monday = LocalDate.of(2026, 8, 10)
        val tuesday = monday.plusDays(1)
        val overrides = listOf(ShiftWeekdayOverride(1, 1, alarmMinute = 420))

        assertEquals(
            420,
            shiftAlarmSuggestion(monday, listOf(daily), overrides, listOf(ShiftDay(localDate = monday.toString(), shiftTypeId = 1)))?.minute,
        )
        assertEquals(
            450,
            shiftAlarmSuggestion(tuesday, listOf(daily), overrides, listOf(ShiftDay(localDate = tuesday.toString(), shiftTypeId = 1)))?.minute,
        )
    }
}
