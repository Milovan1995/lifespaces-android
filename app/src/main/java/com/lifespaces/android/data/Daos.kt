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

    @Query("DELETE FROM spaces WHERE id = :id")
    suspend fun deleteById(id: Long)

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

    @Query("UPDATE items SET spaceId = :spaceId, updatedAt = :updatedAt WHERE id = :itemId")
    suspend fun updateSpace(itemId: Long, spaceId: Long?, updatedAt: Long)

    @Query("UPDATE items SET spaceId = NULL, updatedAt = :updatedAt WHERE spaceId = :spaceId")
    suspend fun clearSpace(spaceId: Long, updatedAt: Long)

    @Query("UPDATE items SET text = :text, updatedAt = :updatedAt WHERE id = :itemId")
    suspend fun updateText(itemId: Long, text: String, updatedAt: Long)

    @Query("UPDATE items SET completed = :completed, updatedAt = :updatedAt WHERE id = :itemId")
    suspend fun updateCompleted(itemId: Long, completed: Boolean, updatedAt: Long)

    @Query("UPDATE items SET scheduledAt = :scheduledAt, updatedAt = :updatedAt WHERE id = :itemId")
    suspend fun updateScheduledAt(itemId: Long, scheduledAt: Long?, updatedAt: Long)

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
