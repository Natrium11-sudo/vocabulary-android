package com.wortschatz.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {
    @Query("SELECT * FROM vocabulary ORDER BY language COLLATE NOCASE, createdAt DESC")
    fun observeAll(): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE language = :language")
    suspend fun getByLanguage(language: String): List<VocabularyEntity>

    @Query("SELECT * FROM vocabulary WHERE folderId = :folderId")
    suspend fun getByFolder(folderId: Long): List<VocabularyEntity>

    @Query("UPDATE vocabulary SET folderId = NULL WHERE folderId = :folderId")
    suspend fun clearFolder(folderId: Long)

    @Query("SELECT * FROM vocabulary ORDER BY language COLLATE NOCASE, createdAt DESC")
    suspend fun getAll(): List<VocabularyEntity>

    @Query("SELECT COUNT(*) FROM vocabulary")
    suspend fun count(): Int

    @Insert
    suspend fun insert(entity: VocabularyEntity): Long

    @Insert
    suspend fun insertAll(entities: List<VocabularyEntity>)

    @Update
    suspend fun update(entity: VocabularyEntity)

    @Delete
    suspend fun delete(entity: VocabularyEntity)
}

data class FolderSummaryRow(
    val id: Long,
    val name: String,
    val wordCount: Int,
)

@Dao
interface FolderDao {
    @Query(
        """
        SELECT folders.id AS id, folders.name AS name, COUNT(vocabulary.id) AS wordCount
        FROM folders
        LEFT JOIN vocabulary ON vocabulary.folderId = folders.id
        GROUP BY folders.id
        ORDER BY folders.createdAt ASC
        """,
    )
    fun observeSummaries(): Flow<List<FolderSummaryRow>>

    @Insert
    suspend fun insert(folder: VocabularyFolderEntity): Long

    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteById(folderId: Long)
}

@Dao
interface StudyDayDao {
    @Query("SELECT * FROM study_days WHERE date = :date LIMIT 1")
    fun observeDay(date: String): Flow<StudyDayEntity?>

    @Query("SELECT * FROM study_days ORDER BY date ASC")
    fun observeAllDays(): Flow<List<StudyDayEntity>>

    @Query("SELECT * FROM study_days WHERE date >= :startDate ORDER BY date ASC")
    fun observeSince(startDate: String): Flow<List<StudyDayEntity>>

    @Query("SELECT * FROM study_days WHERE date = :date LIMIT 1")
    suspend fun getDay(date: String): StudyDayEntity?

    @Insert
    suspend fun insert(day: StudyDayEntity)

    @Update
    suspend fun update(day: StudyDayEntity)
}
