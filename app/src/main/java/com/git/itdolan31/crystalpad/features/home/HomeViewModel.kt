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
package com.git.itdolan31.crystalpad.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.git.itdolan31.crystalpad.core.data.local.room.entities.NoteEntity
import com.git.itdolan31.crystalpad.core.data.repository.NoteRepository
import com.git.itdolan31.crystalpad.core.data.repository.SettingsRepository
import com.git.itdolan31.crystalpad.core.domain.model.DatePatternType
import com.git.itdolan31.crystalpad.core.domain.model.NoteSortType
import com.git.itdolan31.crystalpad.core.domain.model.SettingsConstants
import com.git.itdolan31.crystalpad.core.domain.model.TimePatternType
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val sortType: StateFlow<NoteSortType> = settingsRepository.sortTypeFlow
        .map { name ->
            NoteSortType.entries.find { it.name == name } ?: SettingsConstants.DEFAULT_SORT_TYPE
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = NoteSortType.DATE_DESC
        )

    val notes: StateFlow<List<NoteEntity>> = sortType
        .flatMapLatest { selectedType ->
            noteRepository.getNotes(selectedType)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val datePattern = settingsRepository.datePatternFlow
        .map { name ->
            DatePatternType.entries.find { it.name == name }
                ?: SettingsConstants.DEFAULT_DATE_PATTERN
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsConstants.DEFAULT_DATE_PATTERN
        )

    val timePattern = settingsRepository.timePatternFlow
        .map { name ->
            TimePatternType.entries.find { it.name == name }
                ?: SettingsConstants.DEFAULT_TIME_PATTERN
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsConstants.DEFAULT_TIME_PATTERN
        )

    fun onSortTypeChange(type: NoteSortType) {
        viewModelScope.launch {
            settingsRepository.saveSortType(type)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteRepository.delete(note)
        }
    }
}
