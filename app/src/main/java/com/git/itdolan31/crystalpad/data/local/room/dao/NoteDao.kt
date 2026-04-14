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