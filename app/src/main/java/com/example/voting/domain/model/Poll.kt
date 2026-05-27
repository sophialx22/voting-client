package com.example.voting.domain.model

data class Poll(
    val id: Int,
    val title: String,
    val description: String,
    val createdAt: Long,
    val createdBy: String,
    val isActive: Boolean,
    val options: List<PollOption>
)

data class PollOption(
    val id: Int,
    val text: String,
    val votesCount: Int = 0
)