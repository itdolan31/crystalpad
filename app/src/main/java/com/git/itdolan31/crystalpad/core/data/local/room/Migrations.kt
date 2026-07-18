package com.git.itdolan31.crystalpad.core.data.local.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.apply {
                execSQL(
                    """
                        CREATE TABLE notes_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            title TEXT NOT NULL,
                            content TEXT NOT NULL,
                            created_at INTEGER NOT NULL DEFAULT 0,
                            updated_at INTEGER NOT NULL DEFAULT 0,
                            is_trashed INTEGER NOT NULL DEFAULT 0,
                            trashed_at INTEGER DEFAULT NULL
                        )
                    """.trimIndent()
                )

                execSQL(
                    """
                        INSERT INTO notes_new (id, title, content, created_at, updated_at, is_trashed, trashed_at)
                        SELECT id, REPLACE(title, '\n', ' ') AS title, content, timestamp, timestamp, 0, NULL FROM notes
                    """.trimIndent()
                )

                execSQL("DROP TABLE notes")
                execSQL("ALTER TABLE notes_new RENAME TO notes")
            }
        }
    }
}
