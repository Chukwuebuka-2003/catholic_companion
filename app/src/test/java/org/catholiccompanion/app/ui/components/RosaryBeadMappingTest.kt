package org.catholiccompanion.app.ui.components

import org.catholiccompanion.app.model.MysterySet
import org.catholiccompanion.app.model.RosaryCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RosaryBeadMappingTest {
    private val steps = RosaryCatalog.stepsFor(MysterySet.JOYFUL)

    @Test
    fun `opening prayers follow crucifix and pendant beads`() {
        assertTrue(rosaryTargetFor(steps.first { it.id == "creed" }).crucifix)
        assertEquals(0, rosaryTargetFor(steps.first { it.id == "opening-our-father" }).stemIndex)
        assertEquals(3, rosaryTargetFor(steps.first { it.id == "opening-hail-mary-3" }).stemIndex)
    }

    @Test
    fun `each decade maps our father and ten hail marys to eleven ring beads`() {
        for (decade in 1..5) {
            val first = (decade - 1) * 11
            assertEquals(
                first,
                rosaryTargetFor(steps.first { it.id == "decade-$decade-our-father" }).ringIndex,
            )
            assertEquals(
                first + 10,
                rosaryTargetFor(steps.first { it.id == "decade-$decade-hail-mary-10" }).ringIndex,
            )
        }
    }

    @Test
    fun `immersive loop contains fifty five distinct points inside its drawing bounds`() {
        val points = (0 until RING_BEAD_COUNT).map { rosaryLoopPoint(it, 1_000f, 1_000f) }

        assertEquals(RING_BEAD_COUNT, points.toSet().size)
        assertTrue(points.all { it.x in 0f..1_000f })
        assertTrue(points.all { it.y in 0f..700f })
    }
}
