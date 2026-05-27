package com.example.voting.domain.usecase
import com.example.voting.domain.model.Poll
import com.example.voting.domain.repository.PollRepository

class GetAllPollsUseCase(
    private val pollRepository: PollRepository
) {
    suspend operator fun invoke(): Result<List<Poll>> {
        return try {
            pollRepository.getAllPolls()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}