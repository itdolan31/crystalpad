package com.git.itdolan31.crystalpad.manager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.git.itdolan31.crystalpad.data.local.preferences.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

object TranslationManager {
    private val translations = mapOf(
        "en" to mapOf(
            "delete" to "Delete",
            "cancel" to "Cancel",
            "delete_confirmation_title" to "Are you sure?",
            "delete_confirmation_message" to "The note will be deleted without the possibility of recovery.",
            "save" to "Save",
            "title" to "Title",
            "note" to "Note",
            "system" to "System",
            "dark" to "Dark",
            "light" to "Light",
            "language" to "Language",
            "theme" to "Theme",
            "settings" to "Settings",
            "source_code" to "Source code"
        ),
        "ru" to mapOf(
            "delete" to "Удалить",
            "cancel" to "Отмена",
            "delete_confirmation_title" to "Вы уверены?",
            "delete_confirmation_message" to "Заметка будет удалена без возможности восстановления.",
            "save" to "Сохранить",
            "title" to "Заголовок",
            "note" to "Заметка",
            "system" to "Системная",
            "dark" to "Тёмная",
            "light" to "Светлая",
            "language" to "Язык",
            "theme" to "Тема",
            "settings" to "Настройки",
            "source_code" to "Исходный код"
        ),
    )

    private val supportedLanguages = translations.keys

    var language by mutableStateOf("en")
        private set

    fun init(settingsManager: SettingsManager) {
        val systemLang = Locale.getDefault().language
        language = if (systemLang in supportedLanguages) systemLang else "en"

        CoroutineScope(Dispatchers.Main).launch {
            settingsManager.languageFlow.collect { savedLanguage ->
                val systemLanguage = Locale.getDefault().language

                val newLanguage = when (savedLanguage) {
                    "system" -> {
                        if (systemLanguage in supportedLanguages) systemLanguage else "en"
                    }

                    in supportedLanguages -> savedLanguage
                    else -> "en"
                }

                if (language != newLanguage) {
                    language = newLanguage
                }
            }
        }
    }

    fun getString(key: String): String {
        return translations[language]?.get(key) ?: key
    }
}