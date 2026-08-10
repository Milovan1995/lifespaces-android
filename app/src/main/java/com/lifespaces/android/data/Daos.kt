package com.lifespaces.android.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SpaceDao {
    @Query("SELECT * FROM spaces ORDER BY createdAt DESC")
    fun observeSpaces(): Flow<List<Space>>

    @Query("SELECT * FROM space_capabilities")
    fun observeCapabilities(): Flow<List<SpaceCapability>>

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

    @Insert
    suspend fun insertCapabilities(capabilities: List<SpaceCapability>)

    @Query("DELETE FROM space_capabilities WHERE spaceId = :spaceId")
    suspend fun deleteCapabilities(spaceId: Long)

    @Query("UPDATE spaces SET name = :name, location = :location, color = :color WHERE id = :spaceId")
    suspend fun updateDetails(spaceId: Long, name: String, location: String?, color: Long?)

    @Query("UPDATE items SET completed = NULL, updatedAt = :updatedAt WHERE spaceId = :spaceId")
    suspend fun clearCompleted(spaceId: Long, updatedAt: Long)

    @Query("UPDATE items SET scheduledAt = NULL, updatedAt = :updatedAt WHERE spaceId = :spaceId")
    suspend fun clearScheduledAt(spaceId: Long, updatedAt: Long)

    @Query("UPDATE items SET spaceId = NULL, updatedAt = :updatedAt WHERE spaceId = :spaceId")
    suspend fun clearItemsSpace(spaceId: Long, updatedAt: Long)

    @Transaction
    suspend fun create(space: Space, capabilities: Set<String>): Long {
        val spaceId = insert(space)
        insertCapabilities(capabilities.map { SpaceCapability(spaceId = spaceId, capability = it) })
        return spaceId
    }

    @Transaction
    suspend fun updateWithCapabilities(
        spaceId: Long,
        name: String,
        location: String?,
        color: Long?,
        capabilities: Set<String>,
        clearCompleted: Boolean,
        clearScheduledAt: Boolean,
        updatedAt: Long,
    ) {
        updateDetails(spaceId, name, location, color)
        deleteCapabilities(spaceId)
        insertCapabilities(capabilities.map { SpaceCapability(spaceId = spaceId, capability = it) })
        if (clearCompleted) clearCompleted(spaceId, updatedAt)
        if (clearScheduledAt) clearScheduledAt(spaceId, updatedAt)
    }

    @Transaction
    suspend fun deleteWithCapabilities(spaceId: Long, updatedAt: Long) {
        clearItemsSpace(spaceId, updatedAt)
        deleteCapabilities(spaceId)
        deleteById(spaceId)
    }
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
interface ShiftDao {
    @Insert
    suspend fun insertType(shiftType: ShiftType): Long

    @Insert
    suspend fun insertOverride(override: ShiftWeekdayOverride)

    @Insert
    suspend fun insertDay(day: ShiftDay): Long

    @Query("SELECT * FROM shift_days WHERE id = :id LIMIT 1")
    suspend fun getDayById(id: Long): ShiftDay?
}

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY localDate ASC, minuteOfDay ASC, id ASC")
    fun observeAlarms(): Flow<List<Alarm>>

    @Insert
    suspend fun insert(alarm: Alarm): Long

    @Delete
    suspend fun delete(alarm: Alarm)
}
