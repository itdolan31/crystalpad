package com.git.itdolan31.crystalpad.feature.note_edit.components

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.git.itdolan31.crystalpad.R

@Composable
fun ImageNameDialog(
    uri: Uri,
    error: String?, onDismiss: () -> Unit, onConfirm: (String) -> Unit
) {
    val context = LocalContext.current

    val fileNameState = rememberTextFieldState()
    val fileNameFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        fileNameFocusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(fileNameState.text.toString()) }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = {
            Text(stringResource(R.string.file_name))
        },
        text = {
            OutlinedTextField(
                state = fileNameState,
                modifier = Modifier.focusRequester(fileNameFocusRequester),
                suffix = {
                    Text(
                        ".${
                            context.contentResolver.getType(uri)?.let {
                                MimeTypeMap.getSingleton().getExtensionFromMimeType(it)
                            }?.lowercase() ?: "jpg"
                        }"
                    )
                },
                supportingText = { error?.let { Text(it) } },
                isError = error != null,
                lineLimits = TextFieldLineLimits.SingleLine
            )
        },
    )
}
