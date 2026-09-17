package org.catholiccompanion.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AngelusPrayerTest {
    @Test
    fun `both languages contain complete aligned prayer text`() {
        assertEquals(12, AngelusPrayer.parts.size)
        AngelusLanguage.entries.forEach { language ->
            assertTrue(AngelusPrayer.parts.all { it.text(language).isNotBlank() })
            assertFalse(AngelusPrayer.parts.any { "..." in it.text(language) })
        }
    }

    @Test
    fun `hail mary is included in full three times`() {
        assertEquals(
            3,
            AngelusPrayer.parts.count { it.english.startsWith("Hail, Mary, full of grace") },
        )
        assertEquals(
            3,
            AngelusPrayer.parts.count { it.latin.startsWith("Ave, María, grátia plena") },
        )
    }

    @Test
    fun `closing prayer ends with amen in both languages`() {
        val closing = AngelusPrayer.parts.last()
        assertTrue(closing.english.endsWith("Amen."))
        assertTrue(closing.latin.endsWith("Amen."))
    }
}
