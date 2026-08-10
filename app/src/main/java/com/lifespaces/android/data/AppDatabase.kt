package com.lifespaces.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.Instant
import java.time.ZoneId

@Database(
    entities = [
        Space::class,
        SpaceCapability::class,
        Item::class,
        ShiftType::class,
        ShiftWeekdayOverride::class,
        ShiftDay::class,
        Alarm::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun spaceDao(): SpaceDao
    abstract fun itemDao(): ItemDao
    abstract fun shiftDao(): ShiftDao
    abstract fun alarmDao(): AlarmDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "lifespaces.db")
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}

internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `shift_types` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `color` INTEGER NOT NULL, `defaultStartMinute` INTEGER NOT NULL, `defaultEndMinute` INTEGER NOT NULL, `defaultAlarmMinute` INTEGER NOT NULL)""",
        )
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `shift_weekday_overrides` (`shiftTypeId` INTEGER NOT NULL, `weekday` INTEGER NOT NULL, `startMinute` INTEGER, `endMinute` INTEGER, `alarmMinute` INTEGER, PRIMARY KEY(`shiftTypeId`, `weekday`), FOREIGN KEY(`shiftTypeId`) REFERENCES `shift_types`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)""",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_shift_weekday_overrides_shiftTypeId` ON `shift_weekday_overrides` (`shiftTypeId`)",
        )
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `shift_days` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `localDate` TEXT NOT NULL, `shiftTypeId` INTEGER, `note` TEXT, FOREIGN KEY(`shiftTypeId`) REFERENCES `shift_types`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION)""",
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_shift_days_localDate` ON `shift_days` (`localDate`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_shift_days_shiftTypeId` ON `shift_days` (`shiftTypeId`)")
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `alarms` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `itemId` INTEGER, `shiftDayId` INTEGER, `localDate` TEXT NOT NULL, `minuteOfDay` INTEGER NOT NULL, `description` TEXT, `snoozeMinutes` INTEGER NOT NULL, `completed` INTEGER NOT NULL)""",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_alarms_itemId` ON `alarms` (`itemId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_alarms_shiftDayId` ON `alarms` (`shiftDayId`)")

        db.query("SELECT id, itemId, scheduledAt, enabled FROM reminders").use { cursor ->
            while (cursor.moveToNext()) {
                val localTime = Instant.ofEpochMilli(cursor.getLong(2)).atZone(ZoneId.systemDefault())
                db.execSQL(
                    """INSERT INTO alarms (id, itemId, shiftDayId, localDate, minuteOfDay, description, snoozeMinutes, completed) VALUES (?, ?, NULL, ?, ?, NULL, 10, ?)""",
                    arrayOf<Any?>(
                        cursor.getLong(0),
                        cursor.getLong(1),
                        localTime.toLocalDate().toString(),
                        localTime.hour * 60 + localTime.minute,
                        if (cursor.getInt(3) == 0) 1 else 0,
                    ),
                )
            }
        }
        db.execSQL("DROP TABLE reminders")
    }
}
