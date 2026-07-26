package com.lifespaces.android

import android.app.Application
import com.lifespaces.android.data.AppDatabase
import com.lifespaces.android.data.LifeSpacesRepository

class LifeSpacesApplication : Application() {
    val database by lazy { AppDatabase.create(this) }
    val repository by lazy { LifeSpacesRepository(database.spaceDao(), database.itemDao(), database.reminderDao()) }
}
