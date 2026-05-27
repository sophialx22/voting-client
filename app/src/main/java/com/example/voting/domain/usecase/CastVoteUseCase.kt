package com.example.voting.domain.usecase
import com.example.voting.domain.repository.AuthRepository
import com.example.voting.domain.repository.VoteRepository

class CastVoteUseCase(
    private val voteRepository: VoteRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(pollId: Int, optionId: Int): Result<Boolean> {
        if (pollId <= 0) {
            return Result.failure(IllegalArgumentException("Неверный ID голосования"))
        }
        if (optionId <= 0) {
            return Result.failure(IllegalArgumentException("Неверный ID варианта ответа"))
        }

        val userEmail = authRepository.getCurrentUserEmail()
        if (userEmail.isNullOrBlank()) {
            return Result.failure(Exception("Пользователь не авторизован"))
        }

        val hasVoted = voteRepository.hasUserVoted(pollId, userEmail)
        if (hasVoted.isSuccess && hasVoted.getOrNull() == true) {
            return Result.failure(IllegalStateException("Вы уже голосовали в этом опросе"))
        }

        return voteRepository.castVote(pollId, optionId)
    }
}