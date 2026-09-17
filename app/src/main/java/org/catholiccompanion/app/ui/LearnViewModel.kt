package org.catholiccompanion.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.catholiccompanion.app.ai.AiCitation
import org.catholiccompanion.app.ai.AiHistoryItem
import org.catholiccompanion.app.ai.AiRepository
import org.catholiccompanion.app.ai.AiRequestContext
import org.catholiccompanion.app.ai.AiTask

enum class LearnMessageRole { USER, ASSISTANT }

data class LearnMessage(
    val role: LearnMessageRole,
    val text: String,
    val citations: List<AiCitation> = emptyList(),
    val limitations: List<String> = emptyList(),
)

data class LearnUiState(
    val isConfigured: Boolean,
    val draft: String = "",
    val messages: List<LearnMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class LearnViewModel(private val repository: AiRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LearnUiState(isConfigured = repository.isConfigured))
    val uiState: StateFlow<LearnUiState> = _uiState.asStateFlow()

    fun updateDraft(value: String) {
        _uiState.update { it.copy(draft = value.take(800), error = null) }
    }

    fun ask(task: AiTask, question: String, context: AiRequestContext) {
        val cleaned = question.trim()
        val current = _uiState.value
        if (cleaned.length < 2 || current.isLoading || !current.isConfigured) return
        val history = current.messages.takeLast(6).map { message ->
            AiHistoryItem(
                role = if (message.role == LearnMessageRole.USER) "user" else "assistant",
                text = message.text,
            )
        }
        _uiState.update {
            it.copy(
                draft = "",
                error = null,
                isLoading = true,
                messages = it.messages + LearnMessage(LearnMessageRole.USER, cleaned),
            )
        }
        viewModelScope.launch {
            runCatching { repository.ask(task, cleaned, context, history) }
                .onSuccess { answer ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            messages = it.messages + LearnMessage(
                                role = LearnMessageRole.ASSISTANT,
                                text = answer.text,
                                citations = answer.citations,
                                limitations = answer.limitations,
                            ),
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message ?: "The explanation failed.")
                    }
                }
        }
    }

    fun clearConversation() {
        if (!_uiState.value.isLoading) {
            _uiState.update { it.copy(messages = emptyList(), draft = "", error = null) }
        }
    }

    class Factory(private val repository: AiRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(LearnViewModel::class.java))
            return LearnViewModel(repository) as T
        }
    }
}

