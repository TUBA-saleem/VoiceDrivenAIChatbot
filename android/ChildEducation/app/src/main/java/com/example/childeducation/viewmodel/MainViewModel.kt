package com.example.childeducation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.childeducation.data.FirestoreHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HistoryEntry(val entry: String, val timestamp: Long)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val darkModeKey = booleanPreferencesKey("dark_mode_enabled")
    private val dataStore = application.dataStore

    val isDarkMode: StateFlow<Boolean> = dataStore.data
        .map { it[darkModeKey] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    private val _selectedLanguage = MutableStateFlow<String>("")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val history: StateFlow<List<HistoryEntry>> = _history.asStateFlow()

    init {
        fetchLanguage()
        fetchHistory()
    }

    fun fetchLanguage() {
        viewModelScope.launch {
            val language = FirestoreHelper.getLanguage() ?: "English"
            _selectedLanguage.value = language
        }
    }

    fun setLanguage(language: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            FirestoreHelper.saveLanguage(language)
            _selectedLanguage.value = language
            addHistory("Language set to $language")
            onSuccess()  // Call the callback to show Snackbar
        }
    }

    private fun addHistory(entry: String) {
        viewModelScope.launch {
            FirestoreHelper.saveHistoryEntry(entry)
        }
    }

    fun fetchHistory() {
        viewModelScope.launch {
            FirestoreHelper.getHistoryFlow()
                .collect { entries -> _history.value = entries }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            FirestoreHelper.clearAllHistory()
        }
    }
}
