package com.wortschatz.app.data.repository

import com.wortschatz.app.data.local.VocabularyDao
import com.wortschatz.app.data.local.VocabularyEntity
import com.wortschatz.app.data.local.toDomain
import com.wortschatz.app.data.local.toEntity
import com.wortschatz.app.domain.model.LanguageSummary
import com.wortschatz.app.domain.model.Vocabulary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VocabularyRepository(
    private val dao: VocabularyDao,
) {
    fun observeAll(): Flow<List<Vocabulary>> =
        dao.observeAll().map { entities -> entities.map(VocabularyEntity::toDomain) }

    fun observeLanguageSummaries(): Flow<List<LanguageSummary>> =
        observeAll().map { words ->
            words.groupingBy { it.language }
                .eachCount()
                .map { (language, count) -> LanguageSummary(language, count) }
                .sortedWith(compareByDescending<LanguageSummary> { it.wordCount }.thenBy { it.language })
        }

    suspend fun getByLanguage(language: String): List<Vocabulary> =
        dao.getByLanguage(language).map(VocabularyEntity::toDomain)

    suspend fun getByFolder(folderId: Long): List<Vocabulary> =
        dao.getByFolder(folderId).map(VocabularyEntity::toDomain)

    suspend fun getAll(): List<Vocabulary> =
        dao.getAll().map(VocabularyEntity::toDomain)

    suspend fun upsert(vocabulary: Vocabulary): Long {
        val now = System.currentTimeMillis()
        return if (vocabulary.id == 0L) {
            dao.insert(vocabulary.copy(createdAt = now, updatedAt = now).toEntity())
        } else {
            dao.update(vocabulary.copy(updatedAt = now).toEntity())
            vocabulary.id
        }
    }

    suspend fun delete(vocabulary: Vocabulary) = dao.delete(vocabulary.toEntity())

    suspend fun seedIfEmpty(words: List<Vocabulary>) {
        if (dao.count() > 0) return
        dao.insertAll(words.map { it.toEntity() })
    }
}
