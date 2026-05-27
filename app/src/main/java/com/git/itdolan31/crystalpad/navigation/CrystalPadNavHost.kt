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
package com.git.itdolan31.crystalpad.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.git.itdolan31.crystalpad.features.home.HomeScreen
import com.git.itdolan31.crystalpad.features.note_edit.NoteEditScreen
import com.git.itdolan31.crystalpad.features.note_edit.NoteEditViewModel
import com.git.itdolan31.crystalpad.features.settings.SettingsScreen

@Composable
fun CrystalPadNavHost(backStack: NavBackStack<NavKey>) {
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
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
                HomeScreen(
                    onNavigateToSettings = { backStack.add(Screen.Settings) },
                    onNavigateToNoteEdit = { noteId ->
                        backStack.add(Screen.NoteEdit(noteId))
                    })
            }

            entry<Screen.Settings> {
                SettingsScreen(
                    onBack = { backStack.removeLastOrNull() })
            }

            entry<Screen.NoteEdit> { entry ->
                val viewModel: NoteEditViewModel = hiltViewModel(
                    creationCallback = { factory: NoteEditViewModel.Factory ->
                        factory.create(entry.noteId)
                    }
                )

                NoteEditScreen(viewModel = viewModel, onBack = { backStack.removeLastOrNull() })
            }
        })
}