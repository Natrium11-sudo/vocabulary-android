package com.wortschatz.app.domain.model

/** A single vocabulary pair, independent of persistence details. */
data class Vocabulary(
    val id: Long = 0L,
    val nativeText: String,
    val foreignText: String,
    val language: String,
    val category: String = "",
    val folderId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    val displayCategory: String
        get() = category.ifBlank { "" }
}

data class VocabularyDraft(
    val nativeText: String = "",
    val foreignText: String = "",
    val language: String = "",
    val category: String = "",
    val folderId: Long? = null,
)

data class LanguageSummary(
    val language: String,
    val wordCount: Int,
)
