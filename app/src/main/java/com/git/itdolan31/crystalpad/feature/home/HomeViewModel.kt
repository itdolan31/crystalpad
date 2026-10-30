package com.git.itdolan31.crystalpad.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.git.itdolan31.crystalpad.core.data.local.room.entity.NoteEntity
import com.git.itdolan31.crystalpad.core.data.repository.NoteRepository
import com.git.itdolan31.crystalpad.core.data.repository.SettingsRepository
import com.git.itdolan31.crystalpad.domain.model.DatePatternType
import com.git.itdolan31.crystalpad.domain.model.NoteSortType
import com.git.itdolan31.crystalpad.domain.model.SettingsConstants
import com.git.itdolan31.crystalpad.domain.model.TimePatternType
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
class HomeViewModel @Inject constructor(
    private val noteRepository: NoteRepository, private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val tag = javaClass.simpleName
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val sortType: StateFlow<NoteSortType> = settingsRepository.sortTypeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsConstants.DEFAULT_SORT_TYPE
    )

    val notes: StateFlow<List<NoteEntity>> = sortType.flatMapLatest { selectedType ->
        noteRepository.getNotes(selectedType)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    val fontSize: StateFlow<Int> = settingsRepository.fontSizeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_FONT_SIZE
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

    fun setSelectedNote(note: NoteEntity?) {
        _uiState.update {
            it.copy(
                selectedNote = note
            )
        }
    }

    fun setSortType(type: NoteSortType) {
        viewModelScope.launch {
            settingsRepository.saveSortType(type)
        }
    }

    fun deleteNote(note: NoteEntity) {
        note.let { deletedNote ->
            viewModelScope.launch {
                if (isTrashEnabled.value) {
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
}