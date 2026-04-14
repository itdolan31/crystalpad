package com.git.itdolan31.crystalpad.data.repository

import com.git.itdolan31.crystalpad.data.local.room.dao.NoteDao
import com.git.itdolan31.crystalpad.data.local.room.entities.NoteEntity
import com.git.itdolan31.crystalpad.domain.model.NoteSortType
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

