package com.lifespaces.android.ui

import com.lifespaces.android.data.Item
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
}
