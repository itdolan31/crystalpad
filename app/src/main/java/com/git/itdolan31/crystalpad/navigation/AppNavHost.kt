package com.git.itdolan31.crystalpad.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.git.itdolan31.crystalpad.data.repository.NoteRepository
import com.git.itdolan31.crystalpad.data.repository.SettingsRepository
import com.git.itdolan31.crystalpad.features.home.HomeScreen
import com.git.itdolan31.crystalpad.features.home.HomeViewModel
import com.git.itdolan31.crystalpad.features.note_edit.NoteEditScreen
import com.git.itdolan31.crystalpad.features.note_edit.NoteEditViewModel
import com.git.itdolan31.crystalpad.features.settings.SettingsScreen
import com.git.itdolan31.crystalpad.features.settings.SettingsViewModel
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen : NavKey {
    @Serializable
    data object Home : Screen()

    @Serializable
    data object Settings : Screen()

    @Serializable
    data class NoteEdit(val noteId: Long = 0L) : Screen()
}

@Composable
fun AppNavHost(
    settingsRepository: SettingsRepository, noteRepository: NoteRepository
) {
    val backStack = rememberNavBackStack(Screen.Home)

    NavDisplay(backStack = backStack, onBack = {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }, transitionSpec = {
        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
    }, popTransitionSpec = {
        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
    }, entryProvider = entryProvider {
        entry<Screen.Home> {
            val factory = remember {
                viewModelFactory {
                    initializer {
                        HomeViewModel(
                            repository = noteRepository, settingsRepository = settingsRepository
                        )
                    }
                }
            }
            val viewModel: HomeViewModel = viewModel(factory = factory)

            HomeScreen(
                onNavigateToSettings = { backStack.add(Screen.Settings) },
                onNavigateToNoteEdit = { noteId ->
                    backStack.add(Screen.NoteEdit(noteId))
                },
                viewModel = viewModel
            )
        }

        entry<Screen.Settings> {
            val factory = remember {
                viewModelFactory {
                    initializer {
                        SettingsViewModel(settingsRepository)
                    }
                }
            }

            val viewModel: SettingsViewModel = viewModel(factory = factory)

            SettingsScreen(
                onBack = { backStack.removeLastOrNull() }, viewModel = viewModel
            )
        }

        entry<Screen.NoteEdit> {
            val factory = remember {
                viewModelFactory {
                    initializer { NoteEditViewModel(noteRepository) }
                }
            }
            val viewModel: NoteEditViewModel = viewModel(factory = factory)

            NoteEditScreen(
                noteId = it.noteId, onBack = { backStack.removeLastOrNull() }, viewModel = viewModel
            )
        }
    })
}