package com.git.itdolan31.crystalpad.feature.note_edit.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.git.itdolan31.crystalpad.R
import java.io.File

@Composable
fun ImageItem(file: File, selectedIndex: Int?, onToggleSelection: () -> Unit, onClick: () -> Unit) {
    Box(
        Modifier
            .aspectRatio(1f)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onLongClick = onToggleSelection,
                onClick = onClick
            )
    ) {
        val selectionPadding by animateDpAsState(
            targetValue = if (selectedIndex != null) 10.dp else 1.dp,
            animationSpec = tween(200),
            label = "selectionPadding"
        )

        Box(
            Modifier.padding(selectionPadding)
        ) {
            val painter = rememberAsyncImagePainter(model = file, contentScale = ContentScale.Crop)
            val state by painter.state.collectAsStateWithLifecycle()

            if (state is AsyncImagePainter.State.Error) {
                Icon(
                    painter = painterResource(R.drawable.ic_broken_image),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(4.dp))
                )
            } else {
                Image(
                    painter,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Box(
            Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Checkbox,
                    onClick = { onToggleSelection() },
                )
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(24.dp)
                .background(
                    if (selectedIndex != null) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    }, CircleShape
                )
                .then(
                    if (selectedIndex != null) {
                        Modifier
                    } else {
                        Modifier.border(1.5.dp, Color.White, CircleShape)
                    }
                ), contentAlignment = Alignment.Center
        ) {
            if (selectedIndex != null) {
                Text(
                    text = selectedIndex.toString(), color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}