package com.lifespaces.android.data

import androidx.room.Entity
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
    val completed: Boolean? = null,
    val sortOrder: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,
    val scheduledAt: Long,
    val enabled: Boolean = true,
)
