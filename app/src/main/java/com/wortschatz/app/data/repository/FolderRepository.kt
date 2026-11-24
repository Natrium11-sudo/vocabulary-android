package com.wortschatz.app.data.repository

import androidx.room.withTransaction
import com.wortschatz.app.data.local.AppDatabase
import com.wortschatz.app.data.local.FolderDao
import com.wortschatz.app.data.local.VocabularyDao
import com.wortschatz.app.data.local.VocabularyFolderEntity
import com.wortschatz.app.domain.model.VocabularyFolderSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FolderRepository(
    private val database: AppDatabase,
    private val folderDao: FolderDao,
    private val vocabularyDao: VocabularyDao,
) {
    fun observeFolders(): Flow<List<VocabularyFolderSummary>> =
        folderDao.observeSummaries().map { rows ->
            rows.map { row ->
                VocabularyFolderSummary(
                    id = row.id,
                    name = row.name,
                    wordCount = row.wordCount,
                )
            }
        }

    suspend fun create(name: String): Long {
        val now = System.currentTimeMillis()
        return folderDao.insert(
            VocabularyFolderEntity(
                name = name.trim(),
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    /**
     * Deleting a folder only removes the grouping. Its vocabulary stays in the library
     * and becomes unfiled, so no learning content is lost by accident.
     */
    suspend fun delete(folderId: Long) {
        database.withTransaction {
            vocabularyDao.clearFolder(folderId)
            folderDao.deleteById(folderId)
        }
    }
}
