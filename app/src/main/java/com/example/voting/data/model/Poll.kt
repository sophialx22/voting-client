package com.example.voting.data.model
import kotlinx.serialization.Serializable

@Serializable
data class Poll(
    val id: Int,
    val title: String,
    val description: String,
    val createdAt: Long,
    val createdBy: String, // email создателя
    val isActive: Boolean,
    val options: List<PollOption>
)

@Serializable
data class PollOption(
    val id: Int,
    val text: String,
    val votesCount: Int = 0
)

@Serializable
data class CreatePollRequest(
    val title: String,
    val description: String,
    val options: List<String> // список вариантов ответа
)