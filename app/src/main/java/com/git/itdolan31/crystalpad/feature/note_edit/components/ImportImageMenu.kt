package com.git.itdolan31.crystalpad.feature.note_edit.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.git.itdolan31.crystalpad.R
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportImageMenu(
    onDismiss: () -> Unit,
    onDeleteSelected: () -> Unit,
    onAddPhoto: () -> Unit,
    onImageClick: (File) -> Unit,
    onToggleSelection: (File) -> Unit,
    images: List<File>,
    selectedImages: List<File>
) {
    ModalBottomSheet(onDismissRequest = onDismiss, dragHandle = {
        if (selectedImages.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDeleteSelected) {
                    Icon(
                        painter = painterResource(
                            R.drawable.ic_delete
                        ), contentDescription = stringResource(R.string.delete)
                    )
                }
            }
        } else {
            BottomSheetDefaults.DragHandle()
        }
    }) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable {
                            onAddPhoto()
                        },
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_add_photo_alternate),
                        contentDescription = stringResource(R.string.import_photo),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            items(images, key = { it.absolutePath }) { file ->
                ImageItem(
                    file,
                    selectedIndex = selectedImages.indexOf(file).takeIf { it >= 0 }?.plus(1),
                    onToggleSelection = { onToggleSelection(file) },
                    onClick = { onImageClick(file) })
            }
        }
    }
}