package com.example.childeducation.viewmodel

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(private val context: Context) : ViewModel() {

    private val notificationKey = booleanPreferencesKey("notifications_enabled")
    private val darkModeKey = booleanPreferencesKey("dark_mode_enabled")

    val notificationsEnabled: StateFlow<Boolean> = context.dataStore.data
        .map { preferences: Preferences -> preferences[notificationKey] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    val darkModeEnabled: StateFlow<Boolean> = context.dataStore.data
        .map { preferences: Preferences -> preferences[darkModeKey] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[notificationKey] = enabled
            }
        }
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[darkModeKey] = enabled
            }
        }
    }
}
