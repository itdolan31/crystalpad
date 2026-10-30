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

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.git.itdolan31.crystalpad.R
import com.git.itdolan31.crystalpad.core.data.local.room.AppDatabase
import com.git.itdolan31.crystalpad.core.data.local.room.entity.NoteEntity
import com.git.itdolan31.crystalpad.core.data.repository.NoteRepository
import com.git.itdolan31.crystalpad.core.data.repository.SettingsRepository
import com.git.itdolan31.crystalpad.domain.model.DatePatternType
import com.git.itdolan31.crystalpad.domain.model.SettingsConstants
import com.git.itdolan31.crystalpad.domain.model.ThemeType
import com.git.itdolan31.crystalpad.domain.model.TimePatternType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val database: AppDatabase,
    private val noteRepository: NoteRepository,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val tag = javaClass.simpleName

    private val password: StateFlow<String> = settingsRepository.passwordFlow.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = ""
    )

    val theme: StateFlow<ThemeType> = settingsRepository.themeFlow.stateIn(
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

    val lockTimeout: StateFlow<Long> = settingsRepository.timeoutFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_TIMEOUT
    )

    val datePattern: StateFlow<DatePatternType> = settingsRepository.datePatternFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_DATE_PATTERN
    )

    val timePattern: StateFlow<TimePatternType> = settingsRepository.timePatternFlow.stateIn(
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

    val isWordWrapEnabled: StateFlow<Boolean> = settingsRepository.wordWrapFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsConstants.DEFAULT_WORD_WRAP
    )

    val notes: StateFlow<List<NoteEntity>> = noteRepository.getNotes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val trashedNotes: StateFlow<List<NoteEntity>> =
        noteRepository.getNotes(isTrashed = true).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
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

    fun setLockTimeout(seconds: Long) {
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

    fun setTrashRetention(seconds: Long) {
        viewModelScope.launch {
            settingsRepository.saveTrashRetention(seconds)
        }
    }

    fun setWordWrapEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveWordWrap(enabled)
        }
    }

    fun clearTrash() {
        viewModelScope.launch {
            try {
                noteRepository.clearTrash()
            } catch (e: Exception) {
                Log.e(tag, "Failed to clear trash", e)
            }
        }
    }

    suspend fun submitPassword(
        inputPassword: String,
        confirmInputPassword: String
    ): Pair<Boolean, Int> {
        val password = settingsRepository.passwordFlow.first()
        val isPasswordSet = password.isNotEmpty()

        return if (isPasswordSet) {
            if (inputPassword == password) {
                setPassword("")
                true to R.string.password_reset_success
            } else {
                false to R.string.password_incorrect
            }
        } else {
            when {
                inputPassword.length < 4 -> {
                    false to R.string.password_min_length
                }

                inputPassword != confirmInputPassword -> {
                    false to R.string.password_mismatch
                }

                else -> {
                    setPassword(inputPassword)
                    true to R.string.password_set_success
                }
            }
        }
    }

    suspend fun exportNotes(uri: Uri): String = withContext(Dispatchers.IO) {
        val zipFile = File.createTempFile("export-", ".zip", context.cacheDir)

        try {
            ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
                zip.putNextEntry(ZipEntry("manifest.json"))
                zip.write(
                    JSONObject().apply {
                        put("version", 2)
                    }.toString().toByteArray()
                )
                zip.closeEntry()

                zip.putNextEntry(ZipEntry("notes/"))
                zip.closeEntry()

                noteRepository.getNotes().first().forEach { note ->
                    val noteDir = "notes/${note.id}/"

                    zip.putNextEntry(ZipEntry(noteDir))
                    zip.closeEntry()

                    zip.putNextEntry(ZipEntry("${noteDir}note.json"))
                    zip.write(
                        JSONObject().apply {
                            put("title", note.title)
                            put("content", note.content)
                            put("created_at", note.createdAt)
                            put("updated_at", note.updatedAt)
                        }.toString().toByteArray()
                    )
                    zip.closeEntry()

                    File(context.filesDir, "media/${note.id}").listFiles()?.let { mediaFiles ->
                        val mediaEntry = "${noteDir}media/"
                        zip.putNextEntry(ZipEntry(mediaEntry))
                        zip.closeEntry()

                        mediaFiles.filter {
                            MimeTypeMap.getSingleton()
                                .getMimeTypeFromExtension(it.extension.lowercase())
                                ?.startsWith("image/") == true
                        }.forEach { file ->
                            zip.putNextEntry(ZipEntry("$mediaEntry${file.name}"))
                            file.inputStream().use { input ->
                                input.copyTo(zip)
                            }
                            zip.closeEntry()
                        }
                    }
                }
            }

            val outputStream = context.contentResolver.openOutputStream(uri)
                ?: return@withContext context.getString(R.string.error_file_not_found, uri)

            outputStream.use { output ->
                zipFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            }

            context.getString(R.string.export_successful)
        } catch (e: Exception) {
            "$e"
        } finally {
            zipFile.delete()
        }
    }

    suspend fun importNotes(uri: Uri): String = withContext(Dispatchers.IO) {
        val zipFile = File.createTempFile("import-", ".zip", context.cacheDir)
        val tempBase = File(context.cacheDir, "import-media-${System.currentTimeMillis()}")

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext context.getString(R.string.error_file_not_found, uri)

            inputStream.use { input ->
                zipFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            ZipFile(zipFile).use { zip ->
                val manifestEntry =
                    zip.getEntry("manifest.json") ?: return@withContext context.getString(
                        R.string.error_file_not_found, "manifest.json"
                    )

                val manifest = try {
                    JSONObject(
                        zip.getInputStream(manifestEntry).bufferedReader().use { it.readText() })
                } catch (_: JSONException) {
                    return@withContext context.getString(
                        R.string.error_file_not_json, "manifest.json"
                    )
                }

                val version = manifest.optInt("version", 0)
                if (version !in 1..2) {
                    return@withContext context.getString(
                        R.string.error_version_not_supported, version
                    )
                }

                val notesRegex = Regex("^notes/[^/]+/$")
                val noteDirs = zip.entries().asSequence()
                    .filter { it.isDirectory && it.name.matches(notesRegex) }.map { it.name }
                    .toList()

                if (noteDirs.isEmpty()) {
                    return@withContext context.getString(R.string.error_no_notes_found)
                }

                tempBase.mkdirs()
                val prepared = mutableListOf<Pair<NoteEntity, File?>>()

                for (noteDir in noteDirs) {
                    val notePath = "${noteDir}note.json"
                    val noteEntry = zip.getEntry(notePath) ?: return@withContext context.getString(
                        R.string.error_file_not_found, notePath
                    )

                    val noteJson = try {
                        JSONObject(
                            zip.getInputStream(noteEntry).bufferedReader().use { it.readText() })
                    } catch (_: JSONException) {
                        return@withContext context.getString(R.string.error_file_not_json, notePath)
                    }

                    val note = NoteEntity(
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

                    val oldId = noteDir.removePrefix("notes/").removeSuffix("/")
                    val mediaPrefix = "${noteDir}media/"

                    val imageEntries = zip.entries().asSequence()
                        .filter { it.name.startsWith(mediaPrefix) && !it.isDirectory }
                        .filter { entry ->
                            MimeTypeMap.getSingleton()
                                .getMimeTypeFromExtension(
                                    entry.name.substringAfterLast('.').lowercase()
                                )
                                ?.startsWith("image/") == true
                        }
                        .toList()

                    val tempDir: File? = if (imageEntries.isNotEmpty()) {
                        val dir = File(tempBase, oldId)
                        dir.mkdirs()

                        imageEntries.forEach { entry ->
                            val fileName = entry.name.removePrefix(mediaPrefix)
                            if (fileName.isEmpty()) return@forEach

                            val outFile = File(dir, fileName)
                            zip.getInputStream(entry).use { input ->
                                outFile.outputStream().use { output -> input.copyTo(output) }
                            }
                        }
                        dir
                    } else {
                        null
                    }

                    prepared.add(note to tempDir)
                }

                database.withTransaction {
                    prepared.forEach { (note, tempDir) ->
                        val newId = noteRepository.insert(note)

                        if (tempDir != null) {
                            val mediaDir = File(context.filesDir, "media/$newId")
                            mediaDir.parentFile?.mkdirs()

                            if (!tempDir.renameTo(mediaDir)) {
                                throw IOException("renameTo failed: $tempDir → $mediaDir")
                            }
                        }
                    }
                }
            }

            context.getString(R.string.import_successful)
        } catch (_: ZipException) {
            return@withContext context.getString(R.string.error_zip_empty)
        } catch (e: Exception) {
            "$e"
        } finally {
            zipFile.delete()
            tempBase.deleteRecursively()
        }
    }
}