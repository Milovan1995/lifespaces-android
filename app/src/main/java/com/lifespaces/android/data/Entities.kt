package com.lifespaces.android.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "spaces")
data class Space(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val template: String,
    val location: String? = null,
    val icon: String? = null,
    val color: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "space_capabilities")
data class SpaceCapability(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val spaceId: Long,
    val capability: String,
)

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val spaceId: Long? = null,
    val text: String,
    val scheduledAt: Long? = null,
    val hasScheduledTime: Boolean = false,
    val completed: Boolean? = null,
    val sortOrder: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "voice_notes",
    foreignKeys = [
        ForeignKey(
            entity = Item::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["itemId"], unique = true)],
)
data class VoiceNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,
    val filePath: String,
    val durationMs: Long,
    val byteSize: Long,
)

@Entity(tableName = "shift_types")
data class ShiftType(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Long,
    val defaultStartMinute: Int,
    val defaultEndMinute: Int,
    val defaultAlarmMinute: Int,
)

@Entity(
    tableName = "shift_weekday_overrides",
    primaryKeys = ["shiftTypeId", "weekday"],
    foreignKeys = [
        ForeignKey(
            entity = ShiftType::class,
            parentColumns = ["id"],
            childColumns = ["shiftTypeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("shiftTypeId")],
)
data class ShiftWeekdayOverride(
    val shiftTypeId: Long,
    val weekday: Int,
    val startMinute: Int? = null,
    val endMinute: Int? = null,
    val alarmMinute: Int? = null,
)

@Entity(
    tableName = "shift_days",
    foreignKeys = [
        ForeignKey(
            entity = ShiftType::class,
            parentColumns = ["id"],
            childColumns = ["shiftTypeId"],
        ),
    ],
    indices = [Index(value = ["localDate"], unique = true), Index("shiftTypeId")],
)
data class ShiftDay(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val localDate: String,
    val shiftTypeId: Long? = null,
    val note: String? = null,
)

@Entity(
    tableName = "alarms",
    indices = [Index("itemId"), Index("shiftDayId")],
)
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long? = null,
    val shiftDayId: Long? = null,
    val localDate: String,
    val minuteOfDay: Int,
    val description: String? = null,
    val snoozeMinutes: Int = 10,
    val completed: Boolean = false,
)
