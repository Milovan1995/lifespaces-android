package com.lifespaces.android

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ShareIntentTest {
    @Test
    fun acceptsOnlyNonBlankPlainTextShares() {
        assertEquals(
            "https://example.com",
            sharedTextFrom(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, " https://example.com ")),
        )
        assertNull(sharedTextFrom(Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_TEXT, "photo")))
        assertNull(sharedTextFrom(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, "   ")))
    }
}
