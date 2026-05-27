package com.example.voting.data.model
import kotlinx.serialization.Serializable

@Serializable
data class PollResultData(
    val pollId: Int,
    val title: String,
    val totalVotes: Int,
    val options: List<OptionResultData>
)

@Serializable
data class OptionResultData(
    val optionId: Int,
    val text: String,
    val votes: Int,
    val percentage: Double
)