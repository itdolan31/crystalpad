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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.git.itdolan31.crystalpad.R
import com.git.itdolan31.crystalpad.features.settings.components.SettingsRadioItem
import com.git.itdolan31.crystalpad.features.settings.components.SettingsSectionHeader
import com.git.itdolan31.crystalpad.features.settings.components.SettingsTextItem
import com.git.itdolan31.crystalpad.ui.components.DialogRadioItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(), onBack: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val uriHandler = LocalUriHandler.current

    var showThemeDialog by rememberSaveable { mutableStateOf(false) }

    val theme by viewModel.themeState.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()

    Scaffold(topBar = {
        TopAppBar(navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.back)
                )
            }
        }, title = { Text(stringResource(R.string.settings)) })
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader(stringResource(R.string.section_interface))

            SettingsTextItem(
                onClick = { showThemeDialog = true },
                icon = when (theme) {
                    "light" -> painterResource(R.drawable.ic_light_mode)
                    "dark" -> painterResource(R.drawable.ic_dark_mode)
                    "oled" -> painterResource(R.drawable.ic_contrast)
                    else -> painterResource(R.drawable.ic_brightness_4)
                },
                title = stringResource(R.string.theme),
                subtitle = when (theme) {
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
                onClick = { viewModel.setKeepScreenOn(!keepScreenOn) },
                icon = painterResource(R.drawable.ic_mobile_lock_portrait),
                title = stringResource(R.string.keep_screen_on),
                subtitle = stringResource(R.string.keep_screen_on_description),
                checked = keepScreenOn,
                onCheckedChange = { viewModel.setKeepScreenOn(it) }
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
    }
}