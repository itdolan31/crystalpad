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
package com.git.itdolan31.crystalpad

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.git.itdolan31.crystalpad.core.data.repository.NoteRepository
import com.git.itdolan31.crystalpad.core.data.repository.SettingsRepository
import com.git.itdolan31.crystalpad.domain.model.SettingsConstants
import com.git.itdolan31.crystalpad.domain.model.ThemeType
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class MainViewModel @Inject constructor(
    private val noteRepository: NoteRepository, private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val TAG = javaClass.simpleName

    private var lockJob: Job? = null

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    var isLoading by mutableStateOf(true)
        private set

    val theme: StateFlow<ThemeType> = settingsRepository.themeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_THEME
    )

    val isFlagSecureEnabled: StateFlow<Boolean> = settingsRepository.flagSecureFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_FLAG_SECURE
    )

    val dynamicColorEnabled: StateFlow<Boolean> = settingsRepository.dynamicColorFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_DYNAMIC_COLOR
    )

    init {
        viewModelScope.launch {
            settingsRepository.migrateTrashRetentionIfNeeded()

            _isLocked.value = combine(
                settingsRepository.themeFlow,
                settingsRepository.dynamicColorFlow,
                settingsRepository.passwordFlow
            ) { _, _, password ->
                password
            }.first().isNotEmpty()
            isLoading = false
        }
    }

    fun unlock() {
        _isLocked.value = false
    }

    fun cleanExpiredTrash() {
        viewModelScope.launch {
            try {
                val isTrashEnabled = settingsRepository.trashFlow.first()

                if (isTrashEnabled) {
                    val trashRetention = settingsRepository.trashRetentionFlow.first()

                    noteRepository.deleteExpiredTrashedNotes(trashRetention)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear expired trash", e)
            }
        }
    }

    fun scheduleLockEnabled(enabled: Boolean) {
        if (enabled) {
            lockJob = viewModelScope.launch {
                if (settingsRepository.passwordFlow.first().isNotEmpty()) {
                    val lockTimeout = settingsRepository.timeoutFlow.first()

                    if (lockTimeout > 0L) {
                        delay(lockTimeout.seconds)
                        _isLocked.value = true
                    } else if (lockTimeout == 0L) {
                        _isLocked.value = true
                    }
                }
            }
        } else {
            lockJob?.cancel()
            lockJob = null
        }
    }
}