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
package com.git.itdolan31.crystalpad.feature.settings.about.licenses

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.git.itdolan31.crystalpad.R
import com.git.itdolan31.crystalpad.feature.settings.components.SettingsTextItem
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    Scaffold(topBar = {
        TopAppBar(navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.back)
                )
            }
        }, title = { Text(stringResource(R.string.licenses_title)) })
    }) { innerPadding ->
        val libraries by produceLibraries(R.raw.aboutlibraries)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = innerPadding
        ) {
            item {
                SettingsTextItem(
                    onClick = { uriHandler.openUri("https://github.com/itdolan31/crystalpad") },
                    title = "Crystalpad",
                    subtitle = "GPL-3.0-or-later"
                )
            }
            item {
                SettingsTextItem(
                    onClick = { uriHandler.openUri("https://github.com/google/material-design-icons") },
                    title = "Material Symbols",
                    subtitle = "Apache License 2.0"
                )
            }
            items(libraries?.libraries ?: emptyList(), key = { it.uniqueId }) { lib ->
                SettingsTextItem(
                    onClick = { lib.scm?.url?.let { uriHandler.openUri(it) } },
                    title = lib.name,
                    subtitle = lib.licenses.joinToString { it.name })
            }
        }
    }
}