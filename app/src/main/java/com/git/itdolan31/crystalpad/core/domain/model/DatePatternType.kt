package com.git.itdolan31.crystalpad.core.domain.model

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