package com.wortschatz.app.data

import android.content.Context
import com.wortschatz.app.domain.settings.AppLanguage
import com.wortschatz.app.domain.settings.MascotType
import com.wortschatz.app.domain.settings.ThemeMode
import com.wortschatz.app.domain.settings.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Non-learning preferences live here. Vocabulary and progress stay in Room.
 * The seeded flag prevents the sample deck from reappearing after the user deletes it.
 */
class SettingsStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("wortschatz_settings", Context.MODE_PRIVATE)

    private val _preferences = MutableStateFlow(readPreferences())
    val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    var isSeeded: Boolean
        get() = prefs.getBoolean(KEY_SEEDED, false)
        set(value) = prefs.edit().putBoolean(KEY_SEEDED, value).apply()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _preferences.value = _preferences.value.copy(themeMode = mode)
    }

    fun setAppLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_APP_LANGUAGE, language.name).apply()
        _preferences.value = _preferences.value.copy(appLanguage = language)
    }

    fun setMascot(mascot: MascotType) {
        prefs.edit().putString(KEY_MASCOT, mascot.name).apply()
        _preferences.value = _preferences.value.copy(mascot = mascot)
    }

    private fun readPreferences(): UserPreferences = UserPreferences(
        themeMode = enumValueOrDefault(prefs.getString(KEY_THEME_MODE, null), ThemeMode.SYSTEM),
        appLanguage = enumValueOrDefault(
            prefs.getString(KEY_APP_LANGUAGE, null),
            AppLanguage.GERMAN,
        ),
        mascot = enumValueOrDefault(prefs.getString(KEY_MASCOT, null), MascotType.FOX),
    )

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == value } ?: fallback

    private companion object {
        const val KEY_SEEDED = "sample_data_seeded"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_APP_LANGUAGE = "app_language"
        const val KEY_MASCOT = "mascot"
    }
}
