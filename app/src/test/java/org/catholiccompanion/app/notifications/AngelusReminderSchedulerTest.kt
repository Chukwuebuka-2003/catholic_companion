package org.catholiccompanion.app.notifications

import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class AngelusReminderSchedulerTest {
    private val lagos = ZoneId.of("Africa/Lagos")

    @Test
    fun `future prayer time stays on the current local day`() {
        val now = ZonedDateTime.of(2026, 9, 16, 5, 45, 0, 0, lagos)

        val result = AngelusReminderScheduler.nextOccurrence(now, 6)

        assertEquals(ZonedDateTime.of(2026, 9, 16, 6, 0, 0, 0, lagos), result)
    }

    @Test
    fun `elapsed prayer time moves to the next local day`() {
        val now = ZonedDateTime.of(2026, 9, 16, 12, 0, 0, 0, lagos)

        val result = AngelusReminderScheduler.nextOccurrence(now, 12)

        assertEquals(ZonedDateTime.of(2026, 9, 17, 12, 0, 0, 0, lagos), result)
    }

    @Test
    fun `evening reminder uses the supplied device timezone`() {
        val newYork = ZoneId.of("America/New_York")
        val now = ZonedDateTime.of(2026, 3, 8, 13, 10, 0, 0, newYork)

        val result = AngelusReminderScheduler.nextOccurrence(now, 18)

        assertEquals(newYork, result.zone)
        assertEquals(18, result.hour)
        assertEquals(8, result.dayOfMonth)
    }
}
