package com.example.voting.domain.usecase
import com.example.voting.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Boolean> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email и пароль не могут быть пустыми"))
        }
        return authRepository.login(email, password)
    }
}