package com.git.itdolan31.crystalpad.features.home

import com.git.itdolan31.crystalpad.core.data.local.room.entities.NoteEntity

data class HomeUiState(
    var selectedNote: NoteEntity? = null,
)