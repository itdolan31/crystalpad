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
package com.git.itdolan31.crystalpad.core.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.git.itdolan31.crystalpad.core.domain.model.SettingsConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataSource(private val context: Context) {
    companion object {
        private val THEME = stringPreferencesKey("theme")
        private val SORT_TYPE = stringPreferencesKey("sort_type")
        private val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        private val PASSWORD = stringPreferencesKey("password")
        private val BIOMETRY = booleanPreferencesKey("biometry")
        private val TIMEOUT = intPreferencesKey("timeout")
        private val DATE_PATTERN = stringPreferencesKey("date_pattern")
        private val TIME_PATTERN = stringPreferencesKey("time_pattern")
        private val FONT_SIZE = intPreferencesKey("font_size")
        private val FLAG_SECURE = booleanPreferencesKey("flag_secure")
        private val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    }

    private fun <T> preferenceFlow(
        key: Preferences.Key<T>, defaultValue: T
    ): Flow<T> = context.dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        preferences[key] ?: defaultValue
    }

    private suspend fun <T> savePreference(
        key: Preferences.Key<T>, value: T
    ) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    val themeFlow = preferenceFlow(THEME, SettingsConstants.DEFAULT_THEME.name)
    val sortTypeFlow = preferenceFlow(SORT_TYPE, SettingsConstants.DEFAULT_SORT_TYPE.name)
    val keepScreenOnFlow = preferenceFlow(KEEP_SCREEN_ON, SettingsConstants.DEFAULT_KEEP_SCREEN_ON)
    val passwordFlow = preferenceFlow(PASSWORD, "")
    val biometryFlow = preferenceFlow(BIOMETRY, SettingsConstants.DEFAULT_BIOMETRY)
    val timeoutFlow = preferenceFlow(TIMEOUT, SettingsConstants.DEFAULT_TIMEOUT)
    val datePatternFlow = preferenceFlow(DATE_PATTERN, SettingsConstants.DEFAULT_DATE_PATTERN.name)
    val timePatternFlow = preferenceFlow(TIME_PATTERN, SettingsConstants.DEFAULT_TIME_PATTERN.name)
    val fontSizeFlow = preferenceFlow(FONT_SIZE, SettingsConstants.DEFAULT_FONT_SIZE)
    val flagSecureFlow = preferenceFlow(FLAG_SECURE, SettingsConstants.DEFAULT_FLAG_SECURE)
    val dynamicColorFlow = preferenceFlow(DYNAMIC_COLOR, SettingsConstants.DEFAULT_DYNAMIC_COLOR)

    suspend fun saveTheme(theme: String) = savePreference(THEME, theme)

    suspend fun saveSortType(sortType: String) = savePreference(SORT_TYPE, sortType)

    suspend fun saveKeepScreenOn(keepScreenOn: Boolean) =
        savePreference(KEEP_SCREEN_ON, keepScreenOn)

    suspend fun savePassword(password: String) = savePreference(PASSWORD, password)

    suspend fun saveBiometry(enabled: Boolean) = savePreference(BIOMETRY, enabled)

    suspend fun saveTimeout(seconds: Int) = savePreference(TIMEOUT, seconds)

    suspend fun saveDatePattern(pattern: String) = savePreference(DATE_PATTERN, pattern)

    suspend fun saveTimePattern(pattern: String) = savePreference(TIME_PATTERN, pattern)

    suspend fun saveFontSize(size: Int) = savePreference(FONT_SIZE, size)

    suspend fun saveFlagSecure(enabled: Boolean) = savePreference(FLAG_SECURE, enabled)

    suspend fun saveDynamicColor(enabled: Boolean) = savePreference(DYNAMIC_COLOR, enabled)
}