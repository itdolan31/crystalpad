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

object SettingsConstants {
    const val DEFAULT_THEME = "system"
    val DEFAULT_SORT_TYPE = NoteSortType.DATE_DESC
    const val DEFAULT_KEEP_SCREEN_ON = false
    const val DEFAULT_BIOMETRY = false
    const val DEFAULT_TIMEOUT = 0
    val DEFAULT_DATE_PATTERN = DatePatternType.DD_MM_YYYY_DOT
    val DEFAULT_TIME_PATTERN = TimePatternType.HH_MM
    const val DEFAULT_FONT_SIZE = 16
    const val DEFAULT_FLAG_SECURE = true
}