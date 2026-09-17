package org.catholiccompanion.app.data.liturgy

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class LiturgyDao {
    @Query("SELECT * FROM calendar_scopes ORDER BY displayName")
    abstract fun observeScopes(): Flow<List<CalendarScopeEntity>>

    @Query("SELECT * FROM calendar_scopes WHERE id = :calendarId LIMIT 1")
    abstract suspend fun getScope(calendarId: String): CalendarScopeEntity?

    @Query("SELECT * FROM liturgical_days WHERE calendarId = :calendarId AND date = :date LIMIT 1")
    abstract fun observeDay(calendarId: String, date: String): Flow<LiturgicalDayEntity?>

    @Query(
        "SELECT * FROM celebration_options WHERE calendarId = :calendarId AND date = :date " +
            "ORDER BY isPrimary DESC, title ASC",
    )
    abstract fun observeCelebrations(calendarId: String, date: String): Flow<List<CelebrationOptionEntity>>

    @Query("SELECT * FROM reading_references WHERE celebrationId = :celebrationId ORDER BY orderIndex")
    abstract fun observeReadings(celebrationId: String): Flow<List<ReadingReferenceEntity>>

    /**
     * Celebrations across a date range, used by the celebrations browser. Dates are stored as
     * ISO-8601 text, so a lexicographic BETWEEN is also a chronological comparison.
     */
    @Query(
        "SELECT * FROM celebration_options WHERE calendarId = :calendarId " +
            "AND date BETWEEN :startDate AND :endDate " +
            "ORDER BY date ASC, isPrimary DESC, title ASC",
    )
    abstract fun observeCelebrationsInRange(
        calendarId: String,
        startDate: String,
        endDate: String,
    ): Flow<List<CelebrationOptionEntity>>

    @Query(
        "SELECT * FROM liturgical_days WHERE calendarId = :calendarId " +
            "AND date BETWEEN :startDate AND :endDate ORDER BY date ASC",
    )
    abstract fun observeDaysInRange(
        calendarId: String,
        startDate: String,
        endDate: String,
    ): Flow<List<LiturgicalDayEntity>>

    @Query("DELETE FROM calendar_scopes WHERE id = :calendarId")
    protected abstract suspend fun deleteScope(calendarId: String)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertScope(scope: CalendarScopeEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertDays(days: List<LiturgicalDayEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertCelebrations(celebrations: List<CelebrationOptionEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertReadings(readings: List<ReadingReferenceEntity>)

    @Transaction
    open suspend fun replaceBundle(bundle: CalendarBundle) {
        deleteScope(bundle.scope.id)
        insertScope(bundle.scope)
        insertDays(bundle.days)
        insertCelebrations(bundle.celebrations)
        insertReadings(bundle.readings)
    }
}
