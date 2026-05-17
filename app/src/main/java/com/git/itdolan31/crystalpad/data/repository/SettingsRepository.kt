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
package com.git.itdolan31.crystalpad.data.repository

import com.git.itdolan31.crystalpad.data.local.datastore.SettingsDataSource
import com.git.itdolan31.crystalpad.domain.model.NoteSortType
import kotlinx.coroutines.flow.Flow

class SettingsRepository(
    private val settingsDataSource: SettingsDataSource
) {
    val themeFlow: Flow<String> = settingsDataSource.themeFlow
    val sortTypeFlow: Flow<String> = settingsDataSource.sortTypeFlow
    val keepScreenOnFlow: Flow<Boolean> = settingsDataSource.keepScreenOnFlow

    suspend fun saveTheme(theme: String) {
        settingsDataSource.saveTheme(theme)
    }

    suspend fun saveSortType(sortType: NoteSortType) {
        settingsDataSource.saveSortType(sortType.name)
    }

    suspend fun saveKeepScreenOn(keepScreenOn: Boolean) {
        settingsDataSource.saveKeepScreenOn(keepScreenOn)
    }
}
