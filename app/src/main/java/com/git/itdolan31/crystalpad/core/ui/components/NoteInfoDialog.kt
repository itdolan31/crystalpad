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
package com.git.itdolan31.crystalpad.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.git.itdolan31.crystalpad.R
import com.git.itdolan31.crystalpad.core.data.local.room.entity.NoteEntity
import com.git.itdolan31.crystalpad.core.utils.formatDateTime
import com.git.itdolan31.crystalpad.domain.model.DatePatternType
import com.git.itdolan31.crystalpad.domain.model.TimePatternType

@Composable
fun NoteInfoDialog(
    onClick: () -> Unit,
    note: NoteEntity,
    datePattern: DatePatternType,
    timePattern: TimePatternType,
    fontSize: Int,
    trashRetention: Long
) {
    AlertDialog(
        onDismissRequest = onClick,
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = "${note.id}",
                    onValueChange = {},
                    readOnly = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = fontSize.sp, lineHeight = (fontSize * 1.5).sp
                    ),
                    label = { Text("ID") })

                OutlinedTextField(
                    value = "${
                        formatDateTime(
                            note.createdAt, datePattern.pattern
                        )
                    } ${formatDateTime(note.createdAt, timePattern.pattern)}",
                    onValueChange = {},
                    readOnly = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = fontSize.sp, lineHeight = (fontSize * 1.5).sp
                    ),
                    label = { Text(stringResource(R.string.created_at)) })

                OutlinedTextField(
                    value = "${
                        formatDateTime(
                            note.updatedAt, datePattern.pattern
                        )
                    } ${formatDateTime(note.updatedAt, timePattern.pattern)}",
                    onValueChange = {},
                    readOnly = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = fontSize.sp, lineHeight = (fontSize * 1.5).sp
                    ),
                    label = { Text(stringResource(R.string.updated_at)) })

                note.trashedAt?.let { trashedAt ->
                    OutlinedTextField(
                        value = "${
                            formatDateTime(
                                trashedAt + trashRetention,
                                datePattern.pattern
                            )
                        } ${
                            formatDateTime(
                                trashedAt, timePattern.pattern
                            )
                        }",
                        onValueChange = {},
                        readOnly = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = fontSize.sp, lineHeight = (fontSize * 1.5).sp
                        ),
                        label = { Text(stringResource(R.string.trashed_at)) })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClick) {
                Text("OK")
            }
        })
}