package com.git.itdolan31.crystalpad.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class MainViewModel @Inject constructor(
    private val noteRepository: NoteRepository, private val settingsRepository: SettingsRepository
) : ViewModel() {
    private var lockJob: Job? = null

    private val _isLocked = MutableStateFlow(false)

    var isThemeLoaded by mutableStateOf(false)
        private set
    var isDynamicColorLoaded by mutableStateOf(false)
        private set

    var isPasswordLoaded by mutableStateOf(false)
        private set

    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    val themeType: StateFlow<ThemeType> = settingsRepository.themeFlow.onEach {
        isThemeLoaded = true
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_THEME
    )

    val isFlagSecureEnabled: StateFlow<Boolean> = settingsRepository.flagSecureFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_FLAG_SECURE
    )

    val dynamicColorEnabled: StateFlow<Boolean> =
        settingsRepository.dynamicColorFlow.onEach { isDynamicColorLoaded = true }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsConstants.DEFAULT_DYNAMIC_COLOR
        )

    var isPasswordSet = false
        private set
    var lockTimeout by mutableIntStateOf(SettingsConstants.DEFAULT_TIMEOUT)
        private set

    init {
        viewModelScope.launch {
            _isLocked.value = settingsRepository.passwordFlow.first().isNotEmpty()
            isPasswordLoaded = true

            settingsRepository.passwordFlow.collect { password ->
                isPasswordSet = password.isNotEmpty()
            }
        }

        viewModelScope.launch {
            settingsRepository.timeoutFlow.collect { timeout ->
                lockTimeout = timeout
            }
        }
    }

    fun unlock() {
        _isLocked.value = false
    }

    fun cleanExpiredTrash() {
        viewModelScope.launch {
            val isTrashEnabled = settingsRepository.trashFlow.first()
            val trashRetention = settingsRepository.trashRetentionFlow.first()

            if (isTrashEnabled) {
                noteRepository.deleteExpiredTrashedNotes(trashRetention)
            }
        }
    }

    fun scheduleLockEnabled(enabled: Boolean) {
        if (enabled) {
            lockJob = viewModelScope.launch {
                if (isPasswordSet) {
                    if (lockTimeout > 0) {
                        delay((lockTimeout * 1000L).milliseconds)
                        _isLocked.value = true
                    } else if (lockTimeout == 0) {
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