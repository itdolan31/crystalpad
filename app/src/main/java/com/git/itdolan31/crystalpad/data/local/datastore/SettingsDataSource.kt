/*
 * Crystalpad
 * Copyright (C) 2026 itdolan31
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.git.itdolan31.crystalpad.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.git.itdolan31.crystalpad.domain.model.SettingsConstants
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataSource(private val context: Context) {

    companion object {
        private val THEME = stringPreferencesKey("theme")
        private val SORT_TYPE = stringPreferencesKey("sort_type")
        private val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
    }

    val themeFlow = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[THEME] ?: SettingsConstants.DEFAULT_THEME
        }

    val sortTypeFlow = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[SORT_TYPE] ?: SettingsConstants.DEFAULT_SORT_TYPE
        }

    val keepScreenOnFlow = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEEP_SCREEN_ON] ?: SettingsConstants.DEFAULT_KEEP_SCREEN_ON
        }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME] = theme
        }
    }

    suspend fun saveSortType(sortType: String) {
        context.dataStore.edit { preferences ->
            preferences[SORT_TYPE] = sortType
        }
    }

    suspend fun saveKeepScreenOn(keepScreenOn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEEP_SCREEN_ON] = keepScreenOn
        }
    }
}