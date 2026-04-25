package com.git.itdolan31.crystalpad.features.note_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.git.itdolan31.crystalpad.data.local.room.entities.NoteEntity
import com.git.itdolan31.crystalpad.data.repository.NoteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoteEditViewModel(private val noteRepository: NoteRepository, private val noteId: Long = 0L) :
    ViewModel() {
    private val _uiState = MutableStateFlow(NoteEditUiState())
    val uiState = _uiState.asStateFlow()

    private var note: NoteEntity? = null
    private var isDeleted = false
    private var saveJob: Job? = null

    init {
        loadNote()
    }

    fun onTitleChange(title: String) {
        _uiState.update {
            it.copy(title = title)
        }
        saveNote()
    }

    fun onContentChange(content: String) {
        _uiState.update {
            it.copy(content = content)
        }
        saveNote()
    }

    fun deleteNote() {
        isDeleted = true
        saveJob?.cancel()
        note?.let { deletedNote ->
            viewModelScope.launch {
                noteRepository.delete(deletedNote)
            }
        }
    }

    private fun loadNote() {
        if (noteId == 0L) {
            note = null
            _uiState.update { NoteEditUiState() }
            return
        }
        if (note?.id == noteId) return

        viewModelScope.launch {
            noteRepository.getNoteById(noteId)?.let { loadedNote ->
                note = loadedNote
                _uiState.update { it.copy(title = loadedNote.title, content = loadedNote.content) }
            }
        }
    }

    private fun saveNote() {
        val state = _uiState.value

        if (isDeleted || note == null && state.title.isBlank() && state.content.isBlank()) return

        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            val savedNote = note?.copy(
                title = state.title.trim(),
                content = state.content.trim(),
                timestamp = System.currentTimeMillis()
            ) ?: NoteEntity(
                title = state.title.trim(),
                content = state.content.trim(),
                timestamp = System.currentTimeMillis()
            )

            if (savedNote.id == 0L) {
                val newId = noteRepository.insert(savedNote)
                note = savedNote.copy(id = newId)
            } else {
                noteRepository.update(savedNote)
                note = savedNote
            }
        }
    }
}
