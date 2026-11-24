package com.wortschatz.app.domain.progress

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressMathTest {

    private val today = LocalDate.of(2026, 3, 15)
    private val goal = 50

    @Test
    fun `streak counts consecutive goal days including today`() {
        val xp = mapOf(
            today to 60,
            today.minusDays(1) to 50,
            today.minusDays(2) to 20,
        )
        assertEquals(2, calculateStreak(xp, today, goal))
    }

    @Test
    fun `streak stays visible until todays goal is missed at midnight`() {
        val xp = mapOf(
            today.minusDays(1) to 50,
            today.minusDays(2) to 80,
        )
        assertEquals(2, calculateStreak(xp, today, goal))
    }

    @Test
    fun `a gap breaks the streak`() {
        val xp = mapOf(
            today to 50,
            today.minusDays(1) to 10,
            today.minusDays(2) to 50,
        )
        assertEquals(1, calculateStreak(xp, today, goal))
    }

    @Test
    fun `progress summary exposes today and a seven day week`() {
        val records = listOf(
            ProgressRecord(today, xp = 75, answers = 20, correctAnswers = 18, masteredWords = 3),
            ProgressRecord(today.minusDays(1), xp = 50),
            ProgressRecord(today.minusDays(2), xp = 50),
        )
        val summary = buildProgressSummary(records, today, goal)

        assertEquals(75, summary.today.xp)
        assertEquals(0, summary.today.remainingXp)
        assertTrue(summary.today.isGoalReached)
        assertEquals(3, summary.today.masteredWords)
        assertEquals(3, summary.streak)
        assertEquals(7, summary.week.size)
        assertTrue(summary.week.last().isToday)
        assertTrue(summary.week.last().goalMet)
        assertTrue(summary.week[4].goalMet)
        assertFalse(summary.week[0].goalMet)
    }

    @Test
    fun `progress is capped at one`() {
        val summary = buildProgressSummary(
            listOf(ProgressRecord(today, xp = 200)),
            today,
            goal,
        )
        assertEquals(1f, summary.today.progress)
    }
}
