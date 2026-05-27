package com.example.voting.domain.usecase
import com.example.voting.domain.repository.PollRepository

class DeletePollUseCase(
    private val pollRepository: PollRepository
) {
    suspend operator fun invoke(pollId: Int): Result<Boolean> {
        if (pollId <= 0) {
            return Result.failure(IllegalArgumentException("Неверный ID голосования"))
        }
        return pollRepository.deletePoll(pollId)
    }
}