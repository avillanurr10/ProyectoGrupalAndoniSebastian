package com.example.gamedeals.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
    }

    val notificationsEnabled: Flow<Boolean> = context.userDataStore.data
        .map { preferences ->
            preferences[NOTIFICATIONS_ENABLED] ?: true
        }

    val selectedLanguage: Flow<String> = context.userDataStore.data
        .map { preferences ->
            preferences[SELECTED_LANGUAGE] ?: "Español"
        }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.userDataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setSelectedLanguage(language: String) {
        context.userDataStore.edit { preferences ->
            preferences[SELECTED_LANGUAGE] = language
        }
    }
}
