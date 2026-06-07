package com.git.itdolan31.crystalpad.core.domain.model

enum class DatePatternType(val pattern: String) {
    DD_MM_YYYY_SLASH(
        "dd/MM/yyyy"
    ),
    MM_DD_YYYY(
        "MM/dd/yyyy"
    ),
    MM_DD_YY(
        "MM/dd/yy"
    ),

    YYYY_MM_DD(
        "yyyy-MM-dd"
    ),
    DD_MM_YYYY_DOT(
        "dd.MM.yyyy"
    ),
    YYMMDD(
        "yyMMdd"
    ),
    YYYY_MM_DD_SLASH(
        "yyyy/MM/dd"
    )
}