package org.catholiccompanion.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.catholiccompanion.app.data.liturgy.CalendarScopeEntity
import org.catholiccompanion.app.data.liturgy.CelebrationDayRecord
import org.catholiccompanion.app.data.liturgy.CelebrationRank
import org.catholiccompanion.app.data.liturgy.LiturgyRepository

/** Ranks the browser can filter to. Weekdays are excluded: they carry no saint or feast. */
val BROWSABLE_RANKS: List<CelebrationRank> = listOf(
    CelebrationRank.SOLEMNITY,
    CelebrationRank.FEAST,
    CelebrationRank.MEMORIAL,
    CelebrationRank.OPTIONAL_MEMORIAL,
)

data class CelebrationsUiState(
    val isLoading: Boolean = true,
    val scope: CalendarScopeEntity? = null,
    val month: YearMonth = YearMonth.now(),
    val records: List<CelebrationDayRecord> = emptyList(),
    val activeRanks: Set<CelebrationRank> = BROWSABLE_RANKS.toSet(),
    val today: LocalDate = LocalDate.now(),
) {
    /** True when the calendar has no coverage for the selected month at all. */
    val isOutsideCoverage: Boolean get() = !isLoading && scope != null && records.isEmpty()
}

@OptIn(ExperimentalCoroutinesApi::class)
class CelebrationsViewModel(
    private val repository: LiturgyRepository,
) : ViewModel() {
    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val activeRanks = MutableStateFlow(BROWSABLE_RANKS.toSet())

    val uiState = combine(selectedMonth, activeRanks) { month, ranks -> month to ranks }
        .flatMapLatest { (month, ranks) ->
            repository.scopes.flatMapLatest { scopes ->
                stateFor(month, ranks, scopes.firstOrNull())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CelebrationsUiState(),
        )

    fun previousMonth() {
        selectedMonth.value = selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        selectedMonth.value = selectedMonth.value.plusMonths(1)
    }

    fun returnToCurrentMonth() {
        selectedMonth.value = YearMonth.now()
    }

    /** Toggling the last active rank off would leave an unexplained empty list, so keep one on. */
    fun toggleRank(rank: CelebrationRank) {
        val current = activeRanks.value
        val updated = if (rank in current) current - rank else current + rank
        if (updated.isNotEmpty()) activeRanks.value = updated
    }

    private fun stateFor(
        month: YearMonth,
        ranks: Set<CelebrationRank>,
        scope: CalendarScopeEntity?,
    ): Flow<CelebrationsUiState> {
        if (scope == null) {
            return flowOf(CelebrationsUiState(isLoading = false, month = month, activeRanks = ranks))
        }
        return repository
            .observeCelebrationRange(scope.id, month.atDay(1), month.atEndOfMonth())
            .map { records ->
                CelebrationsUiState(
                    isLoading = false,
                    scope = scope,
                    month = month,
                    records = records.filter { it.rank in ranks },
                    activeRanks = ranks,
                )
            }
    }

    class Factory(
        private val repository: LiturgyRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CelebrationsViewModel(repository) as T
    }
}
