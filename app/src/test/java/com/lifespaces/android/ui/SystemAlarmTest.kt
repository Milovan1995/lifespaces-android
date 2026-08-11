package com.lifespaces.android.ui

import android.provider.AlarmClock
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SystemAlarmTest {
    @Test
    fun alarmWindowEndsOneMinuteBeforeSameTimeTomorrow() {
        val now = ZonedDateTime.of(2026, 8, 9, 13, 0, 0, 0, ZoneId.of("Europe/Podgorica"))

        assertTrue(isSystemAlarmTimeAllowed(LocalDate.of(2026, 8, 10), 12 * 60 + 59, now))
        assertFalse(isSystemAlarmTimeAllowed(LocalDate.of(2026, 8, 10), 13 * 60, now))
        assertFalse(isSystemAlarmTimeAllowed(LocalDate.of(2026, 8, 9), 12 * 60 + 59, now))
    }

    @Test
    fun systemClockIntentContainsTimeAndVisibleDate() {
        val intent = createSystemAlarmIntent(LocalDate.of(2026, 8, 12), 7 * 60 + 15, "Doktor")

        assertEquals(AlarmClock.ACTION_SET_ALARM, intent.action)
        assertEquals(7, intent.getIntExtra(AlarmClock.EXTRA_HOUR, -1))
        assertEquals(15, intent.getIntExtra(AlarmClock.EXTRA_MINUTES, -1))
        assertEquals("Doktor · 12.08.2026.", intent.getStringExtra(AlarmClock.EXTRA_MESSAGE))
        assertTrue(intent.getBooleanExtra(AlarmClock.EXTRA_SKIP_UI, false))
    }
}
