package com.git.itdolan31.crystalpad.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.git.itdolan31.crystalpad.core.localization.Translator

@Composable
fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Translator.getString("delete_confirmation_title")) },
        text = { Text(Translator.getString("delete_confirmation_message")) },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translator.getString("cancel"))
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(Translator.getString("delete"))
            }
        }
    )
}