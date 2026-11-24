package com.wortschatz.app.domain.session

import java.text.Normalizer
import java.util.Locale

/** Which side of the vocabulary pair is shown and which side has to be answered. */
enum class LearningDirection {
    FOREIGN_TO_NATIVE,
    NATIVE_TO_FOREIGN,
}

/** How the learner enters the answer. */
enum class AnswerMode {
    CHOICE,
    TYPING,
}

data class LearningMode(
    val direction: LearningDirection = LearningDirection.FOREIGN_TO_NATIVE,
    val answerMode: AnswerMode = AnswerMode.CHOICE,
) {
    companion object {
        val Default = LearningMode()
    }
}

/**
 * Typed answers are forgiving about casing, extra whitespace and diacritics, but still
 * require the actual word. Multiple accepted variants can be separated with |, ; or /.
 */
fun isTypedAnswerCorrect(input: String, correctAnswer: String): Boolean {
    val normalizedInput = normalizeTypedAnswer(input)
    if (normalizedInput.isEmpty()) return false
    return correctAnswer.split('|', ';', '/')
        .map(::normalizeTypedAnswer)
        .filter { it.isNotEmpty() }
        .any { it == normalizedInput }
}

fun normalizeTypedAnswer(value: String): String {
    val withoutDiacritics = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
    return withoutDiacritics
        .lowercase(Locale.ROOT)
        .replace(Regex("\\s+"), " ")
}
