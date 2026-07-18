package com.git.itdolan31.crystalpad.features.trash

import com.git.itdolan31.crystalpad.core.data.local.room.entities.NoteEntity

data class TrashUiState(
    var selectedNote: NoteEntity? = null,
)
