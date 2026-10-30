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