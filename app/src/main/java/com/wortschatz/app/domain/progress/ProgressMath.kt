package com.wortschatz.app.domain.progress

import com.wortschatz.app.domain.model.DailyProgress
import com.wortschatz.app.domain.model.ProgressSummary
import com.wortschatz.app.domain.model.WeekDayProgress
import java.time.LocalDate

/**
 * Counts consecutive goal days. If today's goal is not reached yet, the streak is
 * counted through yesterday so it stays visible while the user is still learning.
 */
fun calculateStreak(xpByDate: Map<LocalDate, Int>, today: LocalDate, goal: Int): Int {
    var cursor = if ((xpByDate[today] ?: 0) >= goal) today else today.minusDays(1)
    var streak = 0
    while ((xpByDate[cursor] ?: 0) >= goal) {
        streak++
        cursor = cursor.minusDays(1)
    }
    return streak
}

data class ProgressRecord(
    val date: LocalDate,
    val xp: Int,
    val answers: Int = 0,
    val correctAnswers: Int = 0,
    val masteredWords: Int = 0,
)

fun buildProgressSummary(
    records: List<ProgressRecord>,
    today: LocalDate,
    goal: Int,
): ProgressSummary {
    val xpByDate = records.associate { it.date to it.xp }
    val todayRecord = records.firstOrNull { it.date == today }

    val week = (6L downTo 0L).map { offset ->
        val date = today.minusDays(offset)
        val xp = xpByDate[date] ?: 0
        WeekDayProgress(
            date = date,
            dayOfWeek = date.dayOfWeek,
            xp = xp,
            goalMet = xp >= goal,
            isToday = date == today,
        )
    }

    return ProgressSummary(
        today = DailyProgress(
            xp = todayRecord?.xp ?: 0,
            goal = goal,
            answers = todayRecord?.answers ?: 0,
            correctAnswers = todayRecord?.correctAnswers ?: 0,
            masteredWords = todayRecord?.masteredWords ?: 0,
        ),
        week = week,
        streak = calculateStreak(xpByDate, today, goal),
    )
}
