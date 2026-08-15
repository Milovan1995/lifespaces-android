package com.lifespaces.android.ui

import com.lifespaces.android.data.Item
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompletionFilterTest {
    @Test
    fun filtersCompletedAndRemainingItems() {
        val done = Item(text = "Done", completed = true)
        val remaining = Item(text = "Remaining", completed = false)
        val plain = Item(text = "Plain")

        assertTrue(done.matches(CompletionFilter.ALL))
        assertTrue(done.matches(CompletionFilter.DONE))
        assertFalse(done.matches(CompletionFilter.REMAINING))
        assertTrue(remaining.matches(CompletionFilter.REMAINING))
        assertTrue(plain.matches(CompletionFilter.REMAINING))
        assertFalse(plain.matches(CompletionFilter.DONE))
    }
}
