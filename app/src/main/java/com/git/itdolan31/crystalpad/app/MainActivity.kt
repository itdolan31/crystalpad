package com.git.itdolan31.crystalpad.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.git.itdolan31.crystalpad.core.localization.Translator
import com.git.itdolan31.crystalpad.data.local.datastore.SettingsDataSource
import com.git.itdolan31.crystalpad.data.local.room.AppDatabase
import com.git.itdolan31.crystalpad.data.repository.NoteRepository
import com.git.itdolan31.crystalpad.data.repository.SettingsRepository
import com.git.itdolan31.crystalpad.navigation.AppNavHost
import com.git.itdolan31.crystalpad.ui.theme.CrystalPadTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val noteRepository by lazy {
        val dao = AppDatabase.getInstance(applicationContext).noteDao()
        NoteRepository(dao)
    }
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataSource = SettingsDataSource(applicationContext)
        settingsRepository = SettingsRepository(dataSource)

        lifecycleScope.launch {
            Translator.init(this@MainActivity, settingsRepository)
        }

        enableEdgeToEdge()

        setContent {
            val themeState by settingsRepository.themeFlow.collectAsState("system")

            val darkTheme = when (themeState) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            CrystalPadTheme(darkTheme) {
                AppNavHost(settingsRepository, noteRepository)
            }
        }
    }
}