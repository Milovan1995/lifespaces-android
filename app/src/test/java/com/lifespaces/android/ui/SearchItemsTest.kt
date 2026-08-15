package com.lifespaces.android.ui

import com.lifespaces.android.data.Item
import com.lifespaces.android.data.Space
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchItemsTest {
    @Test
    fun findsItemTextAndSpaceNameWithoutCaseSensitivity() {
        val spaces = listOf(Space(id = 1, name = "Voli", template = "Shopping"))
        val milk = Item(id = 1, text = "Mlijeko", spaceId = 1)
        val doctor = Item(id = 2, text = "Doktor", spaceId = null)

        assertEquals(listOf(milk), searchItems(listOf(milk, doctor), spaces, "MLIJEKO"))
        assertEquals(listOf(milk), searchItems(listOf(milk, doctor), spaces, "voli"))
        assertEquals(emptyList<Item>(), searchItems(listOf(milk, doctor), spaces, "nema"))
    }

    @Test
    fun opensTheMatchingSpaceOrInboxAtItsVisibleHeader() {
        val market = Space(id = 7, name = "Pijaca", template = "General")

        assertEquals(7L to 5, searchResultTarget(Item(id = 1, text = "Cvijeće", spaceId = 7), listOf(market)))
        assertEquals(null to 2, searchResultTarget(Item(id = 2, text = "Bilješka"), listOf(market)))
    }
}
