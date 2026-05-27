package com.example.voting.data.repository
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.voting.data.model.LoginRequest
import com.example.voting.data.model.RegisterRequest
import com.example.voting.data.network.ApiService
import com.example.voting.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class AuthRepositoryImpl(
    private val context: Context,
    private val apiService: ApiService
) : AuthRepository {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }

    override suspend fun register(email: String, password: String): Result<Boolean> {
        return try {
            val response = apiService.register(RegisterRequest(email, password))
            if (response.success && response.token != null) {
                saveToken(response.token)
                saveUserEmail(email)
                Result.success(true)
            } else {
                Result.failure(Exception(response.message ?: "Ошибка регистрации"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.success && response.token != null) {
                saveToken(response.token)
                saveUserEmail(email)
                Result.success(true)
            } else {
                Result.failure(Exception(response.message ?: "Неверный email или пароль"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Boolean> {
        return try {
            val token = getToken()
            if (token != null) {
                apiService.logout(token)
            }
            clearUserData()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isAuthenticated(): Boolean {
        return getToken() != null && getCurrentUserEmail() != null
    }

    override suspend fun getCurrentUserEmail(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[USER_EMAIL_KEY]
    }

    override suspend fun saveUserEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL_KEY] = email
        }
    }

    override suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    override suspend fun getToken(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[TOKEN_KEY]
    }

    override suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_EMAIL_KEY)
        }
    }
}