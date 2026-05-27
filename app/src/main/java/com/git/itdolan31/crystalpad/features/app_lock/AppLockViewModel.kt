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
package com.git.itdolan31.crystalpad.features.app_lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.git.itdolan31.crystalpad.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AppLockViewModel @Inject constructor(settingsRepository: SettingsRepository) : ViewModel() {
    private val _password = MutableStateFlow("")
    private val _isBiometryEnabled = MutableStateFlow<Boolean?>(null)

    private val password: StateFlow<String> = _password.asStateFlow()
    val isBiometryEnabled: StateFlow<Boolean?> = _isBiometryEnabled.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.passwordFlow.collect { password ->
                _password.value = password
            }
        }

        viewModelScope.launch {
            settingsRepository.biometryFlow.collect { biometry ->
                _isBiometryEnabled.value = biometry
            }
        }
    }

    fun checkPassword(input: String): Boolean {
        return password.value == input
    }
}