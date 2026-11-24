package com.wortschatz.app.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

/** XP earned for one correct answer and for a flawless session. */
const val XP_PER_CORRECT_ANSWER = 5
const val XP_PERFECT_SESSION_BONUS = 10

/** Daily target in XP. Reaching it keeps the streak alive. */
const val DAILY_GOAL_XP = 50

data class DailyProgress(
    val xp: Int = 0,
    val goal: Int = DAILY_GOAL_XP,
    val answers: Int = 0,
    val correctAnswers: Int = 0,
    val masteredWords: Int = 0,
) {
    val progress: Float
        get() = if (goal <= 0) 0f else (xp.toFloat() / goal).coerceIn(0f, 1f)

    val isGoalReached: Boolean
        get() = xp >= goal

    val remainingXp: Int
        get() = (goal - xp).coerceAtLeast(0)
}

data class WeekDayProgress(
    val date: LocalDate,
    val dayOfWeek: DayOfWeek,
    val xp: Int,
    val goalMet: Boolean,
    val isToday: Boolean,
)

data class ProgressSummary(
    val today: DailyProgress = DailyProgress(),
    val week: List<WeekDayProgress> = emptyList(),
    val streak: Int = 0,
)
