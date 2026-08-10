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
package com.git.itdolan31.crystalpad.features.trash.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.git.itdolan31.crystalpad.R
import com.git.itdolan31.crystalpad.core.ui.components.DeleteConfirmationDialog
import com.git.itdolan31.crystalpad.core.ui.components.NoteInfoDialog
import com.git.itdolan31.crystalpad.core.ui.components.NoteItem
import com.git.itdolan31.crystalpad.features.trash.presentation.components.ClearTrashDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    viewModel: TrashViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToNoteEdit: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showClearTrashDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showInfoDialog by rememberSaveable { mutableStateOf(false) }

    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val datePattern by viewModel.datePattern.collectAsStateWithLifecycle()
    val timePattern by viewModel.timePattern.collectAsStateWithLifecycle()
    val fontSize by viewModel.fontSize.collectAsStateWithLifecycle()
    val trashRetention by viewModel.trashRetention.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(navigationIcon = {
                IconButton(
                    onClick = onBack
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }, title = { Text(stringResource(R.string.trash)) }, actions = {
                IconButton(
                    onClick = { showClearTrashDialog = true }, enabled = notes.isNotEmpty()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete_sweep),
                        contentDescription = stringResource(R.string.trash_clear_all)
                    )
                }
            })
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
                    trashRetention = trashRetention,
                    datePattern = datePattern,
                    timePattern = timePattern,
                    onRestoreClick = {
                        viewModel.restoreNote(note.id)
                    },
                    onDeleteClick = {
                        viewModel.setSelectedNote(note)
                        showDeleteDialog = true
                    },
                    onInfoClick = {
                        viewModel.setSelectedNote(note)
                        showInfoDialog = true
                    }
                )
            }
        }
    }

    if (showClearTrashDialog) {
        ClearTrashDialog(onDismiss = { showClearTrashDialog = false }, onConfirm = {
            showClearTrashDialog = false
            viewModel.clearTrash()
        })
    } else if (showDeleteDialog && uiState.selectedNote != null) {
        DeleteConfirmationDialog(trashEnabled = false, onDismiss = {
            showDeleteDialog = false
            viewModel.setSelectedNote(null)
        }, onConfirm = {
            uiState.selectedNote?.let { viewModel.deleteNote(it) }
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