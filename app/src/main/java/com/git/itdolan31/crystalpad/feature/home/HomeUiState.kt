package com.git.itdolan31.crystalpad.feature.home

import com.git.itdolan31.crystalpad.core.data.local.room.entity.NoteEntity

data class HomeUiState(
    var selectedNote: NoteEntity? = null,
)