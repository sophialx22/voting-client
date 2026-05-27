package com.example.voting.domain.usecase
import com.example.voting.domain.model.Poll
import com.example.voting.domain.repository.AuthRepository
import com.example.voting.domain.repository.PollRepository

class GetMyPollsUseCase(
    private val pollRepository: PollRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<List<Poll>> {
        val userEmail = authRepository.getCurrentUserEmail()
        if (userEmail.isNullOrBlank()) {
            return Result.failure(Exception("Пользователь не авторизован"))
        }
        return pollRepository.getMyPolls(userEmail)
    }
}