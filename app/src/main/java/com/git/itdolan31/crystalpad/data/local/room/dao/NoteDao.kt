/*
 * Crystalpad
 * Copyright (C) 2026 itdolan31
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.git.itdolan31.crystalpad.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.git.itdolan31.crystalpad.data.local.room.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Long): NoteEntity?

    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getNotesByDateDesc(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY timestamp ASC")
    fun getNotesByDateAsc(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY title ASC")
    fun getNotesByTitleAsc(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY title DESC")
    fun getNotesByTitleDesc(): Flow<List<NoteEntity>>
}