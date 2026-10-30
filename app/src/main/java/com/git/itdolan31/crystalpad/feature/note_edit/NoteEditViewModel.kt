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
package com.git.itdolan31.crystalpad.feature.note_edit

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.git.itdolan31.crystalpad.R
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@HiltViewModel(assistedFactory = NoteEditViewModel.Factory::class)
class NoteEditViewModel @AssistedInject constructor(
    private val noteRepository: NoteRepository,
    settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context,
    @Assisted val noteId: Long?
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(noteId: Long? = null): NoteEditViewModel
    }

    private val tag = javaClass.simpleName

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
            val savedNote = state.note?.copy(
                title = title, content = content, updatedAt = System.currentTimeMillis()
            ) ?: NoteEntity(
                title = title,
                content = content,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val updatedNote = if (savedNote.id == 0L) {
                val newId = noteRepository.insert(savedNote)
                savedNote.copy(id = newId)
            } else {
                noteRepository.update(savedNote)
                savedNote
            }

            _uiState.update {
                it.copy(
                    note = updatedNote,
                    originalTitle = updatedNote.title,
                    originalContent = updatedNote.content
                )
            }
        }
    }

    fun deleteNote() {
        val state = _uiState.value

        _uiState.update { it.copy(isDeleted = true) }
        saveJob?.cancel()
        state.note?.let { deletedNote ->
            viewModelScope.launch {
                if (isTrashEnabled.value && !deletedNote.isTrashed) {
                    noteRepository.moveNoteToTrash(deletedNote.id)
                } else {
                    try {
                        noteRepository.delete(deletedNote)
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to delete note", e)
                    }
                }
            }
        }
    }

    fun restoreNote() {
        val state = _uiState.value

        state.note?.let { restoredNote ->
            viewModelScope.launch {
                noteRepository.restoreNoteFromTrash(restoredNote.id)
                _uiState.update {
                    it.copy(note = restoredNote.copy(isTrashed = false, trashedAt = null))
                }
            }
        }
    }

    private fun loadNote() {
        if (noteId == null) return

        val state = _uiState.value

        viewModelScope.launch {
            noteRepository.getNoteById(noteId)?.let { loadedNote ->
                _uiState.update {
                    it.copy(
                        note = loadedNote,
                        originalTitle = loadedNote.title,
                        originalContent = loadedNote.content
                    )
                }
                state.titleState.setTextAndPlaceCursorAtEnd(loadedNote.title)
                state.contentState.setTextAndPlaceCursorAtEnd(loadedNote.content)

                state.titleState.undoState.clearHistory()
                state.contentState.undoState.clearHistory()

                loadImages()
            }
        }
    }

    fun loadImages() {
        val id = _uiState.value.note?.id ?: return

        viewModelScope.launch {
            val dir = File(context.filesDir, "media/$id")
            if (!dir.exists()) return@launch

            val images = withContext(Dispatchers.IO) {
                dir.listFiles()?.toList() ?: emptyList()
            }

            _uiState.update { it.copy(images = images) }
        }
    }

    suspend fun importImage(uri: Uri, name: String): Pair<Boolean, String> {
        val id = _uiState.value.note?.id
            ?: return false to context.getString(R.string.error_note_not_found)
        val trimmedName = name.trim()

        if (trimmedName.isEmpty()) return false to context.getString(R.string.error_empty_name)
        if (trimmedName.contains('/') || trimmedName.contains('\\')) return false to context.getString(
            R.string.error_name_contains_slash
        )
        if (trimmedName.startsWith(".") || trimmedName.endsWith(".")) return false to context.getString(
            R.string.error_name_dot
        )

        val extension = context.contentResolver.getType(uri)
            ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
            ?.lowercase()
            ?: "jpg"

        val fileName = "$trimmedName.$extension"
        if (fileName.toByteArray(Charsets.UTF_8).size > 200) return false to context.getString(R.string.error_name_too_long)

        return withContext(Dispatchers.IO) {
            val dir = File(context.filesDir, "media/$id").apply { mkdirs() }
            val dest = File(dir, fileName)

            if (dest.exists()) return@withContext false to context.getString(R.string.error_name_already_exists)

            try {
                val input = context.contentResolver.openInputStream(uri)
                    ?: return@withContext false to context.getString(
                        R.string.error_file_not_found,
                        uri
                    )
                input.use { src ->
                    dest.outputStream().use { dst -> src.copyTo(dst) }
                }
            } catch (e: Exception) {
                dest.delete()
                return@withContext false to "$e"
            }

            _uiState.update { it.copy(images = it.images + dest) }
            true to context.getString(R.string.import_successful)
        }
    }

    fun toggleImageSelection(file: File) {
        _uiState.update { state ->
            state.copy(
                selectedImages = if (file in state.selectedImages) {
                    state.selectedImages - file
                } else {
                    state.selectedImages + file
                }
            )
        }
    }

    fun clearImageSelection() {
        _uiState.update { it.copy(selectedImages = emptyList()) }
    }

    fun deleteSelectedImages() {
        viewModelScope.launch {
            val selected = _uiState.value.selectedImages
            if (selected.isEmpty()) return@launch

            val deleted = withContext(Dispatchers.IO) {
                selected.filter { it.delete() }.toSet()
            }

            _uiState.update { state ->
                state.copy(
                    images = state.images.filterNot { it in deleted },
                    selectedImages = selected - deleted
                )
            }
        }
    }
}