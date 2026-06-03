package com.example.voting.presentation.polls
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voting.data.store.SearchHistoryDataStore
import com.example.voting.domain.model.Poll
import com.example.voting.domain.model.PollResult
import com.example.voting.domain.repository.AuthRepository
import com.example.voting.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PollListUiState(
    val isLoading: Boolean = false,
    val polls: List<Poll> = emptyList(),
    val myPolls: List<Poll> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val searchHistory: List<String> = emptyList(),
    val showOnlyMyPolls: Boolean = false,
    val currentUserEmail: String? = null
)

class PollListViewModel(
    private val getAllPollsUseCase: GetAllPollsUseCase,
    private val getMyPollsUseCase: GetMyPollsUseCase,
    private val createPollUseCase: CreatePollUseCase,
    private val deletePollUseCase: DeletePollUseCase,
    private val updatePollUseCase: UpdatePollUseCase,
    private val castVoteUseCase: CastVoteUseCase,
    private val getResultsUseCase: GetResultsUseCase,
    private val searchHistoryDataStore: SearchHistoryDataStore,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PollListUiState())
    val uiState: StateFlow<PollListUiState> = _uiState.asStateFlow()

    init {
        loadPolls()
        loadSearchHistory()
        loadCurrentUserEmail()
    }

    private fun loadCurrentUserEmail() {
        viewModelScope.launch {
            val email = authRepository.getCurrentUserEmail()
            println("Email загружен: $email")
            _uiState.value = _uiState.value.copy(currentUserEmail = email)
        }
    }

    fun loadPolls() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = if (_uiState.value.showOnlyMyPolls) {
                getMyPollsUseCase()
            } else {
                getAllPollsUseCase()
            }

            result.onSuccess { polls ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    polls = if (_uiState.value.showOnlyMyPolls) {
                        _uiState.value.myPolls
                    } else {
                        polls
                    },
                    myPolls = if (_uiState.value.showOnlyMyPolls) polls else _uiState.value.myPolls
                )
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Ошибка загрузки"
                )
            }
        }
    }

    fun toggleShowOnlyMyPolls() {
        _uiState.value = _uiState.value.copy(showOnlyMyPolls = !_uiState.value.showOnlyMyPolls)
        loadPolls()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    private fun loadSearchHistory() {
        viewModelScope.launch {
            val history = searchHistoryDataStore.getSearchHistory()
            _uiState.value = _uiState.value.copy(searchHistory = history)
            println("История загружена: $history")
        }
    }

    fun addToSearchHistory(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            searchHistoryDataStore.addToHistory(query)
            val newHistory = searchHistoryDataStore.getSearchHistory()
            _uiState.value = _uiState.value.copy(searchHistory = newHistory)
            println("Добавлен в историю: $query, история: $newHistory")
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            searchHistoryDataStore.clearHistory()
            _uiState.value = _uiState.value.copy(searchHistory = emptyList())
            println("История очищена")
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "")
    }

    fun createPoll(title: String, description: String, options: List<String>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val token = authRepository.getToken()
            if (token.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка авторизации. Пожалуйста, выйдите и зайдите заново."
                )
                return@launch
            }

            val result = createPollUseCase(title, description, options)
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess {
                loadPolls()
                onSuccess()
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(error = exception.message ?: "Ошибка создания")
            }
        }
    }

    fun deletePoll(pollId: Int) {
        viewModelScope.launch {
            val result = deletePollUseCase(pollId)
            result.onSuccess {
                loadPolls()
            }
        }
    }

    suspend fun castVote(pollId: Int, optionId: Int): Result<Boolean> {
        return castVoteUseCase(pollId, optionId)
    }

    suspend fun loadResults(pollId: Int): Result<PollResult> {
        return getResultsUseCase(pollId)
    }

    suspend fun getPollById(pollId: Int): Result<Poll> {
        return getAllPollsUseCase().map { polls ->
            polls.find { it.id == pollId }
                ?: throw Exception("Голосование не найдено")
        }
    }
    suspend fun updatePoll(pollId: Int, title: String, description: String, options: List<String>): Result<Poll> {
        return updatePollUseCase(pollId, title, description, options)
    }

    fun clearPolls() {
        _uiState.value = PollListUiState()
    }

    fun refreshUserEmail() {
        viewModelScope.launch {
            val email = authRepository.getCurrentUserEmail()
            _uiState.value = _uiState.value.copy(currentUserEmail = email)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    val filteredPolls: List<Poll>
        get() {
            val query = _uiState.value.searchQuery.lowercase()
            return if (query.isBlank()) {
                _uiState.value.polls
            } else {
                _uiState.value.polls.filter { poll ->
                    poll.title.lowercase().contains(query) ||
                            poll.description.lowercase().contains(query)
                }
            }
        }
}