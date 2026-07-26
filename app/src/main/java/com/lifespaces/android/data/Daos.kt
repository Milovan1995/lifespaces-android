package com.lifespaces.android.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SpaceDao {
    @Query("SELECT * FROM spaces ORDER BY createdAt DESC")
    fun observeSpaces(): Flow<List<Space>>

    @Insert
    suspend fun insert(space: Space): Long

    @Update
    suspend fun update(space: Space)

    @Delete
    suspend fun delete(space: Space)

    @Query("SELECT * FROM spaces WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Space?
}

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY sortOrder ASC, createdAt DESC")
    fun observeItems(): Flow<List<Item>>

    @Insert
    suspend fun insert(item: Item): Long

    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Item?
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY scheduledAt ASC")
    fun observeReminders(): Flow<List<Reminder>>

    @Insert
    suspend fun insert(reminder: Reminder): Long

    @Delete
    suspend fun delete(reminder: Reminder)
}
