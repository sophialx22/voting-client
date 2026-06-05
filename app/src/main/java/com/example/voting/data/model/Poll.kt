package com.example.voting.data.model
import kotlinx.serialization.Serializable

@Serializable
data class PollResponse(
    val id: Int,
    val title: String,
    val description: String,
    val createdAt: Long,
    val createdBy: String,
    val isActive: Boolean,
    val options: List<OptionResponse>
)

@Serializable
data class OptionResponse(
    val id: Int,
    val text: String,
    val votesCount: Int = 0
)

@Serializable
data class CreatePollRequest(
    val title: String,
    val description: String,
    val options: List<String>
)