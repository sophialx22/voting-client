package com.example.voting.domain.usecase
import com.example.voting.domain.repository.AuthRepository
import com.example.voting.domain.repository.VoteRepository

class CheckUserVotedUseCase(
    private val voteRepository: VoteRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(pollId: Int): Result<Boolean> {
        val userEmail = authRepository.getCurrentUserEmail()
        if (userEmail.isNullOrBlank()) {
            return Result.success(false)
        }
        return voteRepository.hasUserVoted(pollId, userEmail)
    }
}