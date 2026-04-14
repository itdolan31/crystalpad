package com.git.itdolan31.crystalpad.core.localization

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.git.itdolan31.crystalpad.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale

object Translator {
    private var _translations = mutableStateOf(JSONObject())
    private val supportedLanguages = setOf("de", "en", "es", "fr", "ja", "ko", "ru", "zh")
    private var isInitialized = false

    var language by mutableStateOf("en")
        private set

    private fun getLanguage(selected: String): String = when (selected) {
        "system" -> {
            val system = Locale.getDefault().language
            if (system in supportedLanguages) system else "en"
        }

        in supportedLanguages -> selected
        else -> "en"
    }

    suspend fun init(context: Context, settingsRepository: SettingsRepository) {
        if (isInitialized) return

        val appContext = context.applicationContext

        val savedLanguage = settingsRepository.languageFlow.first()
        val newLanguage = getLanguage(savedLanguage)

        language = newLanguage
        loadTranslations(appContext, newLanguage)
        isInitialized = true

        CoroutineScope(Dispatchers.Main).launch {
            settingsRepository.languageFlow.collect { savedLanguage ->
                val newLanguage = getLanguage(savedLanguage)
                if (language != newLanguage) {
                    language = newLanguage
                    loadTranslations(appContext, newLanguage)
                }
            }
        }
    }

    private fun loadTranslations(context: Context, lang: String) {
        try {
            val jsonString = context.assets.open("translations/$lang.json").bufferedReader()
                .use { it.readText() }
            _translations.value = JSONObject(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val jsonString = context.assets.open("translations/en.json").bufferedReader()
                    .use { it.readText() }
                _translations.value = JSONObject(jsonString)
            } catch (_: Exception) {
                _translations.value = JSONObject()
            }
        }
    }

    @Composable
    fun getString(key: String): String {
        val translations by _translations
        return translations.optString(key, key)
    }
}