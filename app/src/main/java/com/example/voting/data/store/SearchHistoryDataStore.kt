package com.example.voting.data.store
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "search_history")

class SearchHistoryDataStore(private val context: Context) {
    companion object {
        private val HISTORY_KEY = stringPreferencesKey("search_history")
        private const val MAX_HISTORY_SIZE = 10
    }

    suspend fun getSearchHistory(): List<String> {
        val preferences = context.dataStore.data.first()
        val historyString = preferences[HISTORY_KEY] ?: ""
        return if (historyString.isBlank()) emptyList()
        else historyString.split("||").filter { it.isNotBlank() }
    }

    suspend fun addToHistory(query: String) {
        if (query.isBlank()) return

        val currentHistory = getSearchHistory()
        val newHistory = (listOf(query) + currentHistory)
            .distinct()
            .take(MAX_HISTORY_SIZE)

        saveHistory(newHistory)
    }

    suspend fun clearHistory() {
        saveHistory(emptyList())
    }

    private suspend fun saveHistory(history: List<String>) {
        val historyString = history.joinToString("||")
        context.dataStore.edit { preferences ->
            preferences[HISTORY_KEY] = historyString
        }
    }
}