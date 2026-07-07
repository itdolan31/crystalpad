package com.git.itdolan31.crystalpad.core.domain.model

enum class ThemeType {
    LIGHT,
    DARK,
    OLED,
    SYSTEM;

    companion object {
        val DEFAULT = SYSTEM

        fun fromName(name: String): ThemeType {
            return entries.find { it.name == name } ?: DEFAULT
        }
    }
}