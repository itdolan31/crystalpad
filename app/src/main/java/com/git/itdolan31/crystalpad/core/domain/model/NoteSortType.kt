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
package com.git.itdolan31.crystalpad.core.domain.model

enum class NoteSortType {
    CREATED_AT_DESC,
    CREATED_AT_ASC,
    UPDATED_AT_DESC,
    UPDATED_AT_ASC,
    TITLE_ASC,
    TITLE_DESC;

    companion object {
        val DEFAULT = CREATED_AT_DESC

        fun fromName(name: String): NoteSortType {
            return entries.find { it.name == name } ?: DEFAULT
        }
    }
}