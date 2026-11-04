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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.image.attributes.ImageAttributesExtension
import org.commonmark.ext.ins.InsExtension
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.parser.IncludeSourceSpans
import org.commonmark.parser.Parser

private val markdownParser =
    Parser.builder().includeSourceSpans(IncludeSourceSpans.BLOCKS).extensions(
        listOf(
            ImageAttributesExtension.create(),
            AutolinkExtension.create(),
            StrikethroughExtension.create(),
            InsExtension.create(),
            TaskListItemsExtension.create()
        )
    ).build()

val LocalNoteId = staticCompositionLocalOf<Long?> { null }

@Composable
fun Markdown(
    text: String,
    noteId: Long?,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onToggleTask: (sourceLine: Int) -> Unit = {},
) {
    val codeBackground = MaterialTheme.colorScheme.surfaceVariant
    val linkColor = MaterialTheme.colorScheme.primary
    var blocks by remember { mutableStateOf<List<MdBlock>?>(null) }

    LaunchedEffect(text, noteId, codeBackground, linkColor) {
        blocks = withContext(Dispatchers.Default) {
            markdownParser.parse(text).toBlocks(codeBackground, linkColor)
        }
    }

    CompositionLocalProvider(LocalNoteId provides noteId) {
        blocks?.let {
            MarkdownContent(
                it,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 0.dp),
                state = state,
                contentPadding = contentPadding,
                onToggleTask = onToggleTask
            )
        }
    }
}