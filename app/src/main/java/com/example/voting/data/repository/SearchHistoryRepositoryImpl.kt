package com.example.voting.data.repository
import com.example.voting.data.store.SearchHistoryDataStore
import com.example.voting.domain.repository.SearchHistoryRepository

class SearchHistoryRepositoryImpl(
    private val dataStore: SearchHistoryDataStore
) : SearchHistoryRepository {
    override suspend fun getSearchHistory() = dataStore.getSearchHistory()
    override suspend fun addToHistory(query: String) = dataStore.addToHistory(query)
    override suspend fun clearHistory() = dataStore.clearHistory()
}