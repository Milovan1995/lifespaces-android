package com.lifespaces.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Space::class, SpaceCapability::class, Item::class, Reminder::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun spaceDao(): SpaceDao
    abstract fun itemDao(): ItemDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "lifespaces.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
