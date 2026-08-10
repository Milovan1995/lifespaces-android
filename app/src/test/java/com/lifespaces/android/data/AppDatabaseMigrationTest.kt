package com.lifespaces.android.data

import android.content.Context
import android.database.Cursor
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import java.time.Instant
import java.time.ZoneId
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AppDatabaseMigrationTest {
    @Test
    fun migration1To2_preservesExistingDataAndConvertsReminders() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val previousTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Podgorica"))
        context.deleteDatabase(DATABASE_NAME)
        try {
            val firstAlarmAt = 1_786_140_000_123L
            val secondAlarmAt = 1_786_226_400_000L
            val helper = createVersion1Database(context)
            try {
                helper.writableDatabase.apply {
                    execSQL(
                        """INSERT INTO spaces (id, name, template, location, icon, color, createdAt) VALUES (7, 'Home', 'General', 'Kitchen', 'home', 123, 1000)""",
                    )
                    execSQL("INSERT INTO space_capabilities (id, spaceId, capability) VALUES (8, 7, 'DATE')")
                    execSQL(
                        """INSERT INTO items (id, spaceId, text, scheduledAt, completed, sortOrder, createdAt, updatedAt) VALUES (9, 7, 'Existing item', 1700000000000, 1, 4, 2000, 3000)""",
                    )
                    execSQL("INSERT INTO reminders (id, itemId, scheduledAt, enabled) VALUES (10, 9, $firstAlarmAt, 1)")
                    execSQL("INSERT INTO reminders (id, itemId, scheduledAt, enabled) VALUES (11, 9, $secondAlarmAt, 0)")
                }
            } finally {
                helper.close()
            }

            val room = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addMigrations(MIGRATION_1_2)
                .allowMainThreadQueries()
                .build()
            try {
                    val migrated = room.openHelper.writableDatabase
                    migrated.query(
                        "SELECT id, name, template, location, icon, color, createdAt FROM spaces WHERE id = 7",
                    ).use { cursor ->
                        cursor.moveToFirst()
                        assertEquals(7L, cursor.getLong(0))
                        assertEquals("Home", cursor.getString(1))
                        assertEquals("General", cursor.getString(2))
                        assertEquals("Kitchen", cursor.getString(3))
                        assertEquals("home", cursor.getString(4))
                        assertEquals(123L, cursor.getLong(5))
                        assertEquals(1000L, cursor.getLong(6))
                    }
                    migrated.query(
                        "SELECT spaceId, text, scheduledAt, completed, sortOrder, createdAt, updatedAt FROM items WHERE id = 9",
                    ).use { cursor ->
                        cursor.moveToFirst()
                        assertEquals(7L, cursor.getLong(0))
                        assertEquals("Existing item", cursor.getString(1))
                        assertEquals(1_700_000_000_000L, cursor.getLong(2))
                        assertEquals(1, cursor.getInt(3))
                        assertEquals(4L, cursor.getLong(4))
                        assertEquals(2000L, cursor.getLong(5))
                        assertEquals(3000L, cursor.getLong(6))
                    }
                    migrated.query("SELECT spaceId, capability FROM space_capabilities WHERE id = 8").use { cursor ->
                        cursor.moveToFirst()
                        assertEquals(7L, cursor.getLong(0))
                        assertEquals("DATE", cursor.getString(1))
                    }
                    migrated.query(
                        "SELECT id, itemId, shiftDayId, localDate, minuteOfDay, description, snoozeMinutes, completed FROM alarms ORDER BY id",
                    ).use { cursor ->
                        assertEquals(2, cursor.count)
                        cursor.moveToFirst()
                        assertAlarm(cursor, 10, firstAlarmAt, completed = 0)
                        cursor.moveToNext()
                        assertAlarm(cursor, 11, secondAlarmAt, completed = 1)
                    }
                    migrated.query(
                        "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'reminders'",
                    ).use { cursor ->
                        cursor.moveToFirst()
                        assertEquals(0, cursor.getInt(0))
                    }
            } finally {
                room.close()
            }
        } finally {
            context.deleteDatabase(DATABASE_NAME)
            TimeZone.setDefault(previousTimeZone)
        }
    }

    private fun createVersion1Database(context: Context): SupportSQLiteOpenHelper =
        FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(DATABASE_NAME)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(1) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            db.execSQL(
                                """CREATE TABLE IF NOT EXISTS spaces (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, template TEXT NOT NULL, location TEXT, icon TEXT, color INTEGER, createdAt INTEGER NOT NULL)""",
                            )
                            db.execSQL(
                                """CREATE TABLE IF NOT EXISTS space_capabilities (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, spaceId INTEGER NOT NULL, capability TEXT NOT NULL)""",
                            )
                            db.execSQL(
                                """CREATE TABLE IF NOT EXISTS items (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, spaceId INTEGER, text TEXT NOT NULL, scheduledAt INTEGER, completed INTEGER, sortOrder INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)""",
                            )
                            db.execSQL(
                                """CREATE TABLE IF NOT EXISTS reminders (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, itemId INTEGER NOT NULL, scheduledAt INTEGER NOT NULL, enabled INTEGER NOT NULL)""",
                            )
                        }

                        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                    },
                ).build(),
        )

    private fun assertAlarm(cursor: Cursor, id: Long, legacyScheduledAt: Long, completed: Int) {
        val expected = Instant.ofEpochMilli(legacyScheduledAt).atZone(ZoneId.of("Europe/Podgorica"))
        assertEquals(id, cursor.getLong(0))
        assertEquals(9L, cursor.getLong(1))
        assertEquals(true, cursor.isNull(2))
        assertEquals(expected.toLocalDate().toString(), cursor.getString(3))
        assertEquals(expected.hour * 60 + expected.minute, cursor.getInt(4))
        assertEquals(true, cursor.isNull(5))
        assertEquals(10, cursor.getInt(6))
        assertEquals(completed, cursor.getInt(7))
    }

    private companion object {
        const val DATABASE_NAME = "migration-1-2"
    }
}
