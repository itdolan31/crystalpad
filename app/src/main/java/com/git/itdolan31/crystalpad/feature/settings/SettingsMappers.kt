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
package com.git.itdolan31.crystalpad.feature.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.git.itdolan31.crystalpad.R
import com.git.itdolan31.crystalpad.domain.model.ThemeType

object SettingsOptions {
    val lockTimeouts: Map<Long, Int> = mapOf(
        -1L to R.string.never,
        0L to R.string.immediately,
        60L to R.string.minute_1,
        300L to R.string.minutes_5,
        900L to R.string.minutes_15,
        1800L to R.string.minutes_30,
        3600L to R.string.hour_1
    )

    val trashRetentions: Map<Long, Int> = mapOf(
        86_400L to R.string.days_1,
        259_200L to R.string.days_3,
        604_800L to R.string.days_7,
        1_296_000L to R.string.days_15,
        2_592_000L to R.string.days_30
    )
}

@DrawableRes
fun ThemeType.getIconRes(): Int = when (this) {
    ThemeType.LIGHT -> R.drawable.ic_light_mode
    ThemeType.DARK -> R.drawable.ic_dark_mode
    ThemeType.OLED -> R.drawable.ic_contrast
    ThemeType.SYSTEM -> R.drawable.ic_brightness_4
}

@StringRes
fun ThemeType.getTitleRes(): Int = when (this) {
    ThemeType.LIGHT -> R.string.light
    ThemeType.DARK -> R.string.dark
    ThemeType.OLED -> R.string.oled
    ThemeType.SYSTEM -> R.string.system
}