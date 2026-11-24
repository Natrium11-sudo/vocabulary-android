package com.wortschatz.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wortschatz.app.domain.model.Vocabulary

@Entity(
    tableName = "vocabulary",
    indices = [Index("language"), Index("folderId")],
)
data class VocabularyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val nativeText: String,
    val foreignText: String,
    val language: String,
    val category: String,
    val folderId: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

fun VocabularyEntity.toDomain(): Vocabulary = Vocabulary(
    id = id,
    nativeText = nativeText,
    foreignText = foreignText,
    language = language,
    category = category,
    folderId = folderId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Vocabulary.toEntity(): VocabularyEntity = VocabularyEntity(
    id = id,
    nativeText = nativeText,
    foreignText = foreignText,
    language = language,
    category = category,
    folderId = folderId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
