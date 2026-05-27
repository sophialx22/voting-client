package com.example.voting.data.repository
import com.example.voting.data.model.VoteRequest
import com.example.voting.data.network.ApiService
import com.example.voting.domain.model.OptionResult
import com.example.voting.domain.model.PollResult
import com.example.voting.domain.repository.AuthRepository
import com.example.voting.domain.repository.VoteRepository

class VoteRepositoryImpl(
    private val apiService: ApiService,
    private val authRepository: AuthRepository
) : VoteRepository {

    override suspend fun castVote(pollId: Int, optionId: Int): Result<Boolean> {
        return try {
            val token = authRepository.getToken()
            if (token == null) return Result.failure(Exception("Не авторизован"))
            val response = apiService.castVote(VoteRequest(pollId, optionId), token)
            Result.success(response.success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getResults(pollId: Int): Result<PollResult> {
        return try {
            val token = authRepository.getToken()
            if (token == null) return Result.failure(Exception("Не авторизован"))
            val dataResult = apiService.getResults(pollId, token)
            val domainResult = PollResult(
                pollId = dataResult.pollId,
                title = dataResult.title,
                totalVotes = dataResult.totalVotes,
                options = dataResult.options.map { option ->
                    OptionResult(
                        optionId = option.optionId,
                        text = option.text,
                        votes = option.votes,
                        percentage = option.percentage
                    )
                }
            )
            Result.success(domainResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun hasUserVoted(pollId: Int, userEmail: String): Result<Boolean> {
        return try {
            val token = authRepository.getToken()
            if (token == null) return Result.success(false)
            val result = apiService.hasUserVoted(pollId, userEmail, token)
            Result.success(result)
        } catch (e: Exception) {
            Result.success(false)
        }
    }
}