package com.example.voting.data.model
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val email: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null, // JWT токен или просто сессионный ключ
    val user: User? = null
)