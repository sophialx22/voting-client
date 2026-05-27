package com.example.voting.domain.usecase
import com.example.voting.domain.model.PollResult
import com.example.voting.domain.repository.VoteRepository

class GetResultsUseCase(
    private val voteRepository: VoteRepository
) {
    suspend operator fun invoke(pollId: Int): Result<PollResult> {
        if (pollId <= 0) {
            return Result.failure(IllegalArgumentException("Неверный ID голосования"))
        }
        return voteRepository.getResults(pollId)
    }
}