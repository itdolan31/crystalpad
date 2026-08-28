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
package com.git.itdolan31.crystalpad.core.data.repository

import com.git.itdolan31.crystalpad.core.data.local.datastore.SettingsDataSource
import com.git.itdolan31.crystalpad.domain.model.DatePatternType
import com.git.itdolan31.crystalpad.domain.model.NoteSortType
import com.git.itdolan31.crystalpad.domain.model.ThemeType
import com.git.itdolan31.crystalpad.domain.model.TimePatternType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val settingsDataSource: SettingsDataSource
) {
    val themeFlow: Flow<ThemeType> = settingsDataSource.themeFlow.map { name ->
        ThemeType.fromName(name)
    }
    val sortTypeFlow: Flow<NoteSortType> = settingsDataSource.sortTypeFlow.map { name ->
        NoteSortType.fromName(name)
    }
    val keepScreenOnFlow: Flow<Boolean> = settingsDataSource.keepScreenOnFlow
    val passwordFlow: Flow<String> = settingsDataSource.passwordFlow
    val biometryFlow: Flow<Boolean> = settingsDataSource.biometryFlow
    val timeoutFlow: Flow<Int> = settingsDataSource.timeoutFlow
    val datePatternFlow: Flow<DatePatternType> = settingsDataSource.datePatternFlow.map { name ->
        DatePatternType.fromName(name)
    }
    val timePatternFlow: Flow<TimePatternType> = settingsDataSource.timePatternFlow.map { name ->
        TimePatternType.fromName(name)
    }
    val fontSizeFlow: Flow<Int> = settingsDataSource.fontSizeFlow
    val flagSecureFlow: Flow<Boolean> = settingsDataSource.flagSecureFlow
    val dynamicColorFlow: Flow<Boolean> = settingsDataSource.dynamicColorFlow
    val trashFlow: Flow<Boolean> = settingsDataSource.trashFlow
    val trashRetentionFlow: Flow<Long> = settingsDataSource.trashRetentionFlow
    val wordWrapFlow: Flow<Boolean> = settingsDataSource.wordWrapFlow

    suspend fun saveTheme(themeType: ThemeType) {
        settingsDataSource.saveTheme(themeType.name)
    }

    suspend fun saveSortType(sortType: NoteSortType) {
        settingsDataSource.saveSortType(sortType.name)
    }

    suspend fun saveKeepScreenOn(keepScreenOn: Boolean) {
        settingsDataSource.saveKeepScreenOn(keepScreenOn)
    }

    suspend fun savePassword(password: String) {
        settingsDataSource.savePassword(password)
    }

    suspend fun saveBiometry(enabled: Boolean) {
        settingsDataSource.saveBiometry(enabled)
    }

    suspend fun saveTimeout(seconds: Int) {
        settingsDataSource.saveTimeout(seconds)
    }

    suspend fun saveDatePattern(pattern: DatePatternType) {
        settingsDataSource.saveDatePattern(pattern.name)
    }

    suspend fun saveTimePattern(pattern: TimePatternType) {
        settingsDataSource.saveTimePattern(pattern.name)
    }

    suspend fun saveFontSize(size: Int) {
        settingsDataSource.saveFontSize(size)
    }

    suspend fun saveFlagSecure(enabled: Boolean) {
        settingsDataSource.saveFlagSecure(enabled)
    }

    suspend fun saveDynamicColor(enabled: Boolean) {
        settingsDataSource.saveDynamicColor(enabled)
    }

    suspend fun saveTrash(enabled: Boolean) {
        settingsDataSource.saveTrash(enabled)
    }

    suspend fun saveTrashRetention(millis: Long) {
        settingsDataSource.saveTrashRetention(millis)
    }

    suspend fun saveWordWrap(enabled: Boolean) {
        settingsDataSource.saveWordWrap(enabled)
    }
}
