package com.example.voting
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.example.voting.data.network.NetworkClient
import com.example.voting.data.repository.AuthRepositoryImpl
import com.example.voting.data.repository.PollRepositoryImpl
import com.example.voting.data.repository.VoteRepositoryImpl
import com.example.voting.data.store.SearchHistoryDataStore
import com.example.voting.domain.repository.AuthRepository
import com.example.voting.domain.repository.PollRepository
import com.example.voting.domain.repository.VoteRepository
import com.example.voting.domain.usecase.*
import com.example.voting.presentation.auth.AuthViewModel
import com.example.voting.presentation.navigation.NavGraph
import com.example.voting.presentation.polls.PollListViewModel
import com.example.voting.presentation.theme.VotingTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme")

class MainActivity : ComponentActivity() {

    private val searchHistoryDataStore by lazy {
        SearchHistoryDataStore(applicationContext)
    }

    private val apiService by lazy { NetworkClient.getApiService() }

    private val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(applicationContext, apiService)
    }

    private val pollRepository: PollRepository by lazy {
        PollRepositoryImpl(apiService, authRepository)
    }

    private val voteRepository: VoteRepository by lazy {
        VoteRepositoryImpl(apiService, authRepository)
    }
    private val getAllPollsUseCase by lazy { GetAllPollsUseCase(pollRepository) }
    private val getMyPollsUseCase by lazy { GetMyPollsUseCase(pollRepository, authRepository) }
    private val createPollUseCase by lazy { CreatePollUseCase(pollRepository) }
    private val deletePollUseCase by lazy { DeletePollUseCase(pollRepository) }
    private val updatePollUseCase by lazy { UpdatePollUseCase(pollRepository) }
    private val castVoteUseCase by lazy { CastVoteUseCase(voteRepository, authRepository) }
    private val getResultsUseCase by lazy { GetResultsUseCase(voteRepository) }
    private val checkUserVotedUseCase by lazy { CheckUserVotedUseCase(voteRepository, authRepository) }

    private lateinit var authViewModel: AuthViewModel
    private lateinit var pollListViewModel: PollListViewModel

    private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")

    private suspend fun isDarkThemeSaved(): Boolean {
        val preferences = themeDataStore.data.first()
        return preferences[DARK_THEME_KEY] ?: false
    }

    private suspend fun saveDarkTheme(isDark: Boolean) {
        themeDataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = isDark
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel = AuthViewModel(authRepository, apiService)
        pollListViewModel = PollListViewModel(
            getAllPollsUseCase,
            getMyPollsUseCase,
            createPollUseCase,
            deletePollUseCase,
            updatePollUseCase,
            castVoteUseCase,
            getResultsUseCase,
            checkUserVotedUseCase,
            searchHistoryDataStore,
            authRepository
        )

        var isDarkTheme by mutableStateOf(false)

        lifecycleScope.launch {
            isDarkTheme = isDarkThemeSaved()
        }

        val toggleTheme: () -> Unit = {
            lifecycleScope.launch {
                isDarkTheme = !isDarkTheme
                saveDarkTheme(isDarkTheme)
            }
        }

        setContent {
            VotingTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        authViewModel = authViewModel,
                        pollListViewModel = pollListViewModel,
                        onToggleTheme = toggleTheme
                    )
                }
            }
        }
    }
}