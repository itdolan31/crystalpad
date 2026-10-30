package com.git.itdolan31.crystalpad.feature.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.git.itdolan31.crystalpad.R

@Composable
fun PasswordDialog(
    isPasswordSet: Boolean,
    fontSize: Int,
    onDismiss: () -> Unit,
    onConfirm: (password: String, confirmPassword: String) -> Unit
) {
    val passwordState = rememberTextFieldState()
    val confirmPasswordState = rememberTextFieldState()
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        passwordFocusRequester.requestFocus()
    }

    AlertDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(
            onClick = {
                onConfirm(
                    passwordState.text.toString(),
                    confirmPasswordState.text.toString()
                )
            }
        ) {
            Text("OK")
        }
    }, dismissButton = {
        TextButton(
            onClick = onDismiss
        ) {
            Text(stringResource(R.string.cancel))
        }
    }, title = {
        Text(
            if (isPasswordSet) {
                stringResource(R.string.reset_password)
            } else {
                stringResource(R.string.set_password)
            }
        )
    }, text = {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            OutlinedSecureTextField(
                state = passwordState,
                modifier = Modifier.focusRequester(passwordFocusRequester),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = fontSize.sp,
                    textDirection = TextDirection.Content,
                    lineHeight = (fontSize * 1.5).sp
                ),
                label = { Text(stringResource(R.string.password)) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = if (passwordVisible) painterResource(R.drawable.ic_visibility_off) else painterResource(
                                R.drawable.ic_visibility
                            ), contentDescription = if (passwordVisible) {
                                stringResource(R.string.hide_password)
                            } else {
                                stringResource(R.string.show_password)
                            }
                        )
                    }
                },
                textObfuscationMode = if (passwordVisible) TextObfuscationMode.Visible else TextObfuscationMode.Hidden,
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                onKeyboardAction = {
                    if (isPasswordSet) {
                        onConfirm(
                            passwordState.text.toString(),
                            confirmPasswordState.text.toString()
                        )
                    } else {
                        confirmPasswordFocusRequester.requestFocus()
                    }
                }
            )

            if (!isPasswordSet) {
                Spacer(Modifier.height(16.dp))

                OutlinedSecureTextField(
                    state = confirmPasswordState,
                    modifier = Modifier.focusRequester(confirmPasswordFocusRequester),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = fontSize.sp,
                        textDirection = TextDirection.Content,
                        lineHeight = (fontSize * 1.5).sp
                    ),
                    label = {
                        Text(
                            stringResource(R.string.confirm_password)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            confirmPasswordVisible = !confirmPasswordVisible
                        }) {
                            Icon(
                                painter = if (confirmPasswordVisible) painterResource(R.drawable.ic_visibility_off) else painterResource(
                                    R.drawable.ic_visibility
                                ), contentDescription = if (confirmPasswordVisible) {
                                    stringResource(R.string.hide_password)
                                } else {
                                    stringResource(R.string.show_password)
                                }
                            )
                        }
                    },
                    textObfuscationMode = if (confirmPasswordVisible) TextObfuscationMode.Visible else TextObfuscationMode.Hidden,
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    onKeyboardAction = {
                        onConfirm(
                            passwordState.text.toString(),
                            confirmPasswordState.text.toString()
                        )
                    })
            }
        }
    })
}