package com.git.itdolan31.crystalpad.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.git.itdolan31.crystalpad.data.local.database.NoteDao
import com.git.itdolan31.crystalpad.data.local.database.NoteEntity
import com.git.itdolan31.crystalpad.manager.TranslationManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, noteDao: NoteDao) {
    val notes by noteDao.getAllNotes().collectAsState(initial = emptyList())

    val coroutineScope = rememberCoroutineScope()

    Scaffold(topBar = {
        TopAppBar(title = { Text("Crystalpad") }, actions = {
            IconButton(onClick = { navController.navigate("settings") }) {
                Icon(
                    Icons.Default.Settings, null
                )
            }
        })
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = { navController.navigate("note_edit") }, modifier = Modifier.padding(20.dp)
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
                NoteItem(
                    note,
                    onClick = { navController.navigate("note_edit/${note.id}") },
                    onDeleteClick = {
                        coroutineScope.launch {
                            noteDao.delete(note)
                        }
                    })
            }
        }
    }
}

@Composable
fun NoteItem(note: NoteEntity, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),

            ) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(5.dp)
            ) {
                Text(
                    text = note.title,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 3
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        null,
                        Modifier.size(12.dp),
                        MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        formatDate(note.timestamp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.width(5.dp))
                    Icon(
                        Icons.Default.AccessTime,
                        null,
                        Modifier.size(12.dp),
                        MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        formatTime(note.timestamp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Box {
                IconButton(
                    { showMenu = true },
                ) {
                    Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.primary)
                }
                DropdownMenu(
                    expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem({
                        Row {
                            Icon(Icons.Default.Delete, null, Modifier.padding(end = 5.dp))
                            Text(TranslationManager.getString("delete"))
                        }
                    }, {
                        showMenu = false
                        showDeleteDialog = true
                    })
                }
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            title = { Text(TranslationManager.getString("delete_confirmation_title")) },
            text = { Text(TranslationManager.getString("delete_confirmation_message")) },
            onDismissRequest = { showDeleteDialog = false },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    content = { Text(TranslationManager.getString("cancel")) })
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteClick()
                    showDeleteDialog = false
                }, content = { Text(TranslationManager.getString("delete")) })
            })
    }
}

fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return format.format(date)
}

fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}