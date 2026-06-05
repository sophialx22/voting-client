package com.example.voting.data.model
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null
)
