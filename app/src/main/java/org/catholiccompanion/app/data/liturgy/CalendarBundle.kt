package org.catholiccompanion.app.data.liturgy

import java.time.LocalDate

data class CalendarBundle(
    val scope: CalendarScopeEntity,
    val days: List<LiturgicalDayEntity>,
    val celebrations: List<CelebrationOptionEntity>,
    val readings: List<ReadingReferenceEntity>,
)

class InvalidCalendarBundleException(
    val problems: List<String>,
) : IllegalArgumentException(problems.joinToString(separator = "\n"))

object CalendarBundleValidator {
    fun validate(bundle: CalendarBundle): List<String> = buildList {
        val coverageStart = parseDate("coverageStart", bundle.scope.coverageStart, this)
        val coverageEnd = parseDate("coverageEnd", bundle.scope.coverageEnd, this)
        if (coverageStart != null && coverageEnd != null && coverageStart > coverageEnd) {
            add("coverageStart must not be after coverageEnd")
        }
        if (bundle.scope.sourceName.isBlank()) add("sourceName is required")
        if (!bundle.scope.sourceUrl.startsWith("https://")) add("sourceUrl must be an HTTPS URL")
        if (bundle.scope.permissionStatus.isBlank()) add("permissionStatus is required")
        if (bundle.scope.contentVersion.isBlank()) add("contentVersion is required")
        if (bundle.scope.reviewedAt.isBlank()) add("reviewedAt is required")

        val duplicateDays = bundle.days.groupingBy { it.calendarId to it.date }
            .eachCount().filterValues { it > 1 }.keys
        if (duplicateDays.isNotEmpty()) add("duplicate liturgical days: $duplicateDays")

        bundle.days.forEach { day ->
            if (day.calendarId != bundle.scope.id) {
                add("day ${day.date} belongs to a different calendar")
            }
            val date = parseDate("day date", day.date, this)
            if (date != null && coverageStart != null && coverageEnd != null && date !in coverageStart..coverageEnd) {
                add("day ${day.date} is outside declared coverage")
            }
            val options = bundle.celebrations.filter {
                it.calendarId == day.calendarId && it.date == day.date
            }
            if (options.count { it.isPrimary } != 1) {
                add("day ${day.date} must have exactly one primary celebration")
            }
        }

        val dayKeys = bundle.days.map { it.calendarId to it.date }.toSet()
        val celebrationIds = bundle.celebrations.map { it.id }
        if (celebrationIds.size != celebrationIds.distinct().size) add("celebration ids must be unique")
        bundle.celebrations.forEach { celebration ->
            if ((celebration.calendarId to celebration.date) !in dayKeys) {
                add("celebration ${celebration.id} does not belong to a bundled day")
            }
        }

        val celebrationIdSet = celebrationIds.toSet()
        val readingIds = bundle.readings.map { it.id }
        if (readingIds.size != readingIds.distinct().size) add("reading ids must be unique")
        bundle.readings.forEach { reading ->
            if (reading.celebrationId !in celebrationIdSet) {
                add("reading ${reading.id} does not belong to a bundled celebration")
            }
            if (reading.orderIndex < 0) add("reading ${reading.id} has a negative orderIndex")
            if (!reading.sourceUrl.startsWith("https://")) {
                add("reading ${reading.id} sourceUrl must be an HTTPS URL")
            }
        }
        val duplicateReadingPositions = bundle.readings
            .groupingBy { it.celebrationId to it.orderIndex }
            .eachCount().filterValues { it > 1 }.keys
        if (duplicateReadingPositions.isNotEmpty()) {
            add("reading orderIndex values must be unique within a celebration")
        }
    }

    private fun parseDate(
        label: String,
        value: String,
        problems: MutableList<String>,
    ): LocalDate? = runCatching { LocalDate.parse(value) }.getOrElse {
        problems.add("$label must use ISO format YYYY-MM-DD: $value")
        null
    }
}
