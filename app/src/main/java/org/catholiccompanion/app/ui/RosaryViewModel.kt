package org.catholiccompanion.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.catholiccompanion.app.data.RosaryProgressRepository
import org.catholiccompanion.app.model.MysterySet
import org.catholiccompanion.app.model.PrayerStep
import org.catholiccompanion.app.model.RosaryCatalog
import org.catholiccompanion.app.model.RosaryProgress

data class RosaryUiState(
    val isLoading: Boolean = true,
    val selectedSet: MysterySet = MysterySet.recommendedFor(LocalDate.now().dayOfWeek),
    val progress: RosaryProgress? = null,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
) {
    val steps: List<PrayerStep>
        get() = progress?.let { RosaryCatalog.stepsFor(it.mysterySet) }.orEmpty()

    val isComplete: Boolean
        get() = progress != null && progress.stepIndex >= steps.size

    val currentStep: PrayerStep?
        get() = progress?.stepIndex?.let(steps::getOrNull)
}

class RosaryViewModel(
    private val repository: RosaryProgressRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RosaryUiState())
    val uiState: StateFlow<RosaryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.progress.collect { storedProgress ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        selectedSet = storedProgress?.mysterySet ?: current.selectedSet,
                        progress = storedProgress,
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.soundEnabled.collect { enabled ->
                _uiState.update { it.copy(soundEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            repository.hapticsEnabled.collect { enabled ->
                _uiState.update { it.copy(hapticsEnabled = enabled) }
            }
        }
    }

    fun selectMysterySet(set: MysterySet) {
        if (_uiState.value.progress == null) {
            _uiState.update { it.copy(selectedSet = set) }
        }
    }

    fun start() = updateProgress(RosaryProgress(_uiState.value.selectedSet, 0))

    fun next() {
        val current = _uiState.value.progress ?: return
        val lastIndex = RosaryCatalog.stepsFor(current.mysterySet).size
        updateProgress(current.copy(stepIndex = (current.stepIndex + 1).coerceAtMost(lastIndex)))
    }

    fun previous() {
        val current = _uiState.value.progress ?: return
        updateProgress(current.copy(stepIndex = (current.stepIndex - 1).coerceAtLeast(0)))
    }

    fun startAgain() {
        val selectedSet = _uiState.value.progress?.mysterySet ?: _uiState.value.selectedSet
        updateProgress(RosaryProgress(selectedSet, 0))
    }

    fun endSession() {
        _uiState.update { it.copy(progress = null) }
        viewModelScope.launch { repository.clear() }
    }

    fun setSoundEnabled(enabled: Boolean) {
        _uiState.update { it.copy(soundEnabled = enabled) }
        viewModelScope.launch { repository.setSoundEnabled(enabled) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(hapticsEnabled = enabled) }
        viewModelScope.launch { repository.setHapticsEnabled(enabled) }
    }

    private fun updateProgress(progress: RosaryProgress) {
        _uiState.update { it.copy(progress = progress, selectedSet = progress.mysterySet) }
        viewModelScope.launch { repository.save(progress) }
    }

    class Factory(
        private val repository: RosaryProgressRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(RosaryViewModel::class.java))
            return RosaryViewModel(repository) as T
        }
    }
}
