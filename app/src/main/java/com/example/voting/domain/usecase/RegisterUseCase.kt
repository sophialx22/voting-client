package com.example.voting.domain.usecase
import com.example.voting.domain.repository.AuthRepository

class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Boolean> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email и пароль не могут быть пустыми"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Пароль должен содержать минимум 6 символов"))
        }
        return authRepository.register(email, password)
    }
}