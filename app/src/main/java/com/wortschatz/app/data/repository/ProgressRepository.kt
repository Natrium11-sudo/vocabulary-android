package com.wortschatz.app.data.repository

import androidx.room.withTransaction
import com.wortschatz.app.data.local.AppDatabase
import com.wortschatz.app.data.local.StudyDayDao
import com.wortschatz.app.data.local.StudyDayEntity
import com.wortschatz.app.domain.model.DAILY_GOAL_XP
import com.wortschatz.app.domain.model.PracticeDay
import com.wortschatz.app.domain.model.ProgressSummary
import com.wortschatz.app.domain.model.XP_PER_CORRECT_ANSWER
import com.wortschatz.app.domain.model.XP_PERFECT_SESSION_BONUS
import com.wortschatz.app.domain.progress.ProgressRecord
import com.wortschatz.app.domain.progress.buildProgressSummary
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProgressRepository(
    private val database: AppDatabase,
    private val dao: StudyDayDao,
) {
    fun observeSummary(): Flow<ProgressSummary> {
        val today = LocalDate.now()
        return dao.observeSince(today.minusDays(365).toString()).map { days ->
            buildProgressSummary(
                records = days.map { entity ->
                    ProgressRecord(
                        date = LocalDate.parse(entity.date),
                        xp = entity.xp,
                        answers = entity.answers,
                        correctAnswers = entity.correctAnswers,
                        masteredWords = entity.masteredWords,
                    )
                },
                today = LocalDate.now(),
                goal = DAILY_GOAL_XP,
            )
        }
    }

    /** All days with at least one answer, not limited to the streak window. */
    fun observePracticeDays(): Flow<List<PracticeDay>> = dao.observeAllDays().map { days ->
        days
            .filter { it.answers > 0 || it.xp > 0 }
            .map { entity ->
                PracticeDay(
                    date = LocalDate.parse(entity.date),
                    xp = entity.xp,
                    masteredWords = entity.masteredWords,
                    answers = entity.answers,
                )
            }
    }

    suspend fun recordAnswer(isCorrect: Boolean, masteredNow: Boolean) {
        increment(
            xp = if (isCorrect) XP_PER_CORRECT_ANSWER else 0,
            answers = 1,
            correctAnswers = if (isCorrect) 1 else 0,
            masteredWords = if (masteredNow) 1 else 0,
        )
    }

    suspend fun awardPerfectSessionBonus() {
        increment(xp = XP_PERFECT_SESSION_BONUS)
    }

    private suspend fun increment(
        xp: Int = 0,
        answers: Int = 0,
        correctAnswers: Int = 0,
        masteredWords: Int = 0,
    ) {
        val date = LocalDate.now().toString()
        database.withTransaction {
            val existing = dao.getDay(date)
            if (existing == null) {
                dao.insert(
                    StudyDayEntity(
                        date = date,
                        xp = xp,
                        answers = answers,
                        correctAnswers = correctAnswers,
                        masteredWords = masteredWords,
                    ),
                )
            } else {
                dao.update(
                    existing.copy(
                        xp = existing.xp + xp,
                        answers = existing.answers + answers,
                        correctAnswers = existing.correctAnswers + correctAnswers,
                        masteredWords = existing.masteredWords + masteredWords,
                    ),
                )
            }
        }
    }
}
