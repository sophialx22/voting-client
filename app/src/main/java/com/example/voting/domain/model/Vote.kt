package com.example.voting.domain.model

data class Vote(
    val pollId: Int,
    val optionId: Int
)

data class PollResult(
    val pollId: Int,
    val title: String,
    val totalVotes: Int,
    val options: List<OptionResult>
)

data class OptionResult(
    val optionId: Int,
    val text: String,
    val votes: Int,
    val percentage: Double
)