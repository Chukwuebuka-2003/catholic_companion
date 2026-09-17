package org.catholiccompanion.app.data.liturgy

import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

data class LiturgicalDayRecord(
    val day: LiturgicalDayEntity,
    val celebrations: List<CelebrationOptionEntity>,
    val selectedCelebration: CelebrationOptionEntity,
    val readings: List<ReadingReferenceEntity>,
)

/**
 * Liturgical ranks in precedence order. The bundled calendar stores `rank` as free text with
 * inconsistent casing ("SOLEMNITY", "Memorial", "optional memorial"), so the browser groups and
 * filters on this normalised form rather than on the raw string.
 */
enum class CelebrationRank(val label: String) {
    SOLEMNITY("Solemnity"),
    FEAST("Feast"),
    MEMORIAL("Memorial"),
    OPTIONAL_MEMORIAL("Optional memorial"),
    COMMEMORATION("Commemoration"),
    WEEKDAY("Weekday"),
    OTHER("Other");

    companion object {
        fun from(raw: String): CelebrationRank {
            val value = raw.trim().lowercase()
            return when {
                value.startsWith("solemnity") -> SOLEMNITY
                value.contains("precedence over solemnities") -> SOLEMNITY
                value.startsWith("feast") -> FEAST
                value.startsWith("optional memorial") -> OPTIONAL_MEMORIAL
                value.startsWith("memorial") -> MEMORIAL
                value.startsWith("commemoration") -> COMMEMORATION
                value.startsWith("weekday") -> WEEKDAY
                else -> OTHER
            }
        }
    }
}

/** One day in the celebrations browser: its primary celebration plus any optional alternatives. */
data class CelebrationDayRecord(
    val date: LocalDate,
    val day: LiturgicalDayEntity?,
    val primary: CelebrationOptionEntity,
    val alternatives: List<CelebrationOptionEntity>,
) {
    val rank: CelebrationRank get() = CelebrationRank.from(primary.rank)
}

@OptIn(ExperimentalCoroutinesApi::class)
class LiturgyRepository(
    private val database: LiturgyDatabase,
) {
    val scopes: Flow<List<CalendarScopeEntity>> = database.liturgyDao().observeScopes()

    fun observeDay(calendarId: String, date: LocalDate): Flow<LiturgicalDayRecord?> {
        val dao = database.liturgyDao()
        return combine(
            dao.observeDay(calendarId, date.toString()),
            dao.observeCelebrations(calendarId, date.toString()),
        ) { day, celebrations -> day to celebrations }
            .flatMapLatest { (day, celebrations) ->
                val selected = celebrations.firstOrNull { it.isPrimary }
                if (day == null || selected == null) {
                    flowOf(null)
                } else {
                    dao.observeReadings(selected.id).map { readings ->
                        LiturgicalDayRecord(day, celebrations, selected, readings)
                    }
                }
            }
    }

    /**
     * Celebrations between [start] and [end] inclusive, one record per day that has a primary
     * celebration. Days without one are skipped rather than rendered empty: the app does not
     * guess a celebration it cannot verify.
     */
    fun observeCelebrationRange(
        calendarId: String,
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<CelebrationDayRecord>> {
        val dao = database.liturgyDao()
        return combine(
            dao.observeCelebrationsInRange(calendarId, start.toString(), end.toString()),
            dao.observeDaysInRange(calendarId, start.toString(), end.toString()),
        ) { celebrations, days ->
            val daysByDate = days.associateBy { it.date }
            celebrations
                .groupBy { it.date }
                .toSortedMap()
                .mapNotNull { (date, options) ->
                    val primary = options.firstOrNull { it.isPrimary } ?: return@mapNotNull null
                    CelebrationDayRecord(
                        date = LocalDate.parse(date),
                        day = daysByDate[date],
                        primary = primary,
                        alternatives = options.filterNot { it.id == primary.id },
                    )
                }
        }
    }

    suspend fun install(bundle: CalendarBundle) {
        val problems = CalendarBundleValidator.validate(bundle)
        if (problems.isNotEmpty()) throw InvalidCalendarBundleException(problems)
        database.liturgyDao().replaceBundle(bundle)
    }

    suspend fun installIfNeeded(bundle: CalendarBundle): Boolean {
        val installed = database.liturgyDao().getScope(bundle.scope.id)
        if (installed?.contentVersion == bundle.scope.contentVersion) return false
        install(bundle)
        return true
    }
}
