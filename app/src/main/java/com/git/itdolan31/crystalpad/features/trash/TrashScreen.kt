package com.git.itdolan31.crystalpad.features.trash

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
import com.git.itdolan31.crystalpad.features.trash.components.ClearTrashDialog
import com.git.itdolan31.crystalpad.ui.components.DeleteConfirmationDialog
import com.git.itdolan31.crystalpad.ui.components.NoteInfoDialog
import com.git.itdolan31.crystalpad.ui.components.NoteItem

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