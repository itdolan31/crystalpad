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

enum class TimePatternType(val pattern: String) {
    HH_MM("HH:mm"),
    H_MM("h:mm"),
    HH_MM_DOT("HH.mm"),
    H_MM_DOT("h.mm"),

    HH_MM_12("HH:mm a"),
    H_MM_12("h:mm a"),
    HH_MM_DOT_12("HH.mm a"),
    H_MM_DOT_12("h.mm a");

    companion object {
        val DEFAULT = HH_MM

        fun fromName(name: String): TimePatternType {
            return entries.find { it.name == name } ?: DEFAULT
        }
    }
}