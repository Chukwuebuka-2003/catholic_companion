package org.catholiccompanion.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import org.catholiccompanion.app.data.bible.BibleRepository
import org.catholiccompanion.app.data.bible.ResolvedBiblePassage
import org.catholiccompanion.app.data.liturgy.CalendarScopeEntity
import org.catholiccompanion.app.data.liturgy.LiturgicalDayRecord
import org.catholiccompanion.app.data.liturgy.LiturgyRepository

data class TodayUiState(
    val isLoading: Boolean = true,
    val date: LocalDate = LocalDate.now(),
    val scope: CalendarScopeEntity? = null,
    val record: LiturgicalDayRecord? = null,
    val biblePassages: Map<String, ResolvedBiblePassage> = emptyMap(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(
    private val repository: LiturgyRepository,
    private val bibleRepository: BibleRepository,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())

    val uiState = selectedDate
        .flatMapLatest { date ->
            repository.scopes.flatMapLatest { scopes -> stateFor(date, scopes.firstOrNull()) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TodayUiState(),
        )

    fun previousDay() {
        selectedDate.value = selectedDate.value.minusDays(1)
    }

    fun nextDay() {
        selectedDate.value = selectedDate.value.plusDays(1)
    }

    fun returnToToday() {
        selectedDate.value = LocalDate.now()
    }

    private fun stateFor(date: LocalDate, scope: CalendarScopeEntity?): Flow<TodayUiState> {
        if (scope == null) return flowOf(TodayUiState(isLoading = false, date = date))
        return repository.observeDay(scope.id, date)
            .mapLatest { record ->
                val passages = buildMap {
                    record?.readings?.forEach { reading ->
                        if (reading.permittedText == null) {
                            bibleRepository.resolve(reading.citation)?.let { passage ->
                                put(reading.id, passage)
                            }
                        }
                    }
                }
                TodayUiState(false, date, scope, record, passages)
            }
            .onStart { emit(TodayUiState(true, date, scope)) }
    }

    class Factory(
        private val repository: LiturgyRepository,
        private val bibleRepository: BibleRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TodayViewModel::class.java))
            return TodayViewModel(repository, bibleRepository) as T
        }
    }
}
