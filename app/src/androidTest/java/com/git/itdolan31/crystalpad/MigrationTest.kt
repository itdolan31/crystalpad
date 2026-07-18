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
package com.git.itdolan31.crystalpad

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.git.itdolan31.crystalpad.core.data.local.room.AppDatabase
import com.git.itdolan31.crystalpad.core.data.local.room.Migrations
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val dbName = "crystalpad_database"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate() {
        helper.createDatabase(dbName, 1).apply {
            execSQL("CREATE TABLE IF NOT EXISTS notes (id INTEGER PRIMARY KEY, title TEXT NOT NULL, content TEXT NOT NULL, timestamp INTEGER NOT NULL)")
            execSQL("INSERT INTO notes (id, title, content, timestamp) VALUES (1, 'Title 1', 'Content 1', 1000)")
            close()
        }

        helper.runMigrationsAndValidate(dbName, 2, true, Migrations.MIGRATION_1_2).use { db ->
            db.query("SELECT * FROM notes WHERE id = 1").use { cursor ->
                cursor.moveToFirst()
                assertEquals(1, cursor.getLong(cursor.getColumnIndex("id")))
                assertEquals("Title 1", cursor.getString(cursor.getColumnIndex("title")))
                assertEquals("Content 1", cursor.getString(cursor.getColumnIndex("content")))
                assertEquals(1000L, cursor.getLong(cursor.getColumnIndex("created_at")))
                assertEquals(1000L, cursor.getLong(cursor.getColumnIndex("updated_at")))
            }
        }
    }
}