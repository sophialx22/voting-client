package com.example.voting.data.repository
import com.example.voting.data.model.CreatePollRequest
import com.example.voting.data.model.Poll as DataPoll
import com.example.voting.data.network.ApiService
import com.example.voting.domain.model.Poll
import com.example.voting.domain.model.PollOption
import com.example.voting.domain.repository.AuthRepository
import com.example.voting.domain.repository.PollRepository

class PollRepositoryImpl(
    private val apiService: ApiService,
    private val authRepository: AuthRepository
) : PollRepository {

    private suspend fun mapToDomain(dataPoll: DataPoll): Poll {
        return Poll(
            id = dataPoll.id,
            title = dataPoll.title,
            description = dataPoll.description,
            createdAt = dataPoll.createdAt,
            createdBy = dataPoll.createdBy,
            isActive = dataPoll.isActive,
            options = dataPoll.options.map { option ->
                PollOption(
                    id = option.id,
                    text = option.text,
                    votesCount = option.votesCount
                )
            }
        )
    }

    private suspend fun mapToDomainList(dataPolls: List<DataPoll>): List<Poll> {
        return dataPolls.map { mapToDomain(it) }
    }

    override suspend fun getAllPolls(): Result<List<Poll>> {
        return try {
            val token = authRepository.getToken()
            if (token == null) return Result.failure(Exception("Не авторизован"))
            val polls = apiService.getAllPolls(token)
            Result.success(mapToDomainList(polls))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPollById(pollId: Int): Result<Poll> {
        return try {
            val token = authRepository.getToken()
            if (token == null) return Result.failure(Exception("Не авторизован"))
            val poll = apiService.getPollById(pollId, token)
            Result.success(mapToDomain(poll))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMyPolls(userEmail: String): Result<List<Poll>> {
        return try {
            val token = authRepository.getToken()
            if (token == null) return Result.failure(Exception("Не авторизован"))
            val polls = apiService.getMyPolls(userEmail, token)
            Result.success(mapToDomainList(polls))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createPoll(title: String, description: String, options: List<String>): Result<Poll> {
        return try {
            val token = authRepository.getToken()
            println("Токен в createPoll: $token")
            if (token == null) return Result.failure(Exception("Не авторизован"))
            val request = CreatePollRequest(title, description, options)
            val poll = apiService.createPoll(request, token)
            Result.success(mapToDomain(poll))
        } catch (e: Exception) {
            println("Ошибка createPoll: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updatePoll(pollId: Int, title: String, description: String, options: List<String>): Result<Poll> {
        return try {
            val token = authRepository.getToken()
            println("Токен в updatePoll: $token")
            if (token == null) return Result.failure(Exception("Не авторизован"))
            val request = CreatePollRequest(title, description, options)
            val poll = apiService.updatePoll(pollId, request, token)
            println("Голосование обновлено: id=${poll.id}")
            Result.success(mapToDomain(poll))
        } catch (e: Exception) {
            println("Ошибка updatePoll: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deletePoll(pollId: Int): Result<Boolean> {
        return try {
            val token = authRepository.getToken()
            if (token == null) return Result.failure(Exception("Не авторизован"))
            val result = apiService.deletePoll(pollId, token)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}