package org.catholiccompanion.app.model

import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RosaryCatalogTest {
    @Test
    fun `every mystery set produces one complete five decade sequence`() {
        MysterySet.entries.forEach { set ->
            val steps = RosaryCatalog.stepsFor(set)

            assertEquals(80, steps.size)
            assertEquals(5, steps.count { it.id.startsWith("mystery-") })
            assertEquals(53, steps.count { it.title.startsWith("Hail Mary") })
            assertEquals(6, steps.count { it.title == "Our Father" })
            assertEquals(6, steps.count { it.title == "Glory Be" })
            assertEquals(5, steps.mapNotNull { it.decade }.distinct().size)
            assertEquals(steps.size, steps.map { it.id }.distinct().size)
            assertTrue(steps.last().id == "closing-sign")
        }
    }

    @Test
    fun `weekly suggestions follow the customary pattern`() {
        assertEquals(MysterySet.JOYFUL, MysterySet.recommendedFor(DayOfWeek.MONDAY))
        assertEquals(MysterySet.SORROWFUL, MysterySet.recommendedFor(DayOfWeek.TUESDAY))
        assertEquals(MysterySet.GLORIOUS, MysterySet.recommendedFor(DayOfWeek.WEDNESDAY))
        assertEquals(MysterySet.LUMINOUS, MysterySet.recommendedFor(DayOfWeek.THURSDAY))
        assertEquals(MysterySet.SORROWFUL, MysterySet.recommendedFor(DayOfWeek.FRIDAY))
        assertEquals(MysterySet.JOYFUL, MysterySet.recommendedFor(DayOfWeek.SATURDAY))
        assertEquals(MysterySet.GLORIOUS, MysterySet.recommendedFor(DayOfWeek.SUNDAY))
    }

    @Test
    fun `all twenty mysteries include substantial formation content`() {
        val mysteries = MysterySet.entries.flatMap { it.mysteries }

        assertEquals(20, mysteries.size)
        mysteries.forEach { mystery ->
            assertTrue("Missing summary for ${mystery.title}", mystery.summary.length >= 120)
            assertTrue("Missing spiritual fruit for ${mystery.title}", mystery.fruit.length >= 10)
            assertTrue("Missing meditation for ${mystery.title}", mystery.meditation.length >= 100)
            assertTrue("Missing Scripture reference for ${mystery.title}", mystery.scriptureReference.isNotBlank())
        }
    }
}
