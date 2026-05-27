package com.example.voting.domain.repository

interface AuthRepository {
    suspend fun register(email: String, password: String): Result<Boolean>
    suspend fun login(email: String, password: String): Result<Boolean>
    suspend fun logout(): Result<Boolean>
    suspend fun isAuthenticated(): Boolean
    suspend fun getCurrentUserEmail(): String?
    suspend fun saveUserEmail(email: String)
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun clearUserData()
}