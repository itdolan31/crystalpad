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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.git.itdolan31.crystalpad.R
import com.git.itdolan31.crystalpad.core.domain.model.NoteSortType
import com.git.itdolan31.crystalpad.features.home.components.NoteItem
import com.git.itdolan31.crystalpad.ui.components.DialogRadioItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToNoteEdit: (Long?) -> Unit
) {
    var showSortMenu by rememberSaveable { mutableStateOf(false) }

    val notes by viewModel.notes.collectAsState()
    val sortType by viewModel.sortType.collectAsState()
    val datePattern by viewModel.datePattern.collectAsState()
    val timePattern by viewModel.timePattern.collectAsState()

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
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(R.string.settings))
                        },
                        onClick = {
                            showMenu = false
                            onNavigateToSettings()
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_settings),
                                contentDescription = stringResource(R.string.settings)
                            )
                        })
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(R.string.sort_title))
                        },
                        onClick = {
                            showMenu = false
                            showSortMenu = true
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_sort),
                                contentDescription = stringResource(R.string.sort_title)
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
                NoteItem(note = note, onClick = { onNavigateToNoteEdit(note.id) }, onDeleteClick = {
                    viewModel.deleteNote(note)
                }, datePattern = datePattern, timePattern = timePattern)
            }
        }
    }

    if (showSortMenu) {
        val sorts = listOf(
            NoteSortType.DATE_DESC to stringResource(R.string.sort_date_desc),
            NoteSortType.DATE_ASC to stringResource(R.string.sort_date_asc),
            NoteSortType.TITLE_ASC to stringResource(R.string.sort_title_asc),
            NoteSortType.TITLE_DESC to stringResource(R.string.sort_title_desc)
        )

        ModalBottomSheet(
            onDismissRequest = { showSortMenu = false }) {
            Column {
                sorts.forEach { (type, text) ->
                    DialogRadioItem(
                        text = text, selected = sortType == type
                    ) {
                        viewModel.setSortType(type)
                        showSortMenu = false
                    }
                }
            }
        }
    }
}