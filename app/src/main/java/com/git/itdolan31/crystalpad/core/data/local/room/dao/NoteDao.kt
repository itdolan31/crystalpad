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
package com.git.itdolan31.crystalpad.core.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.git.itdolan31.crystalpad.core.data.local.room.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("UPDATE notes SET is_trashed = 1, trashed_at = :trashedAt WHERE id = :noteId")
    suspend fun moveNoteToTrash(noteId: Long, trashedAt: Long)

    @Query("UPDATE notes SET is_trashed = 0, trashed_at = NULL WHERE id = :noteId")
    suspend fun restoreNoteFromTrash(noteId: Long)

    @Query("DELETE FROM notes WHERE is_trashed = 1 AND trashed_at < :cutoffTime")
    suspend fun deleteExpiredTrashedNotes(cutoffTime: Long)

    @Query("DELETE FROM notes WHERE is_trashed = 1")
    suspend fun clearTrash()

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE is_trashed = 0 ORDER BY created_at DESC")
    fun getNotesByCreatedAtDesc(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_trashed = 0 ORDER BY created_at ASC")
    fun getNotesByCreatedAtAsc(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_trashed = 0 ORDER BY updated_at DESC")
    fun getNotesByUpdatedAtDesc(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_trashed = 0 ORDER BY updated_at ASC")
    fun getNotesByUpdatedAtAsc(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_trashed = 0 ORDER BY title ASC")
    fun getNotesByTitleAsc(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_trashed = 0 ORDER BY title DESC")
    fun getNotesByTitleDesc(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_trashed = 1 ORDER BY created_at DESC")
    fun getTrashedNotesByCreatedAtDesc(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_trashed = 1 ORDER BY created_at ASC")
    fun getTrashedNotesByCreatedAtAsc(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_trashed = 1 ORDER BY updated_at DESC")
    fun getTrashedNotesByUpdatedAtDesc(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_trashed = 1 ORDER BY updated_at ASC")
    fun getTrashedNotesByUpdatedAtAsc(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_trashed = 1 ORDER BY title ASC")
    fun getTrashedNotesByTitleAsc(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_trashed = 1 ORDER BY title DESC")
    fun getTrashedNotesByTitleDesc(): Flow<List<NoteEntity>>
}