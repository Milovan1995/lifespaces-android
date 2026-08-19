package com.lifespaces.android.ui

import android.content.Intent
import android.provider.CalendarContract
import com.lifespaces.android.data.Item
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CalendarIntentTest {
    @Test
    fun createsPrefilledAllDayEvent() {
        val start = 1_786_140_000_000L
        val intent = createCalendarInsertIntent(
            item = Item(id = 1, text = "Kupi mlijeko", scheduledAt = start),
            location = "Voli",
        )

        assertEquals(Intent.ACTION_INSERT, intent.action)
        assertEquals(CalendarContract.Events.CONTENT_URI, intent.data)
        assertEquals("Kupi mlijeko", intent.getStringExtra(CalendarContract.Events.TITLE))
        assertEquals("Voli", intent.getStringExtra(CalendarContract.Events.EVENT_LOCATION))
        assertEquals(start, intent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, -1))
        assertTrue(intent.getBooleanExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false))
        assertTrue(intent.getLongExtra(CalendarContract.EXTRA_EVENT_END_TIME, -1) > start)
    }

    @Test
    fun createsPrefilledTimedEvent() {
        val start = 1_786_140_000_000L
        val intent = createCalendarInsertIntent(
            item = Item(id = 1, text = "Doktor", scheduledAt = start, hasScheduledTime = true),
            location = null,
        )

        assertEquals(start, intent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, -1))
        assertEquals(start + 60 * 60 * 1000L, intent.getLongExtra(CalendarContract.EXTRA_EVENT_END_TIME, -1))
        assertFalse(intent.getBooleanExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true))
    }
}
