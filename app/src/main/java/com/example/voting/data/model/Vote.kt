package com.example.voting.data.model
import kotlinx.serialization.Serializable

@Serializable
data class VoteRequest(
    val pollId: Int,
    val optionId: Int
)

@Serializable
data class VoteResponse(
    val success: Boolean,
    val message: String? = null
)

@Serializable
data class PollResultResponse(
    val pollId: Int,
    val title: String,
    val totalVotes: Int,
    val options: List<OptionResultResponse>
)

@Serializable
data class OptionResultResponse(
    val optionId: Int,
    val text: String,
    val votes: Int,
    val percentage: Double
)