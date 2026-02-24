package com.git.itdolan31.crystalpad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.git.itdolan31.crystalpad.data.local.database.AppDatabase
import com.git.itdolan31.crystalpad.data.local.preferences.SettingsManager
import com.git.itdolan31.crystalpad.manager.TranslationManager
import com.git.itdolan31.crystalpad.navigation.AppNavHost
import com.git.itdolan31.crystalpad.ui.theme.CrystalPadTheme

class MainActivity : ComponentActivity() {
    private val noteDao by lazy { AppDatabase.getInstance(this).noteDao() }

    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        settingsManager = SettingsManager(applicationContext)

        TranslationManager.init(settingsManager)

        setContent {
            val themeState by settingsManager.themeFlow.collectAsState(initial = "Auto")

            val darkTheme = when (themeState) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            CrystalPadTheme(darkTheme) {
                AppNavHost(settingsManager, noteDao)
            }
        }
    }
}