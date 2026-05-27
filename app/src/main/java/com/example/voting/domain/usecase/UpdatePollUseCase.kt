package com.example.voting.domain.usecase
import com.example.voting.domain.model.Poll
import com.example.voting.domain.repository.PollRepository

class UpdatePollUseCase(
    private val pollRepository: PollRepository
) {
    suspend operator fun invoke(
        pollId: Int,
        title: String,
        description: String,
        options: List<String>
    ): Result<Poll> {
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Название голосования не может быть пустым"))
        }
        if (options.size < 2) {
            return Result.failure(IllegalArgumentException("Должно быть минимум 2 варианта ответа"))
        }
        if (options.any { it.isBlank() }) {
            return Result.failure(IllegalArgumentException("Варианты ответа не могут быть пустыми"))
        }
        return pollRepository.updatePoll(pollId, title, description, options)
    }
}