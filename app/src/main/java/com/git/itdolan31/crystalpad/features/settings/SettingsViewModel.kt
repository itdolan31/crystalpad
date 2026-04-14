package com.git.itdolan31.crystalpad.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.git.itdolan31.crystalpad.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    val themeState: StateFlow<String> = repository.themeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "system"
        )

    val languageState: StateFlow<String> = repository.languageFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "system"
        )

    fun onThemeSelected(newTheme: String) {
        viewModelScope.launch {
            repository.saveTheme(newTheme)
        }
    }

    fun onLanguageSelected(newLang: String) {
        viewModelScope.launch {
            repository.saveLanguage(newLang)
        }
    }
}
