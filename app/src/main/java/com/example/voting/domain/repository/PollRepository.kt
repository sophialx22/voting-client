package com.example.voting.domain.repository
import com.example.voting.domain.model.Poll

interface PollRepository {
    suspend fun getAllPolls(): Result<List<Poll>>
    suspend fun getPollById(pollId: Int): Result<Poll>
    suspend fun getMyPolls(userEmail: String): Result<List<Poll>>
    suspend fun createPoll(title: String, description: String, options: List<String>): Result<Poll>
    suspend fun updatePoll(pollId: Int, title: String, description: String, options: List<String>): Result<Poll>  // ← добавить
    suspend fun deletePoll(pollId: Int): Result<Boolean>
}