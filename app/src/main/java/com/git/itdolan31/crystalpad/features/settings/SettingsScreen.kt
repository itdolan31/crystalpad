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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.git.itdolan31.crystalpad.core.domain.model.DatePatternType
import com.git.itdolan31.crystalpad.core.domain.model.ThemeType
import com.git.itdolan31.crystalpad.core.domain.model.TimePatternType
import com.git.itdolan31.crystalpad.core.utils.formatDateTime
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
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val uriHandler = LocalUriHandler.current

    val notes by viewModel.notes.collectAsState()
    val themeType by viewModel.themeType.collectAsState()
    val isKeepScreenOn by viewModel.isKeepScreenOn.collectAsState()
    val isPasswordSet by viewModel.isPasswordSet.collectAsState()
    val isBiometryEnabled by viewModel.isBiometryEnabled.collectAsState()
    val lockTimeout by viewModel.lockTimeout.collectAsState()
    val datePattern by viewModel.datePattern.collectAsState()
    val timePattern by viewModel.timePattern.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val isFlagSecureEnabled by viewModel.isFlagSecureEnabled.collectAsState()
    val isDynamicColorEnabled by viewModel.isDynamicColorEnabled.collectAsState()

    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var showLockTimeoutDialog by rememberSaveable { mutableStateOf(false) }
    var showDatePatternDialog by rememberSaveable { mutableStateOf(false) }
    var showTimePatternDialog by rememberSaveable { mutableStateOf(false) }
    var showFontSizeDialog by rememberSaveable { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var hasBiometric by remember { mutableStateOf(false) }

    val biometricRequest = remember {
        biometricRequest(
            title = context.getString(R.string.biometric_confirm_title)
        ) { }
    }

    val biometricLauncher = rememberAuthenticationLauncher { result ->
        if (result is AuthenticationResult.Success) {
            viewModel.setBiometryEnabled(!isBiometryEnabled)
        }
    }

    val exportZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(viewModel.exportNotes(context, it))
            }
        }
    }

    val importZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    viewModel.importNotes(context, it)
                )
            }
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
                onClick = { showThemeDialog = true },
                icon = painterResource(remember(themeType) {
                    when (themeType) {
                        ThemeType.LIGHT -> R.drawable.ic_light_mode
                        ThemeType.DARK -> R.drawable.ic_dark_mode
                        ThemeType.OLED -> R.drawable.ic_contrast
                        ThemeType.SYSTEM -> R.drawable.ic_brightness_4
                    }
                }),
                title = stringResource(R.string.theme),
                subtitle = if (themeType == ThemeType.OLED) {
                    "OLED"
                } else {
                    stringResource(
                        when (themeType) {
                            ThemeType.LIGHT -> R.string.light
                            ThemeType.DARK -> R.string.dark
                            else -> R.string.system
                        }
                    )
                }
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SettingsRadioItem(
                    onClick = { viewModel.setDynamicColorEnabled(!isDynamicColorEnabled) },
                    enabled = themeType != ThemeType.OLED,
                    icon = painterResource(R.drawable.ic_palette),
                    title = stringResource(R.string.dynamic_color_title),
                    subtitle = stringResource(R.string.dynamic_color_subtitle),
                    checked = isDynamicColorEnabled,
                    onCheckedChange = { viewModel.setDynamicColorEnabled(it) }
                )
            }

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

            SettingsTextItem(
                onClick = { showDatePatternDialog = true },
                icon = painterResource(R.drawable.ic_date_range),
                title = stringResource(R.string.format_date_title),
                subtitle = formatDateTime(1738184400000, datePattern.pattern)
            )

            SettingsTextItem(
                onClick = { showTimePatternDialog = true },
                icon = painterResource(R.drawable.ic_schedule),
                title = stringResource(R.string.format_time_title),
                subtitle = formatDateTime(23400000, timePattern.pattern)
            )

            SettingsTextItem(
                onClick = { showFontSizeDialog = true },
                icon = painterResource(R.drawable.ic_format_size),
                title = stringResource(R.string.font_size),
                subtitle = fontSize.toString()
            )

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
                subtitle = stringResource(remember(lockTimeout) {
                    when (lockTimeout) {
                        -1 -> R.string.never
                        60 -> R.string.minute_1
                        300 -> R.string.minutes_5
                        900 -> R.string.minutes_15
                        1800 -> R.string.minutes_30
                        3600 -> R.string.hour_1
                        else -> R.string.immediately
                    }
                }),
                checked = lockTimeout >= 0,
                onCheckedChange = { showLockTimeoutDialog = true })

            SettingsRadioItem(
                onClick = { viewModel.setFlagSecureEnabled(!isFlagSecureEnabled) },
                icon = painterResource(R.drawable.ic_screenshot_monitor),
                title = stringResource(R.string.flag_secure),
                subtitle = stringResource(R.string.flag_secure_description),
                checked = isFlagSecureEnabled,
                onCheckedChange = { viewModel.setFlagSecureEnabled(it) })

            SettingsSectionHeader(stringResource(R.string.section_data))

            SettingsTextItem(
                onClick = {
                    if (notes.isEmpty()) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.export_no_notes))
                        }
                    } else {
                        exportZipLauncher.launch("crystalpad-backup-${System.currentTimeMillis()}.zip")
                    }
                },
                icon = painterResource(R.drawable.ic_upload),
                title = stringResource(R.string.export_title),
                subtitle = stringResource(R.string.export_title_description)
            )

            SettingsTextItem(
                onClick = { importZipLauncher.launch(arrayOf("application/zip")) },
                icon = painterResource(R.drawable.ic_download),
                title = stringResource(R.string.import_title),
                subtitle = stringResource(R.string.import_title_description)
            )

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
        AlertDialog(onDismissRequest = { showThemeDialog = false }, confirmButton = {}, text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                ThemeType.entries.forEach { type ->
                    DialogRadioItem(
                        text = if (type == ThemeType.OLED) {
                            "OLED"
                        } else {
                            stringResource(
                                when (type) {
                                    ThemeType.LIGHT -> R.string.light
                                    ThemeType.DARK -> R.string.dark
                                    else -> R.string.system
                                }
                            )
                        }, selected = themeType == type
                    ) {
                        viewModel.setTheme(type)
                        showThemeDialog = false
                    }
                }
            }
        })
    } else if (showPasswordDialog) {
        var passwordInput by rememberSaveable { mutableStateOf("") }
        var confirmPasswordInput by rememberSaveable { mutableStateOf("") }
        var passwordVisible by rememberSaveable { mutableStateOf(false) }
        var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

        AlertDialog(onDismissRequest = {
            showPasswordDialog = false
        }, confirmButton = {
            TextButton(
                onClick = {
                    if (isPasswordSet) {
                        val (isNotSuccess, resId) = viewModel.resetPassword(passwordInput)

                        showPasswordDialog = isNotSuccess

                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(context.getString(resId))
                        }
                    } else {
                        val (isNotSuccess, resId) = viewModel.setNewPassword(
                            passwordInput, confirmPasswordInput
                        )

                        showPasswordDialog = isNotSuccess

                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(context.getString(resId))
                        }
                    }
                }) {
                Text("OK")
            }
        }, dismissButton = {
            TextButton(
                onClick = {
                    showPasswordDialog = false
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
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
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
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it },
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
        val lockTimeouts = remember {
            listOf(
                -1 to R.string.never,
                0 to R.string.immediately,
                60 to R.string.minute_1,
                300 to R.string.minutes_5,
                900 to R.string.minutes_15,
                1800 to R.string.minutes_30,
                3600 to R.string.hour_1
            )
        }

        AlertDialog(
            onDismissRequest = { showLockTimeoutDialog = false },
            confirmButton = {},
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    lockTimeouts.forEach { (code, id) ->
                        DialogRadioItem(
                            text = stringResource(id), selected = lockTimeout == code
                        ) {
                            viewModel.setLockTimeout(code)
                            showLockTimeoutDialog = false
                        }
                    }
                }
            })
    } else if (showDatePatternDialog) {
        AlertDialog(
            onDismissRequest = { showDatePatternDialog = false },
            confirmButton = {},
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    DatePatternType.entries.forEach { type ->
                        DialogRadioItem(
                            text = formatDateTime(1738184400000, type.pattern),
                            selected = datePattern == type
                        ) {
                            viewModel.setDatePattern(type)
                            showDatePatternDialog = false
                        }
                    }
                }
            })
    } else if (showTimePatternDialog) {
        AlertDialog(
            onDismissRequest = { showTimePatternDialog = false },
            confirmButton = {},
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    TimePatternType.entries.forEach { type ->
                        DialogRadioItem(
                            text = formatDateTime(23400000, type.pattern),
                            selected = timePattern == type
                        ) {
                            viewModel.setTimePattern(type)
                            showTimePatternDialog = false
                        }
                    }
                }
            })
    } else if (showFontSizeDialog) {
        var fontSizeInput by rememberSaveable { mutableStateOf(fontSize.toString()) }

        AlertDialog(onDismissRequest = { showFontSizeDialog = false }, confirmButton = {
            TextButton(onClick = {
                val size = fontSizeInput.toIntOrNull()

                if (size != null && size in 15..50) {
                    showFontSizeDialog = false
                    viewModel.setFontSize(size)
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(context.getString(R.string.font_size_error))
                    }
                }
            }) { Text("OK") }
        }, dismissButton = {
            TextButton(onClick = { showFontSizeDialog = false }) {
                Text(
                    stringResource(R.string.cancel)
                )
            }
        }, text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = fontSizeInput,
                    onValueChange = {
                        if (it.isEmpty() || it.toIntOrNull() != null) {
                            fontSizeInput = it
                        }
                    },
                    label = { Text(stringResource(R.string.font_size)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        })
    }
}