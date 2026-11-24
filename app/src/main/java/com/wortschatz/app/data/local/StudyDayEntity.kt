package com.wortschatz.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One row per day; ISO dates (yyyy-MM-dd) sort correctly as strings. */
@Entity(tableName = "study_days")
data class StudyDayEntity(
    @PrimaryKey val date: String,
    val xp: Int = 0,
    val answers: Int = 0,
    val correctAnswers: Int = 0,
    val masteredWords: Int = 0,
)
