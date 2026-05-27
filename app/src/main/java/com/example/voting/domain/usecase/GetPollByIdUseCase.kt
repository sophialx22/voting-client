package com.example.voting.domain.usecase
import com.example.voting.domain.model.Poll
import com.example.voting.domain.repository.PollRepository

class GetPollByIdUseCase(
    private val pollRepository: PollRepository
) {
    suspend operator fun invoke(pollId: Int): Result<Poll> {
        return try {
            pollRepository.getPollById(pollId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}