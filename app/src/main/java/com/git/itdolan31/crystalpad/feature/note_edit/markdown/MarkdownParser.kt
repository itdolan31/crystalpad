package com.git.itdolan31.crystalpad.feature.note_edit.markdown

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.image.attributes.ImageAttributes
import org.commonmark.ext.ins.Ins
import org.commonmark.ext.task.list.items.TaskListItemMarker
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.HtmlBlock
import org.commonmark.node.HtmlInline
import org.commonmark.node.Image
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.ListBlock
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import org.commonmark.node.ThematicBreak

fun Node.toBlocks(codeBackground: Color, linkColor: Color): List<MdBlock> = buildList {
    var child = firstChild
    while (child != null) {
        when (child) {
            is Heading -> add(
                MdBlock.Heading(
                    child.level, buildInline(child, codeBackground, linkColor)
                )
            )

            is Paragraph -> addAll(child.toParagraphBlocks(codeBackground, linkColor))
            is FencedCodeBlock -> add(MdBlock.Code(child.literal.orEmpty(), child.info))
            is IndentedCodeBlock -> add(MdBlock.Code(child.literal.orEmpty()))
            is ThematicBreak -> add(MdBlock.Divider)
            is BulletList -> add(MdBlock.BulletList(child.toListItems(codeBackground, linkColor)))
            is OrderedList -> add(
                MdBlock.OrderedList(
                    child.markerStartNumber ?: 1, child.toListItems(codeBackground, linkColor)
                )
            )

            is BlockQuote -> add(MdBlock.Quote(child.toBlocks(codeBackground, linkColor)))
            is HtmlBlock -> add(MdBlock.Html(child.literal.orEmpty()))
            else -> Unit
        }
        child = child.next
    }
}

private fun Paragraph.toParagraphBlocks(
    codeBackground: Color,
    linkColor: Color,
): List<MdBlock> {
    val result = mutableListOf<MdBlock>()
    val buffer = mutableListOf<Node>()

    fun flush() {
        if (buffer.isEmpty()) return
        val annotated =
            buildAnnotatedString { buffer.forEach { appendInline(it, codeBackground, linkColor) } }
        if (annotated.isNotEmpty()) result += MdBlock.Paragraph(annotated)
        buffer.clear()
    }

    var child = firstChild
    while (child != null) {
        if (child is Image) {
            flush()
            result += child.toMdImage()
        } else buffer += child
        child = child.next
    }
    flush()
    return result
}

private fun ListBlock.toListItems(
    codeBackground: Color,
    linkColor: Color,
): List<List<MdBlock>> = buildList {
    var item = firstChild
    while (item != null) {
        if (item is ListItem) {
            val marker = item.firstChild as? TaskListItemMarker
            if (marker != null) {
                val sourceLine = item.sourceSpans.firstOrNull()?.lineIndex ?: 0
                add(
                    listOf(
                        MdBlock.TaskItem(
                            marker.isChecked,
                            item.toBlocks(codeBackground, linkColor),
                            sourceLine
                        )
                    )
                )
            } else {
                add(item.toBlocks(codeBackground, linkColor))
            }
        }
        item = item.next
    }
}

private fun buildInline(
    node: Node, codeBackground: Color, linkColor: Color
): AnnotatedString = buildAnnotatedString {
    var child = node.firstChild
    while (child != null) {
        appendInline(child, codeBackground, linkColor)
        child = child.next
    }
}

private fun AnnotatedString.Builder.appendInline(
    node: Node, codeBackground: Color, linkColor: Color
) {
    when (node) {
        is Text -> append(node.literal)
        is StrongEmphasis -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            var child = node.firstChild
            while (child != null) {
                appendInline(child, codeBackground, linkColor)
                child = child.next
            }
        }

        is Emphasis -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
            var child = node.firstChild
            while (child != null) {
                appendInline(child, codeBackground, linkColor)
                child = child.next
            }
        }

        is Strikethrough -> withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
            var child = node.firstChild
            while (child != null) {
                appendInline(child, codeBackground, linkColor)
                child = child.next
            }
        }

        is Ins -> withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
            var child = node.firstChild
            while (child != null) {
                appendInline(child, codeBackground, linkColor)
                child = child.next
            }
        }

        is Code -> withStyle(
            SpanStyle(
                fontFamily = FontFamily.Monospace, background = codeBackground
            ),
        ) {
            append(node.literal)
        }

        is Link -> withLink(
            LinkAnnotation.Url(
                url = node.destination,
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = linkColor, textDecoration = TextDecoration.Underline
                    )
                ),
            )
        ) {
            var child = node.firstChild
            while (child != null) {
                appendInline(child, codeBackground, linkColor)
                child = child.next
            }
        }

        is SoftLineBreak, is HardLineBreak -> append("\n")
        is HtmlInline -> append(node.literal)
        is Image -> Unit
        is TaskListItemMarker -> Unit
        else -> {
            var child = node.firstChild
            while (child != null) {
                appendInline(child, codeBackground, linkColor)
                child = child.next
            }
        }
    }
}

private fun Image.toMdImage(): MdBlock.Image {
    val attributes = generateSequence(firstChild) { it.next }.filterIsInstance<ImageAttributes>()
        .firstOrNull()?.attributes

    return MdBlock.Image(
        destination = destination,
        alt = alt(),
        width = attributes?.get("width")?.toIntOrNull(),
        height = attributes?.get("height")?.toIntOrNull(),
    )
}

private fun Image.alt(): String = generateSequence(firstChild) { it.next }.filterIsInstance<Text>()
    .firstOrNull()?.literal.orEmpty()