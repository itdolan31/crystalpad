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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.git.itdolan31.crystalpad.R
import com.git.itdolan31.crystalpad.core.localization.Translator
import com.git.itdolan31.crystalpad.domain.model.NoteSortType
import com.git.itdolan31.crystalpad.features.home.components.NoteItem
import com.git.itdolan31.crystalpad.ui.components.DialogRadioItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel, onNavigateToSettings: () -> Unit, onNavigateToNoteEdit: (Long) -> Unit
) {
    var showSortMenu by rememberSaveable { mutableStateOf(false) }

    val currentSortType by viewModel.currentSortType.collectAsState()
    val notes by viewModel.notes.collectAsState()

    Scaffold(topBar = {
        TopAppBar(title = { Text("Crystalpad") }, actions = {
            var showMenu by rememberSaveable { mutableStateOf(false) }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = null
                    )
                }
                DropdownMenu(
                    expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = {
                        Text(Translator.getString("settings"))
                    }, onClick = {
                        showMenu = false
                        onNavigateToSettings()
                    })
                    DropdownMenuItem(text = {
                        Text(Translator.getString("sort_title"))
                    }, onClick = {
                        showMenu = false
                        showSortMenu = true
                    })
                }
            }

        })
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = { onNavigateToNoteEdit(0L) }, modifier = Modifier.padding(20.dp)
        ) {
            Text("+", fontSize = 20.sp)
        }
    }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(notes) { note ->
                NoteItem(note = note, onClick = { onNavigateToNoteEdit(note.id) }, onDeleteClick = {
                    viewModel.deleteNote(note)
                })
            }
        }
    }

    if (showSortMenu) {
        val sorts = listOf(
            NoteSortType.DATE_DESC to Translator.getString("sort_date_desc"),
            NoteSortType.DATE_ASC to Translator.getString("sort_date_asc"),
            NoteSortType.TITLE_ASC to Translator.getString("sort_title_asc"),
            NoteSortType.TITLE_DESC to Translator.getString("sort_title_desc")
        )

        ModalBottomSheet(
            onDismissRequest = { showSortMenu = false }) {
            Column(modifier = Modifier.padding()) {
                sorts.forEach { (type, text) ->
                    DialogRadioItem(
                        text = text, selected = currentSortType == type
                    ) {
                        viewModel.onSortTypeChange(type)
                        showSortMenu = false
                    }
                }
            }
        }
    }
}