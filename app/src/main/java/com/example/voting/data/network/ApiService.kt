package com.example.voting.data.network
import com.example.voting.data.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.request.header

class ApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun register(request: RegisterRequest): AuthResponse {
        return client.post {
            url("$baseUrl/auth/register")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    suspend fun login(request: LoginRequest): AuthResponse {
        return client.post {
            url("$baseUrl/auth/login")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    suspend fun logout(token: String): Boolean {
        return client.post {
            url("$baseUrl/auth/logout")
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }
    suspend fun firebaseSignIn(idToken: String): AuthResponse {
        return client.post {
            url("$baseUrl/auth/firebase")
            contentType(ContentType.Application.Json)
            setBody(mapOf("idToken" to idToken))
        }.body()
    }
    suspend fun getAllPolls(token: String): List<Poll> {
        return client.get {
            url("$baseUrl/polls")
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun getPollById(pollId: Int, token: String): Poll {
        return client.get {
            url("$baseUrl/polls/$pollId")
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun getMyPolls(userEmail: String, token: String): List<Poll> {
        return client.get {
            url("$baseUrl/polls/my?email=$userEmail")
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun createPoll(request: CreatePollRequest, token: String): Poll {
        return client.post {
            url("$baseUrl/polls/create")
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }.body()
    }

    suspend fun updatePoll(pollId: Int, request: CreatePollRequest, token: String): Poll {
        return client.put {
            url("$baseUrl/polls/$pollId")
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }.body()
    }

    suspend fun deletePoll(pollId: Int, token: String): Boolean {
        return client.delete {
            url("$baseUrl/polls/$pollId")
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun castVote(request: VoteRequest, token: String): VoteResponse {
        return client.post {
            url("$baseUrl/votes/cast")
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }.body()
    }

    suspend fun getResults(pollId: Int, token: String): PollResultData {
        return client.get {
            url("$baseUrl/votes/results/$pollId")
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun hasUserVoted(pollId: Int, userEmail: String, token: String): Boolean {
        return client.get {
            url("$baseUrl/votes/has-voted/$pollId?email=$userEmail")
            header("Authorization", "Bearer $token")
        }.body()
    }
}