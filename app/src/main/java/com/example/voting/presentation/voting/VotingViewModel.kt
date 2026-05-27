package com.example.voting.presentation.voting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voting.domain.usecase.CastVoteUseCase
import com.example.voting.domain.usecase.GetPollByIdUseCase
import com.example.voting.domain.model.Poll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class VotingUiState(
    val isLoading: Boolean = false,
    val poll: Poll? = null,
    val error: String? = null,
    val isVoting: Boolean = false,
    val voteSuccess: Boolean = false
)

class VotingViewModel(
    private val getPollByIdUseCase: GetPollByIdUseCase,
    private val castVoteUseCase: CastVoteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VotingUiState())
    val uiState: StateFlow<VotingUiState> = _uiState

    fun loadPoll(pollId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = getPollByIdUseCase(pollId)
            result.onSuccess { poll ->
                _uiState.value = _uiState.value.copy(isLoading = false, poll = poll)
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Ошибка загрузки"
                )
            }
        }
    }

    fun castVote(pollId: Int, optionId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVoting = true, error = null)
            val result = castVoteUseCase(pollId, optionId)
            _uiState.value = _uiState.value.copy(isVoting = false)

            result.onSuccess { success ->
                if (success) {
                    _uiState.value = _uiState.value.copy(voteSuccess = true)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(error = "Не удалось проголосовать")
                }
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(error = exception.message ?: "Ошибка голосования")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}