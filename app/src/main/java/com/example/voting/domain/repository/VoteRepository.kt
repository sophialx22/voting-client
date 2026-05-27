package com.example.voting.domain.repository
import com.example.voting.domain.model.PollResult

interface VoteRepository {
    suspend fun castVote(pollId: Int, optionId: Int): Result<Boolean>
    suspend fun getResults(pollId: Int): Result<PollResult>
    suspend fun hasUserVoted(pollId: Int, userEmail: String): Result<Boolean>
}