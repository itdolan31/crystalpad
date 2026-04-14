package com.git.itdolan31.crystalpad.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataSource(private val context: Context) {

    companion object {
        private val THEME = stringPreferencesKey("theme")
        private val LANGUAGE = stringPreferencesKey("language")
        private val SORT_TYPE = stringPreferencesKey("sort_type")
    }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME] = theme
        }
    }

    val themeFlow = context.dataStore.data.map { preferences ->
        preferences[THEME] ?: "system"
    }

    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE] = language
        }
    }

    val languageFlow = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE] ?: "system"
    }

    suspend fun saveSortType(sortType: String) {
        context.dataStore.edit { preferences ->
            preferences[SORT_TYPE] = sortType
        }
    }

    val sortTypeFlow = context.dataStore.data.map { preferences ->
        preferences[SORT_TYPE] ?: "DATE_DESC"
    }
}