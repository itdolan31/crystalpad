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
package com.git.itdolan31.crystalpad.features.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.biometric.AuthenticationRequest.Companion.biometricRequest
import androidx.biometric.AuthenticationResult
import androidx.biometric.BiometricManager
import androidx.biometric.compose.rememberAuthenticationLauncher
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.git.itdolan31.crystalpad.R
import com.git.itdolan31.crystalpad.features.settings.components.SettingsRadioItem
import com.git.itdolan31.crystalpad.features.settings.components.SettingsSectionHeader
import com.git.itdolan31.crystalpad.features.settings.components.SettingsTextItem
import com.git.itdolan31.crystalpad.ui.components.DialogRadioItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(), onBack: () -> Unit
) {
    val confirmTitle = stringResource(R.string.biometric_confirm_title)

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val uriHandler = LocalUriHandler.current

    val theme by viewModel.theme.collectAsState()
    val isKeepScreenOn by viewModel.isKeepScreenOn.collectAsState()
    val isPasswordSet by viewModel.isPasswordSet.collectAsState()
    val isBiometryEnabled by viewModel.isBiometryEnabled.collectAsState()
    val lockTimeout by viewModel.lockTimeout.collectAsState()

    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var showLockTimeoutDialog by rememberSaveable { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    var hasBiometric by remember { mutableStateOf(false) }

    val biometricRequest = remember {
        biometricRequest(
            title = confirmTitle
        ) { }
    }

    val biometricLauncher = rememberAuthenticationLauncher { result ->
        if (result is AuthenticationResult.Success) {
            viewModel.setBiometryEnabled(!isBiometryEnabled)
        }
    }

    LifecycleResumeEffect(Unit) {
        hasBiometric = BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
        onPauseOrDispose { }
    }

    Scaffold(topBar = {
        TopAppBar(navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.back)
                )
            }
        }, title = { Text(stringResource(R.string.settings)) })
    }, snackbarHost = {
        SnackbarHost(
            hostState = snackbarHostState, modifier = Modifier
                .imePadding()
                .padding(8.dp, 0.dp)
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader(stringResource(R.string.section_interface))

            SettingsTextItem(
                onClick = { showThemeDialog = true }, icon = when (theme) {
                    "light" -> painterResource(R.drawable.ic_light_mode)
                    "dark" -> painterResource(R.drawable.ic_dark_mode)
                    "oled" -> painterResource(R.drawable.ic_contrast)
                    else -> painterResource(R.drawable.ic_brightness_4)
                }, title = stringResource(R.string.theme), subtitle = when (theme) {
                    "light" -> stringResource(R.string.light)
                    "dark" -> stringResource(R.string.dark)
                    "oled" -> "OLED"
                    else -> stringResource(R.string.system)
                }
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val language = remember(configuration) { viewModel.getEffectiveLocaleDisplayName() }

                SettingsTextItem(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    icon = painterResource(R.drawable.ic_language),
                    title = stringResource(R.string.language),
                    subtitle = language
                )
            }

            SettingsSectionHeader(stringResource(R.string.section_screen))

            SettingsRadioItem(
                onClick = { viewModel.setKeepScreenOn(!isKeepScreenOn) },
                icon = painterResource(R.drawable.ic_mobile_lock_portrait),
                title = stringResource(R.string.keep_screen_on),
                subtitle = stringResource(R.string.keep_screen_on_description),
                checked = isKeepScreenOn,
                onCheckedChange = { viewModel.setKeepScreenOn(it) })

            SettingsSectionHeader(stringResource(R.string.section_security))

            SettingsRadioItem(
                onClick = { showPasswordDialog = true },
                icon = painterResource(R.drawable.ic_lock),
                title = stringResource(R.string.password_title),
                subtitle = stringResource(R.string.password_subtitle),
                checked = isPasswordSet,
                onCheckedChange = { showPasswordDialog = true })

            SettingsRadioItem(
                onClick = {
                    if (isBiometryEnabled) {
                        viewModel.setBiometryEnabled(false)
                    } else {
                        biometricLauncher.launch(biometricRequest)
                    }
                },
                enabled = isPasswordSet && hasBiometric,
                icon = painterResource(R.drawable.ic_fingerprint),
                title = stringResource(R.string.biometric_title),
                subtitle = stringResource(R.string.biometric_subtitle),
                checked = isBiometryEnabled,
                onCheckedChange = {
                    if (isBiometryEnabled) {
                        viewModel.setBiometryEnabled(false)
                    } else {
                        biometricLauncher.launch(biometricRequest)
                    }
                })

            SettingsRadioItem(
                onClick = {
                    showLockTimeoutDialog = true
                },
                enabled = isPasswordSet,
                icon = painterResource(R.drawable.ic_timer),
                title = stringResource(R.string.auto_lock_title),
                subtitle = stringResource(R.string.auto_lock_subtitle),
                checked = lockTimeout >= 0,
                onCheckedChange = { showLockTimeoutDialog = true })

            SettingsSectionHeader(stringResource(R.string.section_about))

            SettingsTextItem(
                onClick = { uriHandler.openUri("https://github.com/itdolan31/crystalpad") },
                icon = painterResource(R.drawable.ic_code),
                title = stringResource(R.string.source_code),
                subtitle = stringResource(R.string.source_code_description)
            )
        }
    }

    if (showThemeDialog) {
        val themes = listOf(
            "system" to stringResource(R.string.system),
            "light" to stringResource(R.string.light),
            "dark" to stringResource(R.string.dark),
            "oled" to "OLED"
        )

        AlertDialog(onDismissRequest = { showThemeDialog = false }, confirmButton = {}, text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                themes.forEach { (code, text) ->
                    DialogRadioItem(
                        text = text, selected = theme == code
                    ) {
                        viewModel.setTheme(code)
                        showThemeDialog = false
                    }
                }
            }
        })
    } else if (showPasswordDialog) {
        AlertDialog(onDismissRequest = {
            showPasswordDialog = false
            password = ""
            confirmPassword = ""
        }, confirmButton = {
            TextButton(
                onClick = {
                    if (isPasswordSet) {
                        val (isSuccess, messageRes) = viewModel.resetPassword(password)

                        if (isSuccess) {
                            showPasswordDialog = false
                            password = ""
                        }

                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(context.getString(messageRes))
                        }
                    } else {
                        val (isSuccess, messageRes) = viewModel.setNewPassword(
                            password, confirmPassword
                        )

                        if (isSuccess) {
                            showPasswordDialog = false
                            password = ""
                            confirmPassword = ""
                        }

                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(context.getString(messageRes))
                        }
                    }
                }) {
                Text("OK")
            }
        }, dismissButton = {
            TextButton(
                onClick = {
                    showPasswordDialog = false
                    password = ""
                    confirmPassword = ""
                }) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
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
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
                if (!isPasswordSet) {
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text(stringResource(R.string.confirm_password)) },
                        trailingIcon = {
                            IconButton(onClick = {
                                confirmPasswordVisible = !confirmPasswordVisible
                            }) {
                                Icon(
                                    painter = if (confirmPasswordVisible) painterResource(R.drawable.ic_visibility_off) else painterResource(
                                        R.drawable.ic_visibility
                                    ), contentDescription = if (passwordVisible) {
                                        stringResource(R.string.hide_password)
                                    } else {
                                        stringResource(R.string.show_password)
                                    }
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )
                }
            }
        })
    } else if (showLockTimeoutDialog) {
        val lockTimeouts = listOf(
            -1 to stringResource(R.string.never),
            0 to stringResource(R.string.immediately),
            60 to stringResource(R.string.minute_1),
            300 to stringResource(R.string.minutes_5),
            900 to stringResource(R.string.minutes_15),
            1800 to stringResource(R.string.minutes_30),
            3600 to stringResource(R.string.hour_1)
        )

        AlertDialog(
            onDismissRequest = { showLockTimeoutDialog = false },
            confirmButton = {},
            title = {
                Text(stringResource(R.string.timeout))
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    lockTimeouts.forEach { (code, text) ->
                        DialogRadioItem(
                            text = text, selected = lockTimeout == code
                        ) {
                            viewModel.setLockTimeout(code)
                            showLockTimeoutDialog = false
                        }
                    }
                }
            })
    }
}