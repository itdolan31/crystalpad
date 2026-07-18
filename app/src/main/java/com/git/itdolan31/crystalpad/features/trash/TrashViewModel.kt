package com.git.itdolan31.crystalpad.features.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.git.itdolan31.crystalpad.core.data.local.room.entities.NoteEntity
import com.git.itdolan31.crystalpad.core.data.repository.NoteRepository
import com.git.itdolan31.crystalpad.core.data.repository.SettingsRepository
import com.git.itdolan31.crystalpad.core.domain.model.NoteSortType
import com.git.itdolan31.crystalpad.core.domain.model.SettingsConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TrashViewModel @Inject constructor(
    private val noteRepository: NoteRepository, settingsRepository: SettingsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TrashUiState())
    val uiState: StateFlow<TrashUiState> = _uiState.asStateFlow()

    val sortType: StateFlow<NoteSortType> = settingsRepository.sortTypeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsConstants.DEFAULT_SORT_TYPE
    )

    val notes: StateFlow<List<NoteEntity>> = sortType.flatMapLatest { selectedType ->
        noteRepository.getTrashedNotes(selectedType)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val datePattern = settingsRepository.datePatternFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_DATE_PATTERN
    )

    val timePattern = settingsRepository.timePatternFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_TIME_PATTERN
    )

    val fontSize: StateFlow<Int> = settingsRepository.fontSizeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_FONT_SIZE
    )

    val trashRetention: StateFlow<Long> = settingsRepository.trashRetentionFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_TRASH_RETENTION
    )

    fun setSelectedNote(note: NoteEntity?) {
        _uiState.update {
            it.copy(
                selectedNote = note
            )
        }
    }

    fun deleteNote(note: NoteEntity) {
        note.let { deletedNote ->
            viewModelScope.launch {
                noteRepository.delete(deletedNote)
            }
        }
    }

    fun restoreNote(noteId: Long) {
        viewModelScope.launch {
            noteRepository.restoreNoteFromTrash(noteId)
        }
    }

    fun clearTrash() {
        viewModelScope.launch {
            noteRepository.clearTrash()
        }
    }
}