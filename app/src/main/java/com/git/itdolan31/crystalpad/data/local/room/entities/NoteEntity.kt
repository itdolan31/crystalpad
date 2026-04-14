package com.git.itdolan31.crystalpad.data.local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("notes")
data class NoteEntity(
    @PrimaryKey(true)
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)