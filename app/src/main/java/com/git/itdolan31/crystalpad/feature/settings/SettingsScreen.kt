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
package com.git.itdolan31.crystalpad.feature.settings

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
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
import com.git.itdolan31.crystalpad.core.ui.components.ClearTrashDialog
import com.git.itdolan31.crystalpad.core.ui.components.DialogRadioItem
import com.git.itdolan31.crystalpad.core.utils.formatDateTime
import com.git.itdolan31.crystalpad.domain.model.DatePatternType
import com.git.itdolan31.crystalpad.domain.model.ThemeType
import com.git.itdolan31.crystalpad.domain.model.TimePatternType
import com.git.itdolan31.crystalpad.feature.settings.components.PasswordDialog
import com.git.itdolan31.crystalpad.feature.settings.components.SettingsSectionHeader
import com.git.itdolan31.crystalpad.feature.settings.components.SettingsSwitchItem
import com.git.itdolan31.crystalpad.feature.settings.components.SettingsTextItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val theme by viewModel.theme.collectAsStateWithLifecycle()
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
    val trashedNotes by viewModel.trashedNotes.collectAsStateWithLifecycle()

    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var showLockTimeoutDialog by rememberSaveable { mutableStateOf(false) }
    var showDatePatternDialog by rememberSaveable { mutableStateOf(false) }
    var showTimePatternDialog by rememberSaveable { mutableStateOf(false) }
    var showFontSizeDialog by rememberSaveable { mutableStateOf(false) }
    var showClearTrashDialog by rememberSaveable { mutableStateOf(false) }
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
                snackbarHostState.showSnackbar(viewModel.exportNotes(it))
            }
        }
    }

    val importZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    viewModel.importNotes(it)
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

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }, title = { Text(stringResource(R.string.settings)) })
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState, modifier = Modifier
                    .padding(8.dp, 0.dp)
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            SettingsSectionHeader(stringResource(R.string.section_interface))

            SettingsTextItem(
                onClick = { showThemeDialog = true },
                icon = painterResource(theme.getIconRes()),
                title = stringResource(R.string.theme),
                subtitle = stringResource(theme.getTitleRes())
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SettingsSwitchItem(
                    onClick = { viewModel.setDynamicColorEnabled(it) },
                    icon = painterResource(R.drawable.ic_palette),
                    title = stringResource(R.string.dynamic_color_title),
                    subtitle = stringResource(R.string.dynamic_color_subtitle),
                    checked = isDynamicColorEnabled
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

            SettingsSwitchItem(
                onClick = { viewModel.setKeepScreenOn(it) },
                icon = painterResource(R.drawable.ic_mobile_lock_portrait),
                title = stringResource(R.string.keep_screen_on),
                subtitle = stringResource(R.string.keep_screen_on_description),
                checked = isKeepScreenOn
            )

            SettingsSwitchItem(
                onClick = { viewModel.setWordWrapEnabled(it) },
                icon = painterResource(R.drawable.ic_wrap_text),
                title = stringResource(R.string.word_wrap_title),
                subtitle = stringResource(R.string.word_wrap_description),
                checked = isWordWrapEnabled
            )

            SettingsSectionHeader(stringResource(R.string.section_security))

            SettingsSwitchItem(
                onClick = { showPasswordDialog = true },
                icon = painterResource(R.drawable.ic_lock),
                title = stringResource(R.string.password_title),
                subtitle = stringResource(R.string.password_subtitle),
                checked = isPasswordSet
            )

            SettingsSwitchItem(
                onClick = {
                    when {
                        isBiometryEnabled -> viewModel.setBiometryEnabled(false)
                        else -> biometricLauncher.launch(biometricRequest)
                    }
                },
                enabled = isPasswordSet && hasBiometric,
                icon = painterResource(R.drawable.ic_fingerprint),
                title = stringResource(R.string.biometric_title),
                subtitle = stringResource(R.string.biometric_subtitle),
                checked = isBiometryEnabled
            )

            SettingsSwitchItem(
                onClick = {
                    showLockTimeoutDialog = true
                },
                enabled = isPasswordSet,
                icon = painterResource(R.drawable.ic_timer),
                title = stringResource(R.string.auto_lock_title),
                subtitle = stringResource(
                    SettingsOptions.lockTimeouts[lockTimeout] ?: R.string.immediately
                ),
                checked = lockTimeout >= 0
            )

            SettingsSwitchItem(
                onClick = { viewModel.setFlagSecureEnabled(it) },
                icon = painterResource(R.drawable.ic_screenshot_monitor),
                title = stringResource(R.string.flag_secure),
                subtitle = stringResource(R.string.flag_secure_description),
                checked = isFlagSecureEnabled
            )

            SettingsSectionHeader(stringResource(R.string.section_data))

            SettingsSwitchItem(
                onClick = {
                    when {
                        it -> viewModel.setTrashEnabled(true)
                        trashedNotes.isEmpty() -> viewModel.setTrashEnabled(false)
                        else -> showClearTrashDialog = true
                    }
                },
                icon = painterResource(R.drawable.ic_delete),
                title = stringResource(R.string.trash),
                subtitle = stringResource(R.string.trash_description),
                checked = isTrashEnabled
            )

            SettingsTextItem(
                onClick = { showTrashRetentionDialog = true },
                enabled = isTrashEnabled,
                icon = painterResource(R.drawable.ic_delete),
                title = stringResource(R.string.trash_retention_title),
                subtitle = stringResource(
                    SettingsOptions.trashRetentions[trashRetention] ?: R.string.days_30
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

            SettingsTextItem(
                onClick = onNavigateToAbout,
                title = stringResource(R.string.about_title)
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
                        text = stringResource(type.getTitleRes()), selected = theme == type
                    ) {
                        viewModel.setTheme(type)
                        showThemeDialog = false
                    }
                }
            }
        })
    } else if (showPasswordDialog) {
        PasswordDialog(
            isPasswordSet = isPasswordSet,
            fontSize = fontSize,
            onDismiss = {
                showPasswordDialog = false
            },
            onConfirm = { password, confirmPassword ->
                coroutineScope.launch {
                    val (success, resId) = viewModel.submitPassword(password, confirmPassword)

                    showPasswordDialog = !success

                    snackbarHostState.showSnackbar(context.getString(resId))
                }
            },
        )
    } else if (showLockTimeoutDialog) {
        AlertDialog(
            onDismissRequest = { showLockTimeoutDialog = false },
            confirmButton = {},
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    SettingsOptions.lockTimeouts.forEach { (seconds, stringRes) ->
                        DialogRadioItem(
                            text = stringResource(stringRes), selected = lockTimeout == seconds
                        ) {
                            viewModel.setLockTimeout(seconds)
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
    } else if (showClearTrashDialog) {
        ClearTrashDialog(onDismiss = { showClearTrashDialog = false }, onConfirm = {
            showClearTrashDialog = false
            viewModel.setTrashEnabled(false)
            viewModel.clearTrash()
        })
    } else if (showTrashRetentionDialog) {
        AlertDialog(
            onDismissRequest = { showTrashRetentionDialog = false },
            confirmButton = {},
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    SettingsOptions.trashRetentions.forEach { (seconds, stringRes) ->
                        DialogRadioItem(
                            text = stringResource(stringRes), selected = trashRetention == seconds
                        ) {
                            viewModel.setTrashRetention(seconds)
                            showTrashRetentionDialog = false
                        }
                    }
                }
            })
    }
}