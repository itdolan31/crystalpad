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
package com.git.itdolan31.crystalpad.features.note_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.git.itdolan31.crystalpad.core.data.local.room.entities.NoteEntity
import com.git.itdolan31.crystalpad.core.data.repository.NoteRepository
import com.git.itdolan31.crystalpad.core.data.repository.SettingsRepository
import com.git.itdolan31.crystalpad.core.domain.model.SettingsConstants
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

    private var note: NoteEntity? = null
    private var saveJob: Job? = null

    init {
        loadNote()
    }

    fun onTitleChange(title: String) {
        _uiState.update {
            it.copy(title = title)
        }
    }

    fun onContentChange(content: String) {
        _uiState.update {
            it.copy(content = content)
        }
    }

    fun saveNote() {
        val state = _uiState.value

        if (!state.canSave()) return

        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            val savedNote = note?.copy(
                title = state.title,
                content = state.content,
                timestamp = System.currentTimeMillis()
            ) ?: NoteEntity(
                title = state.title,
                content = state.content,
                timestamp = System.currentTimeMillis()
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
                    originalTitle = savedNote.title,
                    originalContent = savedNote.content
                )
            }
        }
    }

    fun deleteNote() {
        _uiState.update { it.copy(isDeleted = true) }
        saveJob?.cancel()
        note?.let { deletedNote ->
            viewModelScope.launch {
                noteRepository.delete(deletedNote)
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
                        title = loadedNote.title,
                        content = loadedNote.content,
                        originalTitle = loadedNote.title,
                        originalContent = loadedNote.content
                    )
                }
            }
        }
    }
}