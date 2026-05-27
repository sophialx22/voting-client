package com.example.voting.presentation.auth
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voting.data.network.ApiService
import com.example.voting.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val firebaseAuth = FirebaseAuth.getInstance()

    val isAuthenticated: StateFlow<Boolean> = _uiState.asStateFlow()
        .let { flow ->
            MutableStateFlow(flow.value.isAuthenticated).also { authFlow ->
                viewModelScope.launch {
                    flow.collect { state ->
                        authFlow.emit(state.isAuthenticated)
                    }
                }
            }
        }

    init {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                onFirebaseUserSignedIn(currentUser)
            }
        }
    }

    fun register(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Заполните все поля")
            return
        }

        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Пароль должен быть минимум 6 символов")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    onFirebaseUserSignedIn(firebaseUser)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(error = "Ошибка регистрации")
                }
            } catch (e: Exception) {
                val errorMessage = getFirebaseErrorMessage(e)
                _uiState.value = _uiState.value.copy(error = errorMessage)
                println("Firebase registration error: ${e.message}")
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Заполните все поля")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    onFirebaseUserSignedIn(firebaseUser)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(error = "Ошибка входа")
                }
            } catch (e: Exception) {
                val errorMessage = getFirebaseErrorMessage(e)
                _uiState.value = _uiState.value.copy(error = errorMessage)
                println("Firebase login error: ${e.message}")
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun getFirebaseErrorMessage(e: Exception): String {
        return when {
            e is FirebaseAuthException -> {
                when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Неверный формат email"
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "Пользователь с таким email уже существует"
                    "ERROR_WEAK_PASSWORD" -> "Слишком слабый пароль (минимум 6 символов)"
                    "ERROR_USER_NOT_FOUND" -> "Такой учетной записи не существует"
                    "ERROR_WRONG_PASSWORD" -> "Неверный пароль"
                    "ERROR_USER_DISABLED" -> "Учетная запись заблокирована"
                    "ERROR_TOO_MANY_REQUESTS" -> "Слишком много попыток. Попробуйте позже"
                    "ERROR_NETWORK_REQUEST_FAILED" -> "Ошибка сети. Проверьте подключение"
                    else -> e.message ?: "Ошибка авторизации"
                }
            }
            e.message?.contains("EMAIL_EXISTS") == true -> "Пользователь с таким email уже существует"
            e.message?.contains("INVALID_EMAIL") == true -> "Неверный формат email"
            e.message?.contains("WEAK_PASSWORD") == true -> "Слишком слабый пароль (минимум 6 символов)"
            e.message?.contains("EMAIL_NOT_FOUND") == true -> "Такой учетной записи не существует"
            e.message?.contains("INVALID_PASSWORD") == true -> "Неверный пароль"
            else -> e.message ?: "Ошибка авторизации"
        }
    }

    private suspend fun onFirebaseUserSignedIn(firebaseUser: FirebaseUser) {
        try {
            val idToken = firebaseUser.getIdToken(false).await().token
            println("ТОКЕН: $idToken")
            if (idToken != null) {
                val response = apiService.firebaseSignIn(idToken)
                if (response.success && response.token != null) {
                    authRepository.saveToken(response.token)
                    authRepository.saveUserEmail(firebaseUser.email ?: "")
                    _uiState.value = _uiState.value.copy(isAuthenticated = true)
                } else {
                    _uiState.value = _uiState.value.copy(error = "Ошибка авторизации на сервере")
                }
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message)
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                firebaseAuth.signOut()
                authRepository.clearUserData()
                _uiState.value = AuthUiState(isAuthenticated = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}