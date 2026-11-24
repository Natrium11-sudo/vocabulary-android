package com.wortschatz.app.domain.session

import com.wortschatz.app.domain.model.XP_PERFECT_SESSION_BONUS

data class SessionSummary(
    val language: String,
    val totalWords: Int,
    val correctAnswers: Int,
    val answers: Int,
    val durationMillis: Long,
    val xpEarned: Int,
    val perfect: Boolean,
) {
    val accuracyPercent: Int
        get() = if (answers == 0) 0 else ((correctAnswers.toFloat() / answers) * 100).toInt()

    val perfectBonus: Int
        get() = if (perfect) XP_PERFECT_SESSION_BONUS else 0
}
