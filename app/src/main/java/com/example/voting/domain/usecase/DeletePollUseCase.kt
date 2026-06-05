package com.example.voting.domain.usecase
import com.example.voting.domain.repository.PollRepository

class DeletePollUseCase(
    private val pollRepository: PollRepository
) {
    suspend operator fun invoke(pollId: Int): Result<Boolean> {
        return pollRepository.deletePoll(pollId)
    }
}