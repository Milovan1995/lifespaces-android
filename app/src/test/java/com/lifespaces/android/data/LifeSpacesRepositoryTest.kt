package com.lifespaces.android.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LifeSpacesRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: LifeSpacesRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = LifeSpacesRepository(db.spaceDao(), db.itemDao(), db.reminderDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun createSpaceAndItem_updatesFlows() = runTest {
        repository.createSpace("Home")
        repository.createItem("Milk")

        assertEquals(1, repository.spaces.first().size)
        assertEquals(1, repository.items.first().size)
    }
}
