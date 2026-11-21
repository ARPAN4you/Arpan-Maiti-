package com.example.data

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.GlassThemeId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

private val Context.portfolioPrefs by preferencesDataStore(name = "friends_portfolio_prefs")

class LocalPhotoStore(private val context: Context) {
    private val appContext = context.applicationContext
    private val themePrefKey = stringPreferencesKey("selected_theme_id")

    val selectedThemeFlow: Flow<GlassThemeId> = appContext.portfolioPrefs.data.map { prefs ->
        GlassThemeId.fromId(prefs[themePrefKey])
    }

    val photoPathsFlow: Flow<Map<String, String>> = appContext.portfolioPrefs.data.map { prefs ->
        prefs.asMap().entries
            .mapNotNull { (key, value) ->
                val name = key.name
                if (name.startsWith(PHOTO_PREFIX) && value is String && File(value).exists()) {
                    name.removePrefix(PHOTO_PREFIX) to value
                } else {
                    null
                }
            }
            .toMap()
    }

    suspend fun saveLocalTheme(themeId: GlassThemeId) {
        appContext.portfolioPrefs.edit { prefs ->
            prefs[themePrefKey] = themeId.id
        }
    }

    /**
     * Copies a zero-permission Photo Picker URI into app-private storage so avatars remain
     * accessible across restarts without requiring broad external storage permissions.
     */
    suspend fun savePickedPhoto(slotKey: String, sourceUri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val safeKey = slotKey.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                val dir = File(appContext.filesDir, "avatars").apply { mkdirs() }
                val destFile = File(dir, "${safeKey}_${System.currentTimeMillis()}.jpg")

                appContext.contentResolver.openInputStream(sourceUri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: return@withContext Result.failure(IllegalStateException("Unable to read selected image"))

                appContext.portfolioPrefs.edit { prefs ->
                    val prefKey = stringPreferencesKey("$PHOTO_PREFIX$safeKey")
                    val oldPath = prefs[prefKey]
                    if (oldPath != null && oldPath != destFile.absolutePath) {
                        runCatching { File(oldPath).delete() }
                    }
                    prefs[prefKey] = destFile.absolutePath
                }
                Result.success(destFile.absolutePath)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun copySlotPhoto(fromSlotKey: String, toSlotKey: String) = withContext(Dispatchers.IO) {
        val safeFrom = fromSlotKey.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        val safeTo = toSlotKey.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        appContext.portfolioPrefs.edit { prefs ->
            val fromKey = stringPreferencesKey("$PHOTO_PREFIX$safeFrom")
            val existingPath = prefs[fromKey]
            if (existingPath != null && File(existingPath).exists()) {
                val dir = File(appContext.filesDir, "avatars").apply { mkdirs() }
                val destFile = File(dir, "${safeTo}_${System.currentTimeMillis()}.jpg")
                File(existingPath).copyTo(destFile, overwrite = true)
                prefs[stringPreferencesKey("$PHOTO_PREFIX$safeTo")] = destFile.absolutePath
                prefs.remove(fromKey)
                runCatching { File(existingPath).delete() }
            }
        }
    }

    suspend fun clearSlotPhoto(slotKey: String) = withContext(Dispatchers.IO) {
        val safeKey = slotKey.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        appContext.portfolioPrefs.edit { prefs ->
            val prefKey = stringPreferencesKey("$PHOTO_PREFIX$safeKey")
            val oldPath = prefs[prefKey]
            if (oldPath != null) {
                runCatching { File(oldPath).delete() }
            }
            prefs.remove(prefKey)
        }
    }

    companion object {
        private const val PHOTO_PREFIX = "photo_slot_"
        const val FOUNDER_SLOT = "founder_main"
        const val DRAFT_FRIEND_SLOT = "draft_friend"
        fun friendSlot(friendId: String): String = "friend_$friendId"
    }
}
