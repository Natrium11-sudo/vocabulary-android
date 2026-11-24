package com.wortschatz.app.domain.session

import com.wortschatz.app.domain.model.XP_PERFECT_SESSION_BONUS
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionSummaryTest {

    @Test
    fun `accuracy is rounded down from answered questions`() {
        val summary = SessionSummary(
            language = "Spanisch",
            totalWords = 10,
            correctAnswers = 15,
            answers = 20,
            durationMillis = 60_000,
            xpEarned = 85,
            perfect = false,
        )
        assertEquals(75, summary.accuracyPercent)
        assertEquals(0, summary.perfectBonus)
    }

    @Test
    fun `perfect sessions expose the bonus`() {
        val summary = SessionSummary(
            language = "Französisch",
            totalWords = 10,
            correctAnswers = 20,
            answers = 20,
            durationMillis = 30_000,
            xpEarned = 110,
            perfect = true,
        )
        assertEquals(100, summary.accuracyPercent)
        assertEquals(XP_PERFECT_SESSION_BONUS, summary.perfectBonus)
    }
}
