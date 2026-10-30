package com.git.itdolan31.crystalpad.feature.note_edit.markdown

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.git.itdolan31.crystalpad.R

@Composable
fun MarkdownContent(
    blocks: List<MdBlock>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onToggleTask: (sourceLine: Int) -> Unit
) {
    LazyColumn(modifier, state, contentPadding = contentPadding) {
        itemsIndexed(blocks) { _, block -> RenderBlock(block, onToggleTask = onToggleTask) }
    }
}

@Composable
private fun RenderBlock(block: MdBlock, onToggleTask: (sourceLine: Int) -> Unit) {
    when (block) {
        is MdBlock.Heading -> RenderHeading(block)
        is MdBlock.Paragraph -> RenderParagraph(block)
        is MdBlock.BulletList -> RenderList(
            block.items,
            ordered = false,
            onToggleTask = onToggleTask
        )

        is MdBlock.OrderedList -> RenderList(
            block.items,
            ordered = true,
            start = block.start,
            onToggleTask = onToggleTask
        )

        is MdBlock.Divider -> RenderDivider()
        is MdBlock.Code -> RenderCode(block)
        is MdBlock.Quote -> RenderQuote(block, onToggleTask = onToggleTask)
        is MdBlock.Image -> RenderImage(block)
        is MdBlock.Html -> RenderHtml(block)
        is MdBlock.TaskItem -> Unit
    }
}

@Composable
private fun RenderHeading(block: MdBlock.Heading) {
    Text(
        text = block.content, modifier = Modifier.padding(
            top = if (block.level <= 2) 28.dp else 20.dp,
            bottom = if (block.level <= 2) 12.dp else 8.dp,
        ), style = when (block.level) {
            1 -> MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold)
            2 -> MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold)
            3 -> MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
            4 -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            5 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            else -> MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    )
}

@Composable
private fun RenderParagraph(block: MdBlock.Paragraph) {
    Text(
        text = block.content,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(bottom = 12.dp),
    )
}

@Composable
private fun RenderList(
    items: List<List<MdBlock>>,
    ordered: Boolean,
    start: Int = 1,
    depth: Int = 0,
    onToggleTask: (sourceLine: Int) -> Unit
) {
    Column(
        Modifier.padding(
            start = if (depth == 0) 8.dp else 12.dp,
            bottom = 12.dp,
        )
    ) {
        items.forEachIndexed { index, item ->
            val task = item.firstOrNull() as? MdBlock.TaskItem
            Row(
                Modifier
                    .padding(bottom = 4.dp)
            ) {
                if (task != null) {
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                        Checkbox(
                            checked = task.checked,
                            onCheckedChange = { onToggleTask(task.sourceLine) }
                        )
                    }
                } else {
                    Text(
                        text = if (ordered) "${start + index}." else "•",
                        modifier = Modifier.width(if (ordered) 24.dp else 16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Column(
                    Modifier
                        .padding(start = 4.dp)
                        .weight(1f)
                ) {
                    if (task != null) {
                        RenderListItemContent(task.children, depth, onToggleTask = onToggleTask)
                    } else {
                        RenderListItemContent(item, depth, onToggleTask = onToggleTask)
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderListItemContent(blocks: List<MdBlock>, depth: Int, onToggleTask: (Int) -> Unit) {
    blocks.forEach { block ->
        when (block) {
            is MdBlock.BulletList -> {
                RenderList(
                    block.items,
                    ordered = false,
                    depth = depth + 1,
                    onToggleTask = onToggleTask
                )
            }

            is MdBlock.OrderedList -> {
                RenderList(
                    block.items,
                    ordered = true,
                    start = block.start,
                    depth = depth + 1,
                    onToggleTask = onToggleTask
                )
            }

            else -> {
                RenderBlock(block, onToggleTask = onToggleTask)
            }
        }
    }
}

@Composable
private fun RenderDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 20.dp)
    )
}

@Composable
private fun RenderCode(block: MdBlock.Code) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)
            )
            .padding(14.dp)
    ) {
        block.lang?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        Text(
            text = block.literal,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun RenderImage(block: MdBlock.Image) {
    val noteId = LocalNoteId.current
    val context = LocalContext.current

    if (noteId == null) {
        Icon(
            painter = painterResource(R.drawable.ic_broken_image),
            contentDescription = null,
        )
        return
    }

    val fileName = block.destination.removePrefix("file://")
    if (fileName.contains("..") || fileName.contains('/')) {
        Icon(
            painter = painterResource(R.drawable.ic_broken_image),
            contentDescription = null,
        )
        return
    }

    val painter = rememberAsyncImagePainter(
        model = "file://${context.filesDir.absolutePath}/media/$noteId/$fileName"
    )
    val state by painter.state.collectAsStateWithLifecycle()

    when (state) {
        is AsyncImagePainter.State.Error -> {
            Icon(
                painter = painterResource(R.drawable.ic_broken_image),
                contentDescription = null,
            )
        }

        else -> {
            val width = block.width
            val height = block.height

            val modifier = when {
                width != null && height != null -> Modifier.size(width.dp, height.dp)
                width != null -> Modifier.width(width.dp)
                height != null -> Modifier.height(height.dp)
                else -> Modifier.fillMaxWidth()
            }

            Image(
                painter = painter,
                contentDescription = block.alt.ifBlank { null },
                modifier = modifier.padding(bottom = 12.dp),
                contentScale = if (width != null || height != null) {
                    ContentScale.FillBounds
                } else {
                    ContentScale.FillWidth
                },
            )
        }
    }
}

@Composable
private fun RenderQuote(block: MdBlock.Quote, onToggleTask: (Int) -> Unit) {
    Row(
        Modifier
            .height(IntrinsicSize.Min)
            .padding(vertical = 4.dp)
    ) {
        Box(
            Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        )
        Column(Modifier.padding(start = 12.dp, end = 4.dp)) {
            block.children.forEach { RenderBlock(it, onToggleTask = onToggleTask) }
        }
    }
}

@Composable
private fun RenderHtml(block: MdBlock.Html) {
    Text(
        text = block.literal,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(bottom = 12.dp),
    )
}