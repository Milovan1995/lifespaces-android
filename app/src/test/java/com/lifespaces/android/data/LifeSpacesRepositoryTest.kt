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
        val spaceId = repository.createSpace("Home")
        val itemId = repository.createItem("Milk")
        val secondItemId = repository.createItem("Bread")

        assertEquals(1, repository.spaces.first().size)
        assertEquals(2, repository.items.first().size)

        repository.moveItem(itemId, spaceId)
        repository.moveItem(secondItemId, spaceId)
        assertEquals(setOf(spaceId, spaceId), repository.items.first().map { it.spaceId }.toSet())

        repository.deleteSpace(spaceId)
        assertEquals(setOf(null), repository.items.first().map { it.spaceId }.toSet())
    }

    @Test
    fun deleteItem_removesIt() = runTest {
        val itemId = repository.createItem("Temporary")

        repository.deleteItem(itemId)

        assertEquals(0, repository.items.first().size)
    }

    @Test
    fun completion_isStoredForAnItem() = runTest {
        val itemId = repository.createItem("Milk")

        repository.setItemCompleted(itemId, true)

        assertEquals(true, repository.items.first().single().completed)
    }
}
