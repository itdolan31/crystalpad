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
package com.git.itdolan31.crystalpad.core.data.repository

import android.content.Context
import com.git.itdolan31.crystalpad.core.data.local.room.dao.NoteDao
import com.git.itdolan31.crystalpad.core.data.local.room.entity.NoteEntity
import com.git.itdolan31.crystalpad.domain.model.NoteSortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class NoteRepository(
    private val noteDao: NoteDao,
    private val context: Context
) {
    suspend fun insert(note: NoteEntity): Long = noteDao.insert(note)

    suspend fun update(note: NoteEntity) = noteDao.update(note)

    suspend fun delete(note: NoteEntity) = withContext(Dispatchers.IO) {
        File(context.filesDir, "media/${note.id}").deleteRecursively()
        noteDao.delete(note)
    }

    suspend fun moveNoteToTrash(noteId: Long) {
        noteDao.moveNoteToTrash(noteId, System.currentTimeMillis())
    }

    suspend fun restoreNoteFromTrash(noteId: Long) {
        noteDao.restoreNoteFromTrash(noteId)
    }

    suspend fun deleteExpiredTrashedNotes(trashRetention: Long) = withContext(Dispatchers.IO) {
        val cutoffTime = System.currentTimeMillis() - trashRetention

        val expiredNoteIds = noteDao.getExpiredTrashedNoteIds(cutoffTime)

        expiredNoteIds.forEach { id ->
            File(context.filesDir, "media/$id").deleteRecursively()
        }

        noteDao.deleteExpiredTrashedNotes(cutoffTime)
    }

    suspend fun clearTrash() = withContext(Dispatchers.IO) {
        val trashedNoteIds = noteDao.getTrashedNoteIds()

        trashedNoteIds.forEach { id ->
            File(context.filesDir, "media/$id").deleteRecursively()
        }

        noteDao.clearTrash()
    }

    suspend fun getNoteById(noteId: Long): NoteEntity? {
        return noteDao.getNoteById(noteId)
    }

    fun getNotes(
        sortType: NoteSortType = NoteSortType.DEFAULT,
        isTrashed: Boolean = false
    ): Flow<List<NoteEntity>> {
        return when (sortType) {
            NoteSortType.CREATED_AT_DESC -> noteDao.getNotesByCreatedAtDesc(isTrashed)
            NoteSortType.CREATED_AT_ASC -> noteDao.getNotesByCreatedAtAsc(isTrashed)
            NoteSortType.UPDATED_AT_DESC -> noteDao.getNotesByUpdatedAtDesc(isTrashed)
            NoteSortType.UPDATED_AT_ASC -> noteDao.getNotesByUpdatedAtAsc(isTrashed)
            NoteSortType.TITLE_DESC -> noteDao.getNotesByTitleDesc(isTrashed)
            NoteSortType.TITLE_ASC -> noteDao.getNotesByTitleAsc(isTrashed)
        }
    }
}

