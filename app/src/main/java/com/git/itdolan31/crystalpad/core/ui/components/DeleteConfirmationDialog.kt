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

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.git.itdolan31.crystalpad.R

@Composable
fun DeleteConfirmationDialog(
    trashEnabled: Boolean,
    onDismiss: () -> Unit, onConfirm: () -> Unit
) {
    AlertDialog(onDismissRequest = onDismiss, title = {
        Text(
            stringResource(
                if (trashEnabled) {
                    R.string.move_to_trash_title
                } else {
                    R.string.delete_permanently_title
                }
            )
        )
    }, text = {
        Text(
            stringResource(
                if (trashEnabled) {
                    R.string.move_to_trash_message
                } else {
                    R.string.delete_permanently_message
                }
            )
        )
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(stringResource(R.string.cancel))
        }
    }, confirmButton = {
        TextButton(onClick = onConfirm) {
            Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
        }
    })
}