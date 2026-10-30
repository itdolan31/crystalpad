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
package com.git.itdolan31.crystalpad.feature.note_edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.git.itdolan31.crystalpad.R
import com.git.itdolan31.crystalpad.core.ui.components.DeleteConfirmationDialog
import com.git.itdolan31.crystalpad.core.ui.components.NoteInfoDialog
import com.git.itdolan31.crystalpad.feature.note_edit.components.ImageNameDialog
import com.git.itdolan31.crystalpad.feature.note_edit.components.ImportImageMenu
import com.git.itdolan31.crystalpad.feature.note_edit.markdown.Markdown
import kotlinx.coroutines.launch
import java.io.File

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
fun NoteEditScreen(
    viewModel: NoteEditViewModel, onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()
    val datePattern by viewModel.datePattern.collectAsStateWithLifecycle()
    val timePattern by viewModel.timePattern.collectAsStateWithLifecycle()
    val fontSize by viewModel.fontSize.collectAsStateWithLifecycle()
    val isTrashEnabled by viewModel.isTrashEnabled.collectAsStateWithLifecycle()
    val trashRetention by viewModel.trashRetention.collectAsStateWithLifecycle()
    val isWordWrapEnabled by viewModel.isWordWrapEnabled.collectAsStateWithLifecycle()

    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var isPreviewMode by rememberSaveable { mutableStateOf(false) }
    var showMenu by rememberSaveable { mutableStateOf(false) }
    var showImportImageMenu by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showInfoDialog by rememberSaveable { mutableStateOf(false) }
    var showImageNameDialog by rememberSaveable { mutableStateOf(false) }
    var previewImage by rememberSaveable { mutableStateOf<File?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val importImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            pendingUri = it
            showImageNameDialog = true
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        viewModel.saveNote()
    }

    Scaffold(
        modifier = Modifier
            .imePadding()
            .then(if (keepScreenOn) Modifier.keepScreenOn() else Modifier), topBar = {
            TopAppBar(navigationIcon = {
                IconButton(
                    onClick = onBack
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }, title = {}, actions = {
                IconButton(
                    onClick = { uiState.contentState.undoState.undo() },
                    enabled = uiState.contentState.undoState.canUndo
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_undo),
                        contentDescription = stringResource(R.string.undo)
                    )
                }
                IconButton(
                    onClick = { uiState.contentState.undoState.redo() },
                    enabled = uiState.contentState.undoState.canRedo
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_redo),
                        contentDescription = stringResource(R.string.redo)
                    )
                }
                IconButton(
                    onClick = {
                        viewModel.saveNote()
                    }, enabled = uiState.canSave()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_save),
                        contentDescription = stringResource(R.string.save)
                    )
                }
                IconButton(
                    onClick = {
                        isPreviewMode = !isPreviewMode
                    }) {
                    Icon(
                        painter = if (isPreviewMode) {
                            painterResource(R.drawable.ic_edit)
                        } else {
                            painterResource(R.drawable.ic_visibility)
                        }, contentDescription = if (isPreviewMode) {
                            stringResource(R.string.editor)
                        } else {
                            stringResource(R.string.preview)
                        }
                    )
                }
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_more_vert),
                            contentDescription = stringResource(R.string.more_options)
                        )
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        if (uiState.note?.isTrashed == true) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.restore)) },
                                onClick = {
                                    showMenu = false
                                    viewModel.restoreNote()
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_restore_from_trash),
                                        contentDescription = null
                                    )
                                })
                        }

                        if (uiState.note != null) {
                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.import_photo))
                                }, onClick = {
                                    showMenu = false
                                    showImportImageMenu = true
                                }, leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_add_photo_alternate),
                                        contentDescription = null
                                    )
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete)) },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_delete),
                                    contentDescription = null
                                )
                            })

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.info)) }, onClick = {
                                showMenu = false
                                showInfoDialog = true
                            }, leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_info),
                                    contentDescription = null
                                )
                            }, enabled = uiState.note != null
                        )
                    }
                }
            })
        }, snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState, modifier = Modifier.padding(8.dp, 0.dp)
            )
        }
    ) { innerPadding ->
        val previewListState = rememberLazyListState()
        val editorScrollState = rememberScrollState()

        Crossfade(targetState = isPreviewMode, label = "preview") { previewMode ->
            if (previewMode) {
                SelectionContainer {
                    Markdown(
                        uiState.contentState.text.toString(),
                        noteId = uiState.note?.id,
                        state = previewListState,
                        contentPadding = innerPadding,
                        onToggleTask = { sourceLine ->
                            val lines = uiState.contentState.text.split("\n").toMutableList()
                            if (sourceLine in lines.indices) {
                                val line = lines[sourceLine]
                                lines[sourceLine] = when {
                                    line.contains("[ ]") -> line.replaceFirst("[ ]", "[x]")
                                    line.contains("[x]") -> line.replaceFirst("[x]", "[ ]")
                                    else -> line
                                }
                                val newText = lines.joinToString("\n")
                                uiState.contentState.setTextAndPlaceCursorAtEnd(newText)
                            }
                        }
                    )
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(editorScrollState)
                        .padding(innerPadding)
                ) {
                    TextField(
                        state = uiState.titleState,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = fontSize.sp,
                            textDirection = TextDirection.Content,
                            lineHeight = (fontSize * 1.5).sp
                        ),
                        placeholder = {
                            Text(
                                stringResource(R.string.title), style = LocalTextStyle.current.copy(
                                    fontSize = fontSize.sp, textDirection = TextDirection.Content
                                )
                            )
                        },
                        lineLimits = TextFieldLineLimits.SingleLine,
                        shape = RectangleShape,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    TextField(
                        state = uiState.contentState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isWordWrapEnabled) Modifier else Modifier.horizontalScroll(
                                    rememberScrollState()
                                )
                            ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = fontSize.sp,
                            textDirection = TextDirection.Content,
                            lineHeight = (fontSize * 1.5).sp
                        ),
                        placeholder = {
                            Text(
                                stringResource(R.string.note), style = LocalTextStyle.current.copy(
                                    fontSize = fontSize.sp, textDirection = TextDirection.Content
                                )
                            )
                        },
                        lineLimits = TextFieldLineLimits.MultiLine(30),
                        shape = RectangleShape,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface
                        ),
                    )
                }
            }
        }
    }

    if (showImportImageMenu) {
        ImportImageMenu(
            onDismiss = {
                showImportImageMenu = false
                viewModel.clearImageSelection()
            },
            onDeleteSelected = viewModel::deleteSelectedImages,
            onAddPhoto = {
                importImageLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onImageClick = { previewImage = it },
            onToggleSelection = viewModel::toggleImageSelection,
            images = uiState.images,
            selectedImages = uiState.selectedImages
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            trashEnabled = isTrashEnabled && uiState.note?.isTrashed == false,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deleteNote()
                onBack()
                showDeleteDialog = false
            })
    } else if (showInfoDialog && uiState.note != null) {
        uiState.note?.let {
            NoteInfoDialog(
                onClick = { showInfoDialog = false },
                note = it,
                datePattern = datePattern,
                timePattern = timePattern,
                fontSize = fontSize,
                trashRetention = trashRetention
            )
        }
    } else if (showImageNameDialog && pendingUri != null) {
        pendingUri?.let { uri ->
            var fileNameError by remember { mutableStateOf<String?>(null) }

            ImageNameDialog(
                uri = uri,
                error = fileNameError,
                onDismiss = {
                    showImageNameDialog = false
                    pendingUri = null
                },
                onConfirm = { name ->
                    pendingUri?.let {
                        coroutineScope.launch {
                            val (success, message) = viewModel.importImage(uri, name)

                            if (success) {
                                showImageNameDialog = false
                                pendingUri = null
                                snackbarHostState.showSnackbar(message)
                            } else {
                                fileNameError = message
                            }
                        }
                    }
                }
            )
        }
    } else if (previewImage != null) {
        previewImage?.let {
            AlertDialog(onDismissRequest = { previewImage = null }, confirmButton = {
                TextButton(onClick = { previewImage = null }) {
                    Text("OK")
                }
            }, text = { SelectionContainer { Text(it.name) } })
        }
    }
}