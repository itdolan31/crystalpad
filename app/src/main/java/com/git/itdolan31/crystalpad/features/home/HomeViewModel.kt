package com.git.itdolan31.crystalpad.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.git.itdolan31.crystalpad.data.local.room.entities.NoteEntity
import com.git.itdolan31.crystalpad.data.repository.NoteRepository
import com.git.itdolan31.crystalpad.data.repository.SettingsRepository
import com.git.itdolan31.crystalpad.domain.model.NoteSortType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: NoteRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _sortType = settingsRepository.sortTypeFlow
        .map { name ->
            NoteSortType.entries.find { it.name == name } ?: NoteSortType.DATE_DESC
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, NoteSortType.DATE_DESC)

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<NoteEntity>> = _sortType
        .flatMapLatest { selectedType ->
            repository.getNotes(selectedType)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentSortType: StateFlow<NoteSortType> = _sortType

    fun onSortTypeChange(type: NoteSortType) {
        viewModelScope.launch {
            settingsRepository.saveSortType(type)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }
}
