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
package com.git.itdolan31.crystalpad.features.settings

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.git.itdolan31.crystalpad.R
import com.git.itdolan31.crystalpad.core.data.local.room.entities.NoteEntity
import com.git.itdolan31.crystalpad.core.data.repository.NoteRepository
import com.git.itdolan31.crystalpad.core.data.repository.SettingsRepository
import com.git.itdolan31.crystalpad.core.domain.model.DatePatternType
import com.git.itdolan31.crystalpad.core.domain.model.SettingsConstants
import com.git.itdolan31.crystalpad.core.domain.model.ThemeType
import com.git.itdolan31.crystalpad.core.domain.model.TimePatternType
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val noteRepository: NoteRepository, private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val password: StateFlow<String> = settingsRepository.passwordFlow.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = ""
    )

    val notes: StateFlow<List<NoteEntity>> =
        noteRepository.getNotes(SettingsConstants.DEFAULT_SORT_TYPE).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val themeType: StateFlow<ThemeType> = settingsRepository.themeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_THEME
    )

    val isKeepScreenOn: StateFlow<Boolean> = settingsRepository.keepScreenOnFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_KEEP_SCREEN_ON
    )

    val isPasswordSet: StateFlow<Boolean> = password.map { it.isNotEmpty() }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = false
    )

    val isBiometryEnabled: StateFlow<Boolean> = settingsRepository.biometryFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_BIOMETRY
    )

    val lockTimeout: StateFlow<Int> = settingsRepository.timeoutFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_TIMEOUT
    )

    val datePattern = settingsRepository.datePatternFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_DATE_PATTERN
    )

    val timePattern = settingsRepository.timePatternFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_TIME_PATTERN
    )

    val fontSize: StateFlow<Int> = settingsRepository.fontSizeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_FONT_SIZE
    )

    val isFlagSecureEnabled: StateFlow<Boolean> = settingsRepository.flagSecureFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_FLAG_SECURE
    )

    val isDynamicColorEnabled: StateFlow<Boolean> = settingsRepository.dynamicColorFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_DYNAMIC_COLOR
    )

    val isTrashEnabled: StateFlow<Boolean> = settingsRepository.trashFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_TRASH
    )

    val trashRetention: StateFlow<Long> = settingsRepository.trashRetentionFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_TRASH_RETENTION
    )

    fun getEffectiveLocaleDisplayName(): String {
        val locales = AppCompatDelegate.getApplicationLocales()

        return if (locales.isEmpty) {
            Locale.getDefault()
        } else {
            locales[0] ?: Locale.getDefault()
        }.displayLanguage
    }

    fun setTheme(type: ThemeType) {
        viewModelScope.launch {
            settingsRepository.saveTheme(type)
        }
    }

    fun setKeepScreenOn(keepScreenOn: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveKeepScreenOn(keepScreenOn)
        }
    }

    private fun setPassword(password: String) {
        viewModelScope.launch {
            settingsRepository.savePassword(password)
        }
    }

    fun setBiometryEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveBiometry(enabled)
        }
    }

    fun setLockTimeout(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.saveTimeout(seconds)
        }
    }

    fun setDatePattern(pattern: DatePatternType) {
        viewModelScope.launch {
            settingsRepository.saveDatePattern(pattern)
        }
    }

    fun setTimePattern(pattern: TimePatternType) {
        viewModelScope.launch {
            settingsRepository.saveTimePattern(pattern)
        }
    }

    fun setFontSize(size: Int) {
        viewModelScope.launch {
            settingsRepository.saveFontSize(size)
        }
    }

    fun setFlagSecureEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveFlagSecure(enabled)
        }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveDynamicColor(enabled)
        }
    }

    fun setTrashEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveTrash(enabled)
        }
    }

    fun setTrashRetention(millis: Long) {
        viewModelScope.launch {
            settingsRepository.saveTrashRetention(millis)
        }
    }

    fun setNewPassword(newPassword: String, confirmPassword: String): Pair<Boolean, Int> {
        return when {
            newPassword.length < 4 -> {
                true to R.string.password_min_length
            }

            newPassword != confirmPassword -> {
                true to R.string.password_mismatch
            }

            else -> {
                setPassword(newPassword)
                false to R.string.password_set_success
            }
        }
    }

    fun resetPassword(currentPassword: String): Pair<Boolean, Int> {
        return if (currentPassword == password.value) {
            setPassword("")
            false to R.string.password_reset_success
        } else {
            true to R.string.password_incorrect
        }
    }

    suspend fun exportNotes(context: Context, uri: Uri): String {
        var result = context.getString(R.string.export_successful)

        withContext(Dispatchers.IO) {
            val tempZip = File(context.cacheDir, "export-${System.currentTimeMillis()}.zip")

            try {
                ZipOutputStream(FileOutputStream(tempZip)).use { zipOut ->
                    zipOut.putNextEntry(ZipEntry("manifest.json"))
                    zipOut.write(
                        JSONObject().apply {
                            put("version", 2)
                        }.toString().toByteArray()
                    )
                    zipOut.closeEntry()

                    zipOut.putNextEntry(ZipEntry("notes/"))
                    zipOut.closeEntry()

                    notes.value.forEach { note ->
                        val folder = "notes/${note.id}/"

                        zipOut.putNextEntry(ZipEntry(folder))
                        zipOut.closeEntry()

                        zipOut.putNextEntry(ZipEntry("${folder}note.json"))
                        zipOut.write(
                            JSONObject().apply {
                                put("title", note.title)
                                put("content", note.content)
                                put("created_at", note.createdAt)
                                put("updated_at", note.updatedAt)
                            }.toString().toByteArray()
                        )
                        zipOut.closeEntry()
                    }
                }

                val outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream == null) {
                    result = context.getString(R.string.error_file_not_found, uri.toString())
                    return@withContext
                }

                outputStream.use { output ->
                    tempZip.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }

                result = context.getString(R.string.export_successful)
            } catch (e: Exception) {
                result = "$e"
            } finally {
                tempZip.delete()
            }
        }
        return result
    }

    suspend fun importNotes(context: Context, uri: Uri): String {
        var result = context.getString(R.string.import_successful)

        withContext(Dispatchers.IO) {
            val tempZip = File(context.cacheDir, "import-${System.currentTimeMillis()}.zip")

            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                result = context.getString(R.string.error_file_not_found, uri.toString())
                return@withContext
            }

            inputStream.use { input ->
                tempZip.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            try {
                ZipFile(tempZip).use { zip ->
                    val manifestEntry = zip.getEntry("manifest.json")
                    if (manifestEntry == null) {
                        result = context.getString(R.string.error_file_not_found, "manifest.json")
                        return@withContext
                    }

                    val manifestJson = try {
                        JSONObject(
                            zip.getInputStream(manifestEntry).bufferedReader()
                                .use { it.readText() })
                    } catch (_: JSONException) {
                        result = context.getString(R.string.error_file_not_json, "manifest.json")
                        return@withContext
                    }

                    val version = manifestJson.optInt("version", 0)
                    if (version < 1) {
                        result = context.getString(R.string.error_version_not_supported, version)
                        return@withContext
                    }

                    val noteFolders = zip.entries().asSequence()
                        .filter { it.isDirectory && it.name.matches(Regex("^notes/[^/]+/$")) }
                        .map { it.name }.toList()

                    if (noteFolders.isEmpty()) {
                        result = context.getString(R.string.error_folder_notes_empty)
                        return@withContext
                    }

                    val importNotes = mutableListOf<NoteEntity>()

                    for (folder in noteFolders) {
                        val fileName = "${folder}note.json"
                        val noteEntry = zip.getEntry(fileName)
                        if (noteEntry == null) {
                            result = context.getString(R.string.error_file_not_found, fileName)
                            return@withContext
                        }

                        val noteJson = try {
                            JSONObject(
                                zip.getInputStream(noteEntry).bufferedReader()
                                    .use { it.readText() })
                        } catch (_: JSONException) {
                            result = context.getString(R.string.error_file_not_json, fileName)
                            return@withContext
                        }

                        importNotes.add(
                            NoteEntity(
                                title = noteJson.optString("title", "").replace("\n", " "),
                                content = noteJson.optString("content", ""),
                                createdAt = noteJson.optLong(
                                    "created_at",
                                    noteJson.optLong("timestamp", System.currentTimeMillis())
                                ),
                                updatedAt = noteJson.optLong(
                                    "updated_at", noteJson.optLong(
                                        "timestamp", System.currentTimeMillis()
                                    )
                                )
                            )
                        )
                    }

                    importNotes.forEach { note ->
                        val importedNote = NoteEntity(
                            title = note.title,
                            content = note.content,
                            createdAt = note.createdAt,
                            updatedAt = note.updatedAt
                        )

                        noteRepository.insert(importedNote)
                    }
                }
            } catch (_: ZipException) {
                result = context.getString(R.string.error_zip_empty)
            } finally {
                tempZip.delete()
            }
        }

        return result
    }
}