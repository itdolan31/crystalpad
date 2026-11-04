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
