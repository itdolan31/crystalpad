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
package com.git.itdolan31.crystalpad.features.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.git.itdolan31.crystalpad.R
import com.git.itdolan31.crystalpad.data.repository.SettingsRepository
import com.git.itdolan31.crystalpad.domain.model.SettingsConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val password: StateFlow<String> = settingsRepository.passwordFlow.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = ""
    )

    val theme: StateFlow<String> = settingsRepository.themeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_THEME
    )

    val isKeepScreenOn: StateFlow<Boolean> = settingsRepository.keepScreenOnFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_KEEP_SCREEN_ON
    )

    val isPasswordSet: StateFlow<Boolean> = password.map { it.isNotEmpty() }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = false
    )

    val isBiometryEnabled: StateFlow<Boolean> = settingsRepository.biometryFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_BIOMETRY
    )

    val lockTimeout: StateFlow<Int> = settingsRepository.timeoutFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_TIMEOUT
    )

    fun getEffectiveLocaleDisplayName(): String {
        val locales = AppCompatDelegate.getApplicationLocales()

        return if (locales.isEmpty) {
            Locale.getDefault()
        } else {
            locales.get(0) ?: Locale.getDefault()
        }.displayLanguage
    }


    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.saveTheme(theme)
        }
    }

    fun setKeepScreenOn(keepScreenOn: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveKeepScreenOn(keepScreenOn)
        }
    }

    private fun setPassword(password: String) {
        viewModelScope.launch {
            settingsRepository.savePassword(password)
        }
    }

    fun setBiometryEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveBiometry(enabled)
        }
    }

    fun setLockTimeout(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.saveTimeout(seconds)
        }
    }

    fun setNewPassword(newPassword: String, confirmPassword: String): Pair<Boolean, Int> {
        return when {
            newPassword.length < 4 -> {
                false to R.string.password_min_length
            }

            newPassword != confirmPassword -> {
                false to R.string.password_mismatch
            }

            else -> {
                setPassword(newPassword)
                true to R.string.password_set_success
            }
        }
    }

    fun resetPassword(currentPassword: String): Pair<Boolean, Int> {
        return if (currentPassword == password.value) {
            setPassword("")
            true to R.string.password_reset_success
        } else {
            false to R.string.password_incorrect
        }
    }
}