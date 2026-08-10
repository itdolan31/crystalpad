package com.git.itdolan31.crystalpad.features.home.presentation

import com.git.itdolan31.crystalpad.core.data.local.room.entity.NoteEntity

data class HomeUiState(
    var selectedNote: NoteEntity? = null,
)