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
package com.git.itdolan31.crystalpad.features.settings.presentation

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
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
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
import com.git.itdolan31.crystalpad.core.ui.components.DialogRadioItem
import com.git.itdolan31.crystalpad.core.utils.formatDateTime
import com.git.itdolan31.crystalpad.domain.model.DatePatternType
import com.git.itdolan31.crystalpad.domain.model.ThemeType
import com.git.itdolan31.crystalpad.domain.model.TimePatternType
import com.git.itdolan31.crystalpad.features.settings.presentation.components.SettingsSectionHeader
import com.git.itdolan31.crystalpad.features.settings.presentation.components.SettingsSwitchItem
import com.git.itdolan31.crystalpad.features.settings.presentation.components.SettingsTextItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(), onBack: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val uriHandler = LocalUriHandler.current

    val themeType by viewModel.themeType.collectAsStateWithLifecycle()
    val isKeepScreenOn by viewModel.isKeepScreenOn.collectAsStateWithLifecycle()
    val isPasswordSet by viewModel.isPasswordSet.collectAsStateWithLifecycle()
    val isBiometryEnabled by viewModel.isBiometryEnabled.collectAsStateWithLifecycle()
    val lockTimeout by viewModel.lockTimeout.collectAsStateWithLifecycle()
    val datePattern by viewModel.datePattern.collectAsStateWithLifecycle()
    val timePattern by viewModel.timePattern.collectAsStateWithLifecycle()
    val fontSize by viewModel.fontSize.collectAsStateWithLifecycle()
    val isFlagSecureEnabled by viewModel.isFlagSecureEnabled.collectAsStateWithLifecycle()
    val isDynamicColorEnabled by viewModel.isDynamicColorEnabled.collectAsStateWithLifecycle()
    val isTrashEnabled by viewModel.isTrashEnabled.collectAsStateWithLifecycle()
    val trashRetention by viewModel.trashRetention.collectAsStateWithLifecycle()
    val isWordWrapEnabled by viewModel.isWordWrapEnabled.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var showLockTimeoutDialog by rememberSaveable { mutableStateOf(false) }
    var showDatePatternDialog by rememberSaveable { mutableStateOf(false) }
    var showTimePatternDialog by rememberSaveable { mutableStateOf(false) }
    var showFontSizeDialog by rememberSaveable { mutableStateOf(false) }
    var showTrashRetentionDialog by rememberSaveable { mutableStateOf(false) }

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
                SettingsSwitchItem(
                    onClick = { viewModel.setDynamicColorEnabled(!isDynamicColorEnabled) },
                    enabled = themeType != ThemeType.OLED,
                    icon = painterResource(R.drawable.ic_palette),
                    title = stringResource(R.string.dynamic_color_title),
                    subtitle = stringResource(R.string.dynamic_color_subtitle),
                    checked = isDynamicColorEnabled,
                    onCheckedChange = { viewModel.setDynamicColorEnabled(it) })
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

            SettingsSwitchItem(
                onClick = { viewModel.setKeepScreenOn(!isKeepScreenOn) },
                icon = painterResource(R.drawable.ic_mobile_lock_portrait),
                title = stringResource(R.string.keep_screen_on),
                subtitle = stringResource(R.string.keep_screen_on_description),
                checked = isKeepScreenOn,
                onCheckedChange = { viewModel.setKeepScreenOn(it) })

            SettingsSwitchItem(
                onClick = { viewModel.setWordWrapEnabled(!isWordWrapEnabled) },
                icon = painterResource(R.drawable.ic_wrap_text),
                title = stringResource(R.string.word_wrap_title),
                subtitle = stringResource(R.string.word_wrap_description),
                checked = isWordWrapEnabled,
                onCheckedChange = { viewModel.setWordWrapEnabled(it) })

            SettingsSectionHeader(stringResource(R.string.section_security))

            SettingsSwitchItem(
                onClick = { showPasswordDialog = true },
                icon = painterResource(R.drawable.ic_lock),
                title = stringResource(R.string.password_title),
                subtitle = stringResource(R.string.password_subtitle),
                checked = isPasswordSet,
                onCheckedChange = { showPasswordDialog = true })

            SettingsSwitchItem(
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

            SettingsSwitchItem(
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

            SettingsSwitchItem(
                onClick = { viewModel.setFlagSecureEnabled(!isFlagSecureEnabled) },
                icon = painterResource(R.drawable.ic_screenshot_monitor),
                title = stringResource(R.string.flag_secure),
                subtitle = stringResource(R.string.flag_secure_description),
                checked = isFlagSecureEnabled,
                onCheckedChange = { viewModel.setFlagSecureEnabled(it) })

            SettingsSectionHeader(stringResource(R.string.section_data))

            SettingsSwitchItem(
                onClick = { viewModel.setTrashEnabled(!isTrashEnabled) },
                icon = painterResource(R.drawable.ic_delete),
                title = stringResource(R.string.trash),
                subtitle = stringResource(R.string.trash_description),
                checked = isTrashEnabled,
                onCheckedChange = { viewModel.setTrashEnabled(it) })

            SettingsTextItem(
                onClick = { showTrashRetentionDialog = true },
                enabled = isTrashEnabled,
                icon = painterResource(R.drawable.ic_delete),
                title = stringResource(R.string.trash_retention_title),
                subtitle = stringResource(
                    when (trashRetention) {
                        86_400_000L -> R.string.days_1
                        259_200_000L -> R.string.days_3
                        604_800_000L -> R.string.days_7
                        1_296_000_000L -> R.string.days_15
                        2_592_000_000L -> R.string.days_30
                        else -> R.string.days_30
                    }
                )
            )

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
                modifier = Modifier.verticalScroll(rememberScrollState())
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
        val passwordState = rememberTextFieldState()
        val confirmPasswordState = rememberTextFieldState()
        val passwordFocusRequester = remember { FocusRequester() }
        val confirmPasswordFocusRequester = remember { FocusRequester() }
        var passwordVisible by rememberSaveable { mutableStateOf(false) }
        var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            passwordFocusRequester.requestFocus()
        }

        AlertDialog(onDismissRequest = {
            showPasswordDialog = false
        }, confirmButton = {
            TextButton(
                onClick = {
                    if (isPasswordSet) {
                        val (success, resId) = viewModel.resetPassword(passwordState.text.toString())

                        showPasswordDialog = !success

                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(context.getString(resId))
                        }
                    } else {
                        val (success, resId) = viewModel.setNewPassword(
                            passwordState.text.toString(), confirmPasswordState.text.toString()
                        )

                        showPasswordDialog = !success

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
                            val (success, resId) = viewModel.resetPassword(passwordState.text.toString())

                            showPasswordDialog = !success

                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(context.getString(resId))
                            }
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
                                    ), contentDescription = if (passwordVisible) {
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
                            val (success, resId) = viewModel.setNewPassword(
                                passwordState.text.toString(), confirmPasswordState.text.toString()
                            )

                            showPasswordDialog = !success

                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(context.getString(resId))
                            }
                        }
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
                    modifier = Modifier.verticalScroll(rememberScrollState())
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
                    modifier = Modifier.verticalScroll(rememberScrollState())
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
                    modifier = Modifier.verticalScroll(rememberScrollState())
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
        val fontSizeState = rememberTextFieldState(fontSize.toString())
        val fontSizeFocusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            fontSizeFocusRequester.requestFocus()
        }

        AlertDialog(onDismissRequest = { showFontSizeDialog = false }, confirmButton = {
            TextButton(onClick = {
                val size = fontSizeState.text.toString().toIntOrNull()

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
                    state = fontSizeState,
                    modifier = Modifier.focusRequester(fontSizeFocusRequester),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = fontSize.sp,
                        textDirection = TextDirection.Content,
                        lineHeight = (fontSize * 1.5).sp
                    ),
                    label = { Text(stringResource(R.string.font_size)) },
                    inputTransformation = InputTransformation {
                        val digits = toString().filter { it.isDigit() }
                        if (toString() != digits) {
                            replace(0, length, digits)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                    ),
                    onKeyboardAction = {
                        val size = fontSizeState.text.toString().toIntOrNull()

                        if (size != null && size in 15..50) {
                            showFontSizeDialog = false
                            viewModel.setFontSize(size)
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.font_size_error))
                            }
                        }
                    },
                    lineLimits = TextFieldLineLimits.SingleLine
                )
            }
        })
    } else if (showTrashRetentionDialog) {
        val trashRetentions = remember {
            listOf(
                86_400_000L to R.string.days_1,
                259_200_000L to R.string.days_3,
                604_800_000L to R.string.days_7,
                1_296_000_000L to R.string.days_15,
                2_592_000_000L to R.string.days_30
            )
        }

        AlertDialog(
            onDismissRequest = { showTrashRetentionDialog = false },
            confirmButton = {},
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    trashRetentions.forEach { (millis, id) ->
                        DialogRadioItem(
                            text = stringResource(id), selected = trashRetention == millis
                        ) {
                            viewModel.setTrashRetention(millis)
                            showTrashRetentionDialog = false
                        }
                    }
                }
            })
    }
}