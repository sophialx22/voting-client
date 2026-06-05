package com.example.voting.presentation.voting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voting.domain.model.Poll
import com.example.voting.domain.usecase.CastVoteUseCase
import com.example.voting.domain.usecase.GetPollByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VotingUiState(
    val isLoading: Boolean = false,
    val poll: Poll? = null,
    val error: String? = null,
    val isVoting: Boolean = false
)

class VotingViewModel(
    private val getPollByIdUseCase: GetPollByIdUseCase,
    private val castVoteUseCase: CastVoteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VotingUiState())
    val uiState: StateFlow<VotingUiState> = _uiState.asStateFlow()

    fun loadPoll(pollId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
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

    suspend fun castVote(pollId: Int, optionId: Int): Result<Boolean> {
        _uiState.value = _uiState.value.copy(isVoting = true)
        val result = castVoteUseCase(pollId, optionId)
        _uiState.value = _uiState.value.copy(isVoting = false)
        return result
    }
}