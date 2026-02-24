package com.git.itdolan31.crystalpad.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.git.itdolan31.crystalpad.data.local.database.NoteDao
import com.git.itdolan31.crystalpad.data.local.database.NoteEntity
import com.git.itdolan31.crystalpad.manager.TranslationManager
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun NoteEditScreen(navController: NavController, noteDao: NoteDao, noteId: Long = 0) {
    var noteId by rememberSaveable { mutableLongStateOf(noteId) }

    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }

    var isModified by rememberSaveable { mutableStateOf(false) }

    var originalTitle by rememberSaveable { mutableStateOf("") }
    var originalContent by rememberSaveable { mutableStateOf("") }

    var showMenu by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(noteId) {
        if (noteId > 0L) {
            val note = noteDao.getNoteById(noteId)
            note?.let {
                title = it.title
                content = it.content
                originalTitle = it.title
                originalContent = it.content
            }
        }
    }

    LaunchedEffect(title, content) {
        isModified = if (noteId > 0L) {
            title != originalTitle || content != originalContent
        } else {
            title.isNotBlank() || content.isNotBlank()
        }
    }

    fun saveNote() {
        coroutineScope.launch {
            if (noteId == 0L && (title.isNotBlank() || content.isNotBlank())) {
                val newId = noteDao.insert(NoteEntity(title = title, content = content))
                noteId = newId

                originalTitle = title
                originalContent = content
            } else if (isModified) {
                val note = noteDao.getNoteById(noteId)

                note?.let {
                    noteDao.update(
                        it.copy(
                            title = title, content = content, timestamp = System.currentTimeMillis()
                        )
                    )
                }
                originalTitle = title
                originalContent = content
            }
            isModified = false
        }
    }

    fun deleteNote() {
        coroutineScope.launch {
            if (noteId > 0L) {
                val note = noteDao.getNoteById(noteId)
                note?.let { noteDao.delete(it) }
            }
            navController.popBackStack()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP,
                Lifecycle.Event.ON_DESTROY ->
                    coroutineScope.launch {
                        saveNote()
                    }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(navigationIcon = {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, null
                    )
                }
            }, title = {}, actions = {
                Box {
                    IconButton(onClick = {
                        showMenu = true
                    }) {
                        Icon(
                            Icons.Default.MoreVert, null
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row {
                                    Icon(Icons.Default.Delete, null, Modifier.padding(end = 5.dp))
                                    Text(TranslationManager.getString("delete"))
                                }
                            },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            })
                        DropdownMenuItem(
                            text = {
                                Row {
                                    Icon(Icons.Default.Save, null, Modifier.padding(end = 5.dp))
                                    Text(TranslationManager.getString("save"))
                                }
                            },
                            onClick = {
                                showMenu = false
                                saveNote()
                            })
                    }
                }

            })
        }) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                textStyle = MaterialTheme.typography.titleMedium,
                maxLines = Int.MAX_VALUE,
                shape = RectangleShape,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface
                ),
                placeholder = { Text(TranslationManager.getString("title")) },
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(5.dp))
            TextField(
                value = content,
                onValueChange = { content = it },
                textStyle = MaterialTheme.typography.bodyMedium,
                minLines = 10,
                maxLines = Int.MAX_VALUE,
                shape = RectangleShape,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface
                ),
                placeholder = { Text(TranslationManager.getString("note")) },
                modifier = Modifier
                    .fillMaxWidth()

            )
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
                        TextButton(
                            onClick = {
                                deleteNote()
                                showDeleteDialog = false
                            },
                            content = { Text(TranslationManager.getString("delete")) })
                    })
            }
        }
    }
}