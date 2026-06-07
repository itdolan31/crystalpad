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
import com.git.itdolan31.crystalpad.core.data.local.room.entities.NoteEntity
import com.git.itdolan31.crystalpad.core.domain.model.NoteSortType
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    suspend fun insert(note: NoteEntity): Long = noteDao.insert(note)

    suspend fun update(note: NoteEntity) = noteDao.update(note)

    suspend fun delete(note: NoteEntity) = noteDao.delete(note)

    suspend fun getNoteById(noteId: Long): NoteEntity? {
        return noteDao.getNoteById(noteId)
    }

    fun getNotes(sortType: NoteSortType): Flow<List<NoteEntity>> {
        return when (sortType) {
            NoteSortType.DATE_DESC -> noteDao.getNotesByDateDesc()
            NoteSortType.DATE_ASC -> noteDao.getNotesByDateAsc()
            NoteSortType.TITLE_ASC -> noteDao.getNotesByTitleAsc()
            NoteSortType.TITLE_DESC -> noteDao.getNotesByTitleDesc()
        }
    }
}

