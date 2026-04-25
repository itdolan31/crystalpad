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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.git.itdolan31.crystalpad.data.repository.NoteRepository
import com.git.itdolan31.crystalpad.data.repository.SettingsRepository
import com.git.itdolan31.crystalpad.features.home.HomeScreen
import com.git.itdolan31.crystalpad.features.home.HomeViewModel
import com.git.itdolan31.crystalpad.features.note_edit.NoteEditScreen
import com.git.itdolan31.crystalpad.features.note_edit.NoteEditViewModel
import com.git.itdolan31.crystalpad.features.settings.SettingsScreen
import com.git.itdolan31.crystalpad.features.settings.SettingsViewModel

@Composable
fun CrystalPadNavHost(
    settingsRepository: SettingsRepository, noteRepository: NoteRepository
) {
    val backStack = rememberNavBackStack(Screen.Home)


    NavDisplay(
        backStack = backStack, onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        }, entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ), transitionSpec = {
            slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
        }, popTransitionSpec = {
            slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
        }, predictivePopTransitionSpec = {
            slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
        }, entryProvider = entryProvider {
            entry<Screen.Home> {
                val viewModel: HomeViewModel = viewModel(factory = remember {
                    viewModelFactory {
                        initializer {
                            HomeViewModel(
                                noteRepository = noteRepository,
                                settingsRepository = settingsRepository
                            )
                        }
                    }
                })

                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = { backStack.add(Screen.Settings) },
                    onNavigateToNoteEdit = { noteId ->
                        backStack.add(Screen.NoteEdit(noteId))
                    })
            }

            entry<Screen.Settings> {
                val viewModel: SettingsViewModel = viewModel(factory = remember {
                    viewModelFactory {
                        initializer {
                            SettingsViewModel(settingsRepository = settingsRepository)
                        }
                    }
                })

                SettingsScreen(
                    viewModel = viewModel, onBack = { backStack.removeLastOrNull() })
            }

            entry<Screen.NoteEdit> { entry ->
                val viewModel: NoteEditViewModel = viewModel(factory = remember {
                    viewModelFactory {
                        initializer {
                            NoteEditViewModel(
                                noteRepository = noteRepository, noteId = entry.noteId
                            )
                        }
                    }
                })

                NoteEditScreen(
                    viewModel = viewModel, onBack = { backStack.removeLastOrNull() })
            }
        })

}