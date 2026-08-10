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
package com.git.itdolan31.crystalpad.domain.model

enum class DatePatternType(val pattern: String) {
    DD_MM_YYYY_DOT("dd.MM.yyyy"),
    DD_MM_YYYY_SLASH("dd/MM/yyyy"),
    DD_MM_YYYY_DASH("dd-MM-yyyy"),

    YYYY_MM_DOT("yyyy.MM.dd"),
    YYYY_MM_SLASH("yyyy/MM/dd"),
    YYYY_MM_DASH("yyyy-MM-dd"),

    MM_DD_YYYY_DOT("MM.dd.yyyy"),
    MM_DD_YYYY_SLASH("MM/dd/yyyy"),
    MM_DD_YYYY_DASH("MM-dd-yyyy");

    companion object {
        val DEFAULT = DD_MM_YYYY_DOT

        fun fromName(name: String): DatePatternType {
            return entries.find { it.name == name } ?: DEFAULT
        }
    }
}