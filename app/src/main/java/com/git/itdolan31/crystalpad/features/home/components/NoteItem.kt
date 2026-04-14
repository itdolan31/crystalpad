package com.git.itdolan31.crystalpad.features.home.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.git.itdolan31.crystalpad.R
import com.git.itdolan31.crystalpad.core.localization.Translator
import com.git.itdolan31.crystalpad.data.local.room.entities.NoteEntity
import com.git.itdolan31.crystalpad.ui.components.DeleteConfirmationDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoteItem(note: NoteEntity, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .border(
                width = 0.8.dp,
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
                    .padding(8.dp)
            ) {
                Text(
                    note.title,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Text(
                    note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 3
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_date_range),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        formatDate(note.timestamp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_schedule),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary,
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
                    onClick = { showMenu = true },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                DropdownMenu(
                    expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = {
                        Row {
                            Icon(
                                painter = painterResource(R.drawable.ic_delete),
                                contentDescription = null,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(Translator.getString("delete"))
                        }
                    }, onClick = {
                        showMenu = false
                        showDeleteDialog = true
                    })
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                onDeleteClick()
                showDeleteDialog = false
            }
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return format.format(date)
}

private fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}