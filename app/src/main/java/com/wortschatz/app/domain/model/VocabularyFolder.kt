package com.wortschatz.app.domain.model

/** A user-created collection such as "Test 21.11". Words can stay unfiled. */
data class VocabularyFolderSummary(
    val id: Long,
    val name: String,
    val wordCount: Int,
)
