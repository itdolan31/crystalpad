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

class NoteEditViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditUiState())
    val uiState = _uiState.asStateFlow()

    private var currentNote: NoteEntity? = null
    private var isDeleted = false
    private var saveJob: Job? = null

    fun loadNote(id: Long) {
        if (id == 0L) {
            currentNote = null
            _uiState.update { NoteEditUiState() }
            return
        }
        if (currentNote?.id == id) return

        viewModelScope.launch {
            repository.getNoteById(id)?.let { note ->
                currentNote = note
                _uiState.update { it.copy(title = note.title, content = note.content) }
            }
        }
    }

    fun onTitleChange(title: String) = _uiState.update { it.copy(title = title) }
    fun onContentChange(content: String) = _uiState.update { it.copy(content = content) }

    fun saveNote() {
        val state = _uiState.value
        val isNotEmpty = state.title.isNotBlank() || state.content.isNotBlank()
        val isChanged = state.title != (currentNote?.title ?: "") ||
                state.content != (currentNote?.content ?: "")

        if (isDeleted || !isChanged || (!isNotEmpty && currentNote == null)) return

        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            val noteToSave = currentNote?.copy(
                title = state.title.trim(),
                content = state.content.trim(),
                timestamp = System.currentTimeMillis()
            ) ?: NoteEntity(
                title = state.title.trim(),
                content = state.content.trim(),
                timestamp = System.currentTimeMillis()
            )

            if (noteToSave.id == 0L) {
                val newId = repository.insert(noteToSave)
                currentNote = noteToSave.copy(id = newId)
            } else {
                repository.update(noteToSave)
                currentNote = noteToSave
            }
        }
    }

    fun deleteNote() {
        isDeleted = true
        saveJob?.cancel()
        currentNote?.let { note ->
            viewModelScope.launch {
                repository.delete(note)
            }
        }
    }
}
