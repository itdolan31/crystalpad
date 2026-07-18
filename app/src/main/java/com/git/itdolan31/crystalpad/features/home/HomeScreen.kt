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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.git.itdolan31.crystalpad.R
import com.git.itdolan31.crystalpad.core.domain.model.NoteSortType
import com.git.itdolan31.crystalpad.ui.components.DeleteConfirmationDialog
import com.git.itdolan31.crystalpad.ui.components.DialogRadioItem
import com.git.itdolan31.crystalpad.ui.components.NoteInfoDialog
import com.git.itdolan31.crystalpad.ui.components.NoteItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToTrash: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNoteEdit: (Long?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSortMenu by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showInfoDialog by rememberSaveable { mutableStateOf(false) }

    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val sortType by viewModel.sortType.collectAsStateWithLifecycle()
    val datePattern by viewModel.datePattern.collectAsStateWithLifecycle()
    val timePattern by viewModel.timePattern.collectAsStateWithLifecycle()
    val fontSize by viewModel.fontSize.collectAsStateWithLifecycle()
    val isTrashEnabled by viewModel.isTrashEnabled.collectAsStateWithLifecycle()
    val trashRetention by viewModel.trashRetention.collectAsStateWithLifecycle()

    Scaffold(topBar = {
        TopAppBar(title = { Text("Crystalpad") }, actions = {
            var showMenu by rememberSaveable { mutableStateOf(false) }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = stringResource(R.string.more_options)
                    )
                }
                DropdownMenu(
                    expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    if (isTrashEnabled) {
                        DropdownMenuItem(text = {
                            Text(stringResource(R.string.trash))
                        }, onClick = {
                            showMenu = false
                            onNavigateToTrash()
                        }, leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_delete_forever),
                                contentDescription = null
                            )
                        })
                    }
                    DropdownMenuItem(text = {
                        Text(stringResource(R.string.settings))
                    }, onClick = {
                        showMenu = false
                        onNavigateToSettings()
                    }, leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings),
                            contentDescription = null
                        )
                    })
                    DropdownMenuItem(text = {
                        Text(stringResource(R.string.sort_title))
                    }, onClick = {
                        showMenu = false
                        showSortMenu = true
                    }, leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_sort), contentDescription = null
                        )
                    })
                }
            }
        })
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = { onNavigateToNoteEdit(null) }, modifier = Modifier.padding(16.dp)
        ) {
            Text("+", fontSize = 24.sp)
        }
    }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(notes, key = { it.id }) { note ->
                NoteItem(
                    onClick = { onNavigateToNoteEdit(note.id) },
                    note = note,
                    datePattern = datePattern,
                    timePattern = timePattern,
                    onDeleteClick = {
                        viewModel.setSelectedNote(note)
                        showDeleteDialog = true
                    },
                    onInfoClick = {
                        viewModel.setSelectedNote(note)
                        showInfoDialog = true
                    })
            }
        }
    }

    if (showSortMenu) {
        val sorts = remember {
            listOf(
                NoteSortType.CREATED_AT_DESC to R.string.sort_created_at_desc,
                NoteSortType.CREATED_AT_ASC to R.string.sort_created_at_asc,
                NoteSortType.UPDATED_AT_DESC to R.string.sort_updated_at_desc,
                NoteSortType.UPDATED_AT_ASC to R.string.sort_updated_at_asc,
                NoteSortType.TITLE_ASC to R.string.sort_title_asc,
                NoteSortType.TITLE_DESC to R.string.sort_title_desc
            )
        }

        ModalBottomSheet(
            onDismissRequest = { showSortMenu = false }) {
            Column {
                sorts.forEach { (type, id) ->
                    DialogRadioItem(
                        text = stringResource(id), selected = sortType == type
                    ) {
                        viewModel.setSortType(type)
                        showSortMenu = false
                    }
                }
            }
        }
    } else if (showDeleteDialog && uiState.selectedNote != null) {
        DeleteConfirmationDialog(trashEnabled = isTrashEnabled, onDismiss = {
            showDeleteDialog = false
            viewModel.setSelectedNote(null)
        }, onConfirm = {
            viewModel.deleteNote(uiState.selectedNote!!)
            showDeleteDialog = false
            viewModel.setSelectedNote(null)
        })
    } else if (showInfoDialog && uiState.selectedNote != null) {
        NoteInfoDialog(
            onClick = {
                showInfoDialog = false
                viewModel.setSelectedNote(null)
            },
            note = uiState.selectedNote!!,
            datePattern = datePattern,
            timePattern = timePattern,
            fontSize = fontSize,
            trashRetention = trashRetention
        )
    }
}