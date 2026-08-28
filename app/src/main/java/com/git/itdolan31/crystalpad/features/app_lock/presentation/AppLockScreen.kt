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
package com.git.itdolan31.crystalpad.features.app_lock.presentation

import androidx.biometric.AuthenticationRequest.Companion.biometricRequest
import androidx.biometric.AuthenticationResult
import androidx.biometric.BiometricManager
import androidx.biometric.compose.rememberAuthenticationLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.git.itdolan31.crystalpad.R
import kotlinx.coroutines.launch

@Composable
fun AppLockScreen(
    viewModel: AppLockViewModel = hiltViewModel(), onUnlocked: () -> Unit
) {
    val context = LocalContext.current

    val isPasswordLoaded by viewModel.isPasswordLoaded.collectAsStateWithLifecycle()
    val isBiometryEnabled by viewModel.isBiometryEnabled.collectAsStateWithLifecycle()
    val fontSize by viewModel.fontSize.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var hasBiometric by remember { mutableStateOf(false) }
    val passwordState = rememberTextFieldState()
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val passwordFocusRequester = remember { FocusRequester() }

    val biometricRequest = remember {
        biometricRequest(
            title = context.getString(R.string.biometric_confirm_title)
        ) { }
    }

    val biometricLauncher = rememberAuthenticationLauncher { result ->
        if (result is AuthenticationResult.Success) {
            onUnlocked()
        }
    }

    LifecycleResumeEffect(Unit) {
        hasBiometric = BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
        onPauseOrDispose { }
    }

    if (isBiometryEnabled != null) {
        LaunchedEffect(Unit) {
            if (isBiometryEnabled == true && hasBiometric) {
                biometricLauncher.launch(biometricRequest)
            } else {
                passwordFocusRequester.requestFocus()
            }
        }
    }

    Scaffold(snackbarHost = {
        SnackbarHost(
            hostState = snackbarHostState, modifier = Modifier
                .imePadding()
                .padding(8.dp, 0.dp)
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.welcome), style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(16.dp))
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
                            ),
                            contentDescription = if (passwordVisible) stringResource(R.string.hide_password) else stringResource(
                                R.string.show_password
                            )
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
                    if (viewModel.checkPassword(passwordState.text.toString())) {
                        onUnlocked()
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.wrong_password)
                            )
                        }
                    }
                }
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (viewModel.checkPassword(passwordState.text.toString())) {
                        onUnlocked()
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.wrong_password)
                            )
                        }
                    }
                }, enabled = isPasswordLoaded
            ) {
                Text(stringResource(R.string.unlock))
            }
            if (isBiometryEnabled == true && hasBiometric) {
                Button(
                    onClick = {
                        biometricLauncher.launch(biometricRequest)
                    }) {
                    Text(stringResource(R.string.unlock_with_biometrics))
                }
            }
        }
    }
}