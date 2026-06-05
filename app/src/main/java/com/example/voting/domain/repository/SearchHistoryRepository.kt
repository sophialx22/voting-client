package com.example.voting.domain.repository

interface SearchHistoryRepository {
    suspend fun getSearchHistory(): List<String>
    suspend fun addToHistory(query: String)
    suspend fun clearHistory()
}