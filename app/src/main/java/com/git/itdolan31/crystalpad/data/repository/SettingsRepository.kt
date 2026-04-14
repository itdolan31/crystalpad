package com.git.itdolan31.crystalpad.data.repository

import com.git.itdolan31.crystalpad.data.local.datastore.SettingsDataSource
import com.git.itdolan31.crystalpad.domain.model.NoteSortType
import kotlinx.coroutines.flow.Flow

class SettingsRepository(
    private val settingsDataSource: SettingsDataSource
) {
    val themeFlow: Flow<String> = settingsDataSource.themeFlow
    val languageFlow: Flow<String> = settingsDataSource.languageFlow
    val sortTypeFlow: Flow<String> = settingsDataSource.sortTypeFlow

    suspend fun saveTheme(theme: String) {
        settingsDataSource.saveTheme(theme)
    }

    suspend fun saveLanguage(language: String) {
        settingsDataSource.saveLanguage(language)
    }

    suspend fun saveSortType(sortType: NoteSortType) {
        settingsDataSource.saveSortType(sortType.name)
    }
}
