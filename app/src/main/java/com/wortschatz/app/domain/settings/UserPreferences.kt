package com.wortschatz.app.domain.settings

/** Visual appearance chosen by the user. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    fun isDark(systemInDarkTheme: Boolean): Boolean = when (this) {
        SYSTEM -> systemInDarkTheme
        LIGHT -> false
        DARK -> true
    }
}

/** Language of the app interface, independent from the vocabulary language. */
enum class AppLanguage(val tag: String) {
    GERMAN("de"),
    ENGLISH("en"),
}

/** The mascot that accompanies the learner. Deliberately no owl. */
enum class MascotType {
    FOX,
    CHAMELEON,
    DOG,
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val appLanguage: AppLanguage = AppLanguage.GERMAN,
    val mascot: MascotType = MascotType.FOX,
)
