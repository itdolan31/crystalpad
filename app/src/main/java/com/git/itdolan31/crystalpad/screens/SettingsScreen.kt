package com.git.itdolan31.crystalpad.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.git.itdolan31.crystalpad.data.local.preferences.SettingsManager
import com.git.itdolan31.crystalpad.manager.TranslationManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, settingsManager: SettingsManager) {
    val uriHandler = LocalUriHandler.current

    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }

    val theme by settingsManager.themeFlow.collectAsState(initial = "system")
    val language by settingsManager.languageFlow.collectAsState(initial = "system")

    val coroutineScope = rememberCoroutineScope()

    Scaffold(topBar = {
        TopAppBar(navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, null
                )
            }
        }, title = { Text(TranslationManager.getString("settings")) })
    }) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            TextButton({ showThemeDialog = true }, shape = RectangleShape) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (theme) {
                            "system" -> Icons.Default.Brightness4
                            "light" -> Icons.Default.LightMode
                            "dark" -> Icons.Default.DarkMode
                            else -> Icons.Default.Brightness4
                        }, contentDescription = null, Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            TranslationManager.getString("theme"),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            TranslationManager.getString(theme),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            TextButton({ showLanguageDialog = true }, shape = RectangleShape) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, null, Modifier.size(32.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            TranslationManager.getString("language"),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            when (language) {
                                "system" -> TranslationManager.getString("system")
                                "en" -> "English"
                                "ru" -> "Русский"
                                else -> language
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                }
            }
            TextButton(
                { uriHandler.openUri("https://github.com/itdolan31/crystalpad") },
                shape = RectangleShape
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Code, null, Modifier.size(32.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        TranslationManager.getString("source_code"),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (showThemeDialog) {
                AlertDialog(
                    { showThemeDialog = false },
                    {},
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DialogRadioItem(
                                theme == "system", TranslationManager.getString("system")
                            ) {
                                coroutineScope.launch {
                                    settingsManager.saveTheme("system")
                                    showThemeDialog = false
                                }
                            }
                            DialogRadioItem(
                                theme == "light", TranslationManager.getString("light")
                            ) {
                                coroutineScope.launch {
                                    settingsManager.saveTheme("light")
                                    showThemeDialog = false
                                }
                            }
                            DialogRadioItem(theme == "dark", TranslationManager.getString("dark")) {
                                coroutineScope.launch {
                                    settingsManager.saveTheme("dark")
                                    showThemeDialog = false
                                }
                            }
                        }
                    })
            } else if (showLanguageDialog) {
                AlertDialog(
                    { showLanguageDialog = false },
                    {},
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DialogRadioItem(
                                language == "system", TranslationManager.getString("system")
                            ) {
                                coroutineScope.launch {
                                    settingsManager.saveLanguage("system")
                                    showLanguageDialog = false
                                }
                            }
                            DialogRadioItem(language == "en", "English") {
                                coroutineScope.launch {
                                    settingsManager.saveLanguage("en")
                                    showLanguageDialog = false
                                }
                            }
                            DialogRadioItem(language == "ru", "Русский") {
                                coroutineScope.launch {
                                    settingsManager.saveLanguage("ru")
                                    showLanguageDialog = false
                                }
                            }
                        }
                    })
            }
        }
    }
}

@Composable
fun DialogRadioItem(selected: Boolean, name: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected, null
        )
        Spacer(Modifier.width(10.dp))
        Text(name)
    }
}