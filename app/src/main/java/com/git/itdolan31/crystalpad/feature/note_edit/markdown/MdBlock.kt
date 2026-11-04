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
package com.git.itdolan31.crystalpad.feature.note_edit.markdown

import androidx.compose.ui.text.AnnotatedString

sealed interface MdBlock {
    data class Heading(val level: Int, val content: AnnotatedString) : MdBlock
    data class Paragraph(val content: AnnotatedString) : MdBlock
    data class BulletList(val items: List<List<MdBlock>>) : MdBlock
    data class OrderedList(val start: Int, val items: List<List<MdBlock>>) : MdBlock
    data class TaskItem(val checked: Boolean, val children: List<MdBlock>, val sourceLine: Int) :
        MdBlock

    data object Divider : MdBlock
    data class Code(val literal: String, val lang: String? = null) : MdBlock
    data class Quote(val children: List<MdBlock>) : MdBlock
    data class Image(
        val destination: String,
        val alt: String,
        val width: Int? = null,
        val height: Int? = null,
    ) : MdBlock

    data class Html(val literal: String) : MdBlock
}

