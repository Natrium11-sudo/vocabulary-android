package com.wortschatz.app.domain.model

import com.wortschatz.app.domain.settings.AppLanguage

/**
 * Known language names are stored with their German canonical name, matching the
 * original data. The UI can display them in the selected app language without creating
 * duplicate language groups.
 */
private val knownLanguages = listOf(
    "Deutsch" to "German",
    "Englisch" to "English",
    "Französisch" to "French",
    "Italienisch" to "Italian",
    "Latein" to "Latin",
    "Portugiesisch" to "Portuguese",
    "Spanisch" to "Spanish",
)

private val knownCategories = listOf(
    "Adjektive" to "Adjectives",
    "Alltag" to "Everyday",
    "Essen" to "Food",
    "Menschen" to "People",
    "Tiere" to "Animals",
    "Verben" to "Verbs",
)

fun canonicalLanguageName(value: String): String {
    val trimmed = value.trim()
    if (trimmed.isEmpty()) return ""
    val match = knownLanguages.firstOrNull { (german, english) ->
        german.equals(trimmed, ignoreCase = true) || english.equals(trimmed, ignoreCase = true)
    }
    return match?.first ?: trimmed
}

fun languageDisplayName(value: String, appLanguage: AppLanguage): String {
    val canonical = canonicalLanguageName(value)
    if (canonical.isEmpty() || appLanguage == AppLanguage.GERMAN) return canonical
    return knownLanguages.firstOrNull { it.first == canonical }?.second ?: canonical
}

/** Terms used by library search so "French" also finds words stored as "Französisch". */
fun languageSearchTerms(value: String): List<String> {
    val canonical = canonicalLanguageName(value)
    if (canonical.isEmpty()) return emptyList()
    val english = knownLanguages.firstOrNull { it.first == canonical }?.second
    return listOfNotNull(value, canonical, english).distinct()
}

fun categoryDisplayName(value: String, appLanguage: AppLanguage): String {
    if (value.isBlank()) return ""
    if (appLanguage == AppLanguage.GERMAN) return value
    return knownCategories.firstOrNull { (german, english) ->
        german.equals(value, ignoreCase = true) || english.equals(value, ignoreCase = true)
    }?.second ?: value
}

fun categorySearchTerms(value: String): List<String> {
    if (value.isBlank()) return emptyList()
    val english = knownCategories.firstOrNull { (german, english) ->
        german.equals(value, ignoreCase = true) || english.equals(value, ignoreCase = true)
    }?.second
    return listOfNotNull(value, english).distinct()
}
