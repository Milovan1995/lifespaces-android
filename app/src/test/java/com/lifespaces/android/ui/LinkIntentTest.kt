package com.lifespaces.android.ui

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LinkIntentTest {
    @Test
    fun optionalLabelRoundTripsWithoutChangingTheUrl() {
        assertEquals("https://example.com", labelledItemText("", " https://example.com "))
        assertEquals("Primjer\nhttps://example.com", labelledItemText(" Primjer ", " https://example.com "))
        assertEquals("Primjer" to "https://example.com", linkItemParts("Primjer\nhttps://example.com"))
        assertEquals("" to "https://example.com", linkItemParts("https://example.com"))
    }

    @Test
    fun recognisesBareAndLabelledSafeHttpLinks() {
        assertEquals("https://example.com/path?q=1", safeWebUri("  https://example.com/path?q=1  ").toString())
        assertEquals("http://example.com", safeWebUri("http://example.com").toString())
        assertEquals("HTTPS://EXAMPLE.COM", safeWebUri("HTTPS://EXAMPLE.COM").toString())
        assertEquals("https://example.com", safeWebUri("Primjer stranice\nhttps://example.com").toString())
        assertNull(safeWebUri("www.example.com"))
        assertNull(safeWebUri("javascript:alert(1)"))
        assertNull(safeWebUri("file:///tmp/note"))
        assertNull(safeWebUri("https://example.com and a note"))
        assertNull(safeWebUri("https://example.com\nObična bilješka"))
        assertNull(safeWebUri("https:///missing-host"))
        assertNull(safeWebUri("https://user:password@example.com"))
    }

    @Test
    fun createsActionViewIntentForRecognisedLink() {
        val intent = createWebLinkIntent("Primjer stranice\nhttps://example.com")

        assertEquals(Intent.ACTION_VIEW, intent?.action)
        assertEquals("https://example.com", intent?.data.toString())
        assertNull(createWebLinkIntent("Obična bilješka"))
    }
}
