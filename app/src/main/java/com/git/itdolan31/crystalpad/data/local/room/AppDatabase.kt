package com.git.itdolan31.crystalpad.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.git.itdolan31.crystalpad.data.local.room.dao.NoteDao
import com.git.itdolan31.crystalpad.data.local.room.entities.NoteEntity

@Database(
    version = 1,
    entities = [NoteEntity::class],
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context = context.applicationContext,
                    klass = AppDatabase::class.java,
                    name = "crystalpad_database"
                ).build(

                )
                INSTANCE = instance
                instance
            }
        }
    }
}
