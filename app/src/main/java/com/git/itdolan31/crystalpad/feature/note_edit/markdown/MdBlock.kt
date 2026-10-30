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

