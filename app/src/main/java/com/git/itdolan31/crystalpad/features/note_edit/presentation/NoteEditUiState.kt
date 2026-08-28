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
package com.git.itdolan31.crystalpad.features.note_edit.presentation

import androidx.compose.foundation.text.input.TextFieldState

data class NoteEditUiState(
    val titleState: TextFieldState = TextFieldState(),
    val contentState: TextFieldState = TextFieldState(),
    val originalTitle: String = "",
    val originalContent: String = "",
    val isDeleted: Boolean = false,
    val isNewNote: Boolean = false
) {
    fun canSave(): Boolean {
        val title = titleState.text.toString()
        val content = contentState.text.toString()
        return (title != originalTitle || content != originalContent) && !isDeleted && !(isNewNote && title.isBlank() && content.isBlank())
    }
}