package com.git.itdolan31.crystalpad.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.git.itdolan31.crystalpad.data.local.database.NoteDao
import com.git.itdolan31.crystalpad.data.local.preferences.SettingsManager
import com.git.itdolan31.crystalpad.screens.MainScreen
import com.git.itdolan31.crystalpad.screens.NoteEditScreen
import com.git.itdolan31.crystalpad.screens.SettingsScreen

@Composable
fun AppNavHost(
    settingsManager: SettingsManager, noteDao: NoteDao
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(navController = navController, noteDao = noteDao)
        }
        composable("settings") {
            SettingsScreen(navController = navController, settingsManager = settingsManager)
        }
        composable("note_edit") {
            NoteEditScreen(navController = navController, noteDao = noteDao)
        }
        composable("note_edit/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toLongOrNull() ?: 0L
            NoteEditScreen(navController = navController, noteDao = noteDao, noteId = noteId)
        }
    }
}