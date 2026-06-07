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
package com.git.itdolan31.crystalpad.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.git.itdolan31.crystalpad.core.data.repository.SettingsRepository
import com.git.itdolan31.crystalpad.core.domain.model.SettingsConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _isLocked = MutableStateFlow(false)
    private val _isFlagSecureEnabled = MutableStateFlow(SettingsConstants.DEFAULT_FLAG_SECURE)

    var themeLoaded by mutableStateOf(false)
        private set
    var passwordLoaded by mutableStateOf(false)
        private set

    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()
    val isFlagSecureEnabled: StateFlow<Boolean> = _isFlagSecureEnabled.asStateFlow()

    var isPasswordSet by mutableStateOf(false)
        private set
    var lockTimeout by mutableIntStateOf(SettingsConstants.DEFAULT_TIMEOUT)
        private set

    val theme: StateFlow<String> = settingsRepository.themeFlow
        .onEach { themeLoaded = true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "system"
        )

    init {
        viewModelScope.launch {
            _isLocked.value = settingsRepository.passwordFlow.first().isNotEmpty()
            passwordLoaded = true

            settingsRepository.passwordFlow.collect { password ->
                isPasswordSet = password.isNotEmpty()
            }
        }

        viewModelScope.launch {
            settingsRepository.timeoutFlow.collect { timeout ->
                lockTimeout = timeout
            }
        }

        viewModelScope.launch {
            settingsRepository.flagSecureFlow.collect { enabled ->
                _isFlagSecureEnabled.value = enabled
            }
        }
    }

    fun setLocked(locked: Boolean) {
        _isLocked.value = locked
    }
}