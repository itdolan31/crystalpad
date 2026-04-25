package com.git.itdolan31.crystalpad.features.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.git.itdolan31.crystalpad.R
import com.git.itdolan31.crystalpad.core.localization.Translator
import com.git.itdolan31.crystalpad.ui.components.DialogRadioItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit, viewModel: SettingsViewModel
) {
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }

    val theme by viewModel.themeState.collectAsState()
    val language by viewModel.languageState.collectAsState()

    val uriHandler = LocalUriHandler.current

    Scaffold(topBar = {
        TopAppBar(navigationIcon = {
            IconButton(onClick = { onBack() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back), contentDescription = null
                )
            }
        }, title = { Text(Translator.getString("settings")) })
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = Translator.getString("appearance"),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            TextButton(onClick = { showThemeDialog = true }, shape = RectangleShape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = when (theme) {
                            "light" -> painterResource(R.drawable.ic_light_mode)
                            "dark" -> painterResource(R.drawable.ic_dark_mode)
                            "oled" -> painterResource(R.drawable.ic_contrast)
                            else -> painterResource(R.drawable.ic_brightness_4)
                        }, contentDescription = null, modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            Translator.getString("theme"),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            when (theme) {
                                "light" -> Translator.getString("light")
                                "dark" -> Translator.getString("dark")
                                "oled" -> "OLED"
                                else -> Translator.getString("system")
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            TextButton(onClick = { showLanguageDialog = true }, shape = RectangleShape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_language),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            Translator.getString("language"),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            when (language) {
                                "system" -> Translator.getString("system")
                                "de" -> "Deutsch"
                                "en" -> "English"
                                "es" -> "Español"
                                "fr" -> "Français"
                                "ja" -> "日本語"
                                "ko" -> "한국어"
                                "ru" -> "Русский"
                                "zh" -> "中文"
                                else -> language
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Text(
                text = Translator.getString("about"),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
            )
            TextButton(
                onClick = { uriHandler.openUri("https://github.com/itdolan31/crystalpad") },
                shape = RectangleShape
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_code),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        Translator.getString("source_code"),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    if (showThemeDialog) {
        val themes = listOf(
            "system" to Translator.getString("system"),
            "light" to Translator.getString("light"),
            "dark" to Translator.getString("dark"),
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
                        viewModel.onThemeSelected(code)
                        showThemeDialog = false
                    }
                }
            }
        })
    } else if (showLanguageDialog) {
        val languages = listOf(
            "system" to Translator.getString("system"),
            "de" to "Deutsch",
            "en" to "English",
            "es" to "Español",
            "fr" to "Français",
            "ja" to "日本語",
            "ko" to "한국어",
            "ru" to "Русский",
            "zh" to "中文"
        )

        AlertDialog(onDismissRequest = { showLanguageDialog = false }, confirmButton = {}, text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                languages.forEach { (code, text) ->
                    DialogRadioItem(
                        text = text, selected = language == code
                    ) {
                        viewModel.onLanguageSelected(code)
                        showLanguageDialog = false
                    }
                }
            }
        })
    }
}