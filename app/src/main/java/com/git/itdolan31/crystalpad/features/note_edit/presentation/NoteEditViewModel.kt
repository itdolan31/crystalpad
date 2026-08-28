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
package com.git.itdolan31.crystalpad.features.note_edit.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.git.itdolan31.crystalpad.core.data.local.room.entity.NoteEntity
import com.git.itdolan31.crystalpad.core.data.repository.NoteRepository
import com.git.itdolan31.crystalpad.core.data.repository.SettingsRepository
import com.git.itdolan31.crystalpad.domain.model.DatePatternType
import com.git.itdolan31.crystalpad.domain.model.SettingsConstants
import com.git.itdolan31.crystalpad.domain.model.TimePatternType
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@HiltViewModel(assistedFactory = NoteEditViewModel.Factory::class)
class NoteEditViewModel @AssistedInject constructor(
    private val noteRepository: NoteRepository,
    settingsRepository: SettingsRepository,
    @Assisted private val noteId: Long?
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(noteId: Long? = null): NoteEditViewModel
    }

    private val _uiState = MutableStateFlow(NoteEditUiState())
    val uiState = _uiState.asStateFlow()

    val keepScreenOn: StateFlow<Boolean> = settingsRepository.keepScreenOnFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_KEEP_SCREEN_ON
    )

    val fontSize: StateFlow<Int> = settingsRepository.fontSizeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_FONT_SIZE
    )

    val datePattern: StateFlow<DatePatternType> = settingsRepository.datePatternFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_DATE_PATTERN
    )

    val timePattern: StateFlow<TimePatternType> = settingsRepository.timePatternFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_TIME_PATTERN
    )

    val isTrashEnabled: StateFlow<Boolean> = settingsRepository.trashFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_TRASH
    )

    val trashRetention: StateFlow<Long> = settingsRepository.trashRetentionFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_TRASH_RETENTION
    )

    val isWordWrapEnabled: StateFlow<Boolean> = settingsRepository.wordWrapFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_WORD_WRAP
    )

    var note: NoteEntity? = null
        private set
    private var saveJob: Job? = null

    init {
        loadNote()
    }

    fun saveNote() {
        val state = _uiState.value

        if (!state.canSave()) return

        val title = state.titleState.text.toString()
        val content = state.contentState.text.toString()

        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            val savedNote = note?.copy(
                title = title, content = content, updatedAt = System.currentTimeMillis()
            ) ?: NoteEntity(
                title = title,
                content = content,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            if (savedNote.id == 0L) {
                val newId = noteRepository.insert(savedNote)
                note = savedNote.copy(id = newId)
            } else {
                noteRepository.update(savedNote)
                note = savedNote
            }

            _uiState.update {
                it.copy(
                    originalTitle = savedNote.title, originalContent = savedNote.content,
                    isNewNote = false
                )
            }
        }
    }

    fun deleteNote() {
        _uiState.update { it.copy(isDeleted = true) }
        saveJob?.cancel()
        note?.let { deletedNote ->
            viewModelScope.launch {
                if (isTrashEnabled.value && !deletedNote.isTrashed) {
                    noteRepository.moveNoteToTrash(deletedNote.id)
                } else {
                    noteRepository.delete(deletedNote)
                }
            }
        }
    }

    fun restoreNote() {
        note?.let { restoredNote ->
            viewModelScope.launch {
                noteRepository.restoreNoteFromTrash(restoredNote.id)
                note = note?.copy(isTrashed = false, trashedAt = null)
            }
        }
    }

    private fun loadNote() {
        if (noteId == null) {
            _uiState.update { it.copy(isNewNote = true) }
            return
        }

        viewModelScope.launch {
            noteRepository.getNoteById(noteId)?.let { loadedNote ->
                note = loadedNote
                _uiState.update {
                    it.copy(
                        originalTitle = loadedNote.title,
                        originalContent = loadedNote.content
                    )
                }
                _uiState.value.titleState.setTextAndPlaceCursorAtEnd(loadedNote.title)
                _uiState.value.contentState.setTextAndPlaceCursorAtEnd(loadedNote.content)

                _uiState.value.titleState.undoState.clearHistory()
                _uiState.value.contentState.undoState.clearHistory()
            }
        }
    }
}