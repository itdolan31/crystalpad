package com.git.itdolan31.crystalpad.core.utils

import androidx.appcompat.app.AppCompatDelegate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDateTime(timestamp: Long, pattern: String): String {
    return SimpleDateFormat(
        pattern, AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()
    ).format(Date(timestamp))
}