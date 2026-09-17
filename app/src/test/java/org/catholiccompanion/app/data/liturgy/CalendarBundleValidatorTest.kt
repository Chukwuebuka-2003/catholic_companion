package org.catholiccompanion.app.data.liturgy

import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarBundleValidatorTest {
    @Test
    fun `accepts a complete provenance-aware day`() {
        assertTrue(CalendarBundleValidator.validate(validBundle()).isEmpty())
    }

    @Test
    fun `rejects ambiguous and untraceable content`() {
        val original = validBundle()
        val invalid = original.copy(
            celebrations = original.celebrations + original.celebrations.first().copy(
                id = "second-primary",
                title = "Unreviewed alternative",
            ),
            readings = original.readings.map { it.copy(sourceUrl = "") },
        )

        val problems = CalendarBundleValidator.validate(invalid)

        assertTrue(problems.any { it.contains("exactly one primary") })
        assertTrue(problems.any { it.contains("sourceUrl must be an HTTPS URL") })
    }

    private fun validBundle(): CalendarBundle {
        val scope = CalendarScopeEntity(
            id = "test-calendar",
            displayName = "Test Calendar",
            rite = "Roman Rite",
            region = "Test only",
            coverageStart = "2026-01-01",
            coverageEnd = "2026-12-31",
            sourceName = "Reviewed fixture",
            sourceUrl = "https://example.test/calendar",
            permissionStatus = "test fixture only",
            contentVersion = "test-1",
            reviewedAt = "2026-01-01",
        )
        val day = LiturgicalDayEntity(
            calendarId = scope.id,
            date = "2026-09-13",
            season = "Test season",
            liturgicalColor = "Green",
            contentReleaseId = "test-release",
        )
        val celebration = CelebrationOptionEntity(
            id = "test-celebration",
            calendarId = scope.id,
            date = day.date,
            title = "Test celebration",
            rank = "Test rank",
            isPrimary = true,
        )
        val reading = ReadingReferenceEntity(
            id = "test-reading",
            celebrationId = celebration.id,
            orderIndex = 0,
            label = "Gospel",
            citation = "John 1:1",
            permittedText = null,
            sourceDocumentTitle = "Test document",
            sourceUrl = "https://example.test/reading",
        )
        return CalendarBundle(scope, listOf(day), listOf(celebration), listOf(reading))
    }
}

