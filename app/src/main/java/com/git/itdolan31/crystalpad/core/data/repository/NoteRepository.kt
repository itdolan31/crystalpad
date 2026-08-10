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

import com.git.itdolan31.crystalpad.core.data.local.room.dao.NoteDao
import com.git.itdolan31.crystalpad.core.data.local.room.entity.NoteEntity
import com.git.itdolan31.crystalpad.domain.model.NoteSortType
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    suspend fun insert(note: NoteEntity): Long = noteDao.insert(note)

    suspend fun update(note: NoteEntity) = noteDao.update(note)

    suspend fun delete(note: NoteEntity) = noteDao.delete(note)

    suspend fun moveNoteToTrash(noteId: Long) {
        noteDao.moveNoteToTrash(noteId, System.currentTimeMillis())
    }

    suspend fun restoreNoteFromTrash(noteId: Long) {
        noteDao.restoreNoteFromTrash(noteId)
    }

    suspend fun deleteExpiredTrashedNotes(trashRetention: Long) {
        noteDao.deleteExpiredTrashedNotes(System.currentTimeMillis() - trashRetention)
    }

    suspend fun clearTrash() = noteDao.clearTrash()

    suspend fun getNoteById(noteId: Long): NoteEntity? {
        return noteDao.getNoteById(noteId)
    }

    fun getNotes(sortType: NoteSortType): Flow<List<NoteEntity>> {
        return when (sortType) {
            NoteSortType.CREATED_AT_DESC -> noteDao.getNotesByCreatedAtDesc()
            NoteSortType.CREATED_AT_ASC -> noteDao.getNotesByCreatedAtAsc()
            NoteSortType.UPDATED_AT_DESC -> noteDao.getNotesByUpdatedAtDesc()
            NoteSortType.UPDATED_AT_ASC -> noteDao.getNotesByUpdatedAtAsc()
            NoteSortType.TITLE_ASC -> noteDao.getNotesByTitleAsc()
            NoteSortType.TITLE_DESC -> noteDao.getNotesByTitleDesc()
        }
    }

    fun getTrashedNotes(sortType: NoteSortType): Flow<List<NoteEntity>> {
        return when (sortType) {
            NoteSortType.CREATED_AT_DESC -> noteDao.getTrashedNotesByCreatedAtDesc()
            NoteSortType.CREATED_AT_ASC -> noteDao.getTrashedNotesByCreatedAtAsc()
            NoteSortType.UPDATED_AT_DESC -> noteDao.getTrashedNotesByUpdatedAtDesc()
            NoteSortType.UPDATED_AT_ASC -> noteDao.getTrashedNotesByUpdatedAtAsc()
            NoteSortType.TITLE_ASC -> noteDao.getTrashedNotesByTitleAsc()
            NoteSortType.TITLE_DESC -> noteDao.getTrashedNotesByTitleDesc()
        }
    }
}

