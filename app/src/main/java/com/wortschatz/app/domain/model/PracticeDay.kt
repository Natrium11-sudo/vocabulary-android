package com.wortschatz.app.domain.model

import java.time.LocalDate

/** One calendar entry for a day on which at least one answer was given. */
data class PracticeDay(
    val date: LocalDate,
    val xp: Int,
    val masteredWords: Int,
    val answers: Int,
)
