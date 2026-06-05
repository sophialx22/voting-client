package com.example.voting.presentation.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voting.domain.model.PollResult
import com.example.voting.domain.usecase.GetResultsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResultsUiState(
    val isLoading: Boolean = false,
    val results: PollResult? = null,
    val error: String? = null
)

class ResultsViewModel(
    private val getResultsUseCase: GetResultsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    fun loadResults(pollId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = getResultsUseCase(pollId)
            result.onSuccess { results ->
                _uiState.value = _uiState.value.copy(isLoading = false, results = results)
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Ошибка загрузки результатов"
                )
            }
        }
    }
}