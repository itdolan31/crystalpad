package com.git.itdolan31.crystalpad.core.domain.model

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