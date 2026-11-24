package com.wortschatz.app.domain.session

import com.wortschatz.app.domain.model.Vocabulary
import kotlin.random.Random

/**
 * A vocabulary only counts as learned after TWO correct answers in a row.
 * One mistake immediately resets its streak to zero.
 */
const val REQUIRED_CORRECT_STREAK = 2

data class StudyWord(
    val vocabulary: Vocabulary,
    val streak: Int = 0,
) {
    val mastered: Boolean get() = streak >= REQUIRED_CORRECT_STREAK
}

/**
 * Immutable state of one learning round.
 *
 * `queue` holds vocabulary ids in the order they will be asked. Mastered words leave
 * the queue; failed or half-learned words are appended again, which spaces repetitions.
 */
data class SessionState(
    val words: List<StudyWord>,
    val queue: List<Long>,
    val answers: Int = 0,
    val correctAnswers: Int = 0,
    val mistakes: Int = 0,
) {
    val totalWords: Int get() = words.size
    val masteredCount: Int get() = words.count { it.mastered }
    val remainingCount: Int get() = words.count { !it.mastered }
    val isComplete: Boolean get() = words.isNotEmpty() && queue.isEmpty()
    val isPerfect: Boolean get() = mistakes == 0

    /** Fraction of all required repetitions completed (0..1). */
    val progress: Float
        get() = if (words.isEmpty()) {
            0f
        } else {
            words.sumOf { it.streak.coerceAtMost(REQUIRED_CORRECT_STREAK) }
                .toFloat() / (words.size * REQUIRED_CORRECT_STREAK)
        }

    val currentWord: StudyWord?
        get() = queue.firstOrNull()?.let { id -> words.firstOrNull { it.vocabulary.id == id } }
}

data class AnswerOutcome(
    val state: SessionState,
    val wasCorrect: Boolean,
    val newStreak: Int,
    /** True only on the answer that takes a word from 0/1 to two correct answers. */
    val masteredNow: Boolean,
)

fun startSession(vocabularies: List<Vocabulary>): SessionState {
    val unique = vocabularies.distinctBy { it.id }
    require(unique.isNotEmpty()) { "A session needs at least one vocabulary." }
    return SessionState(
        words = unique.map { StudyWord(it) },
        queue = unique.map { it.id },
    )
}

/**
 * Applies one answer:
 *  - correct   -> streak + 1
 *  - incorrect -> streak 0
 * The answered word is removed from the front; unless it is mastered it is queued again
 * at the end. The round only completes when every word reached streak >= 2.
 */
fun SessionState.submitAnswer(isCorrect: Boolean): AnswerOutcome {
    val current = currentWord ?: return AnswerOutcome(this, isCorrect, 0, false)

    val newStreak = if (isCorrect) current.streak + 1 else 0
    val updatedWord = current.copy(streak = newStreak)
    val updatedWords = words.map {
        if (it.vocabulary.id == current.vocabulary.id) updatedWord else it
    }

    val rest = queue.drop(1).toMutableList()
    if (newStreak < REQUIRED_CORRECT_STREAK) {
        rest += current.vocabulary.id
    }

    val next = copy(
        words = updatedWords,
        queue = rest,
        answers = answers + 1,
        correctAnswers = correctAnswers + if (isCorrect) 1 else 0,
        mistakes = mistakes + if (isCorrect) 0 else 1,
    )

    return AnswerOutcome(
        state = next,
        wasCorrect = isCorrect,
        newStreak = newStreak,
        masteredNow = newStreak == REQUIRED_CORRECT_STREAK,
    )
}

data class QuizQuestion(
    val vocabularyId: Long,
    val prompt: String,
    val language: String,
    val category: String,
    val correctAnswer: String,
    val options: List<String>,
    val direction: LearningDirection,
    val answerMode: AnswerMode,
)

/**
 * Builds a question for either direction. Multiple-choice distractors are real
 * translations, preferring the same language. If a folder only contains one word per
 * language, the fallback fills the remaining options from the whole deck so the question
 * is still answerable.
 */
fun buildQuestion(
    word: StudyWord,
    pool: List<Vocabulary>,
    random: Random = Random.Default,
    direction: LearningDirection = LearningDirection.FOREIGN_TO_NATIVE,
    answerMode: AnswerMode = AnswerMode.CHOICE,
): QuizQuestion {
    val vocabulary = word.vocabulary
    val prompt = if (direction == LearningDirection.FOREIGN_TO_NATIVE) {
        vocabulary.foreignText
    } else {
        vocabulary.nativeText
    }
    val correct = if (direction == LearningDirection.FOREIGN_TO_NATIVE) {
        vocabulary.nativeText
    } else {
        vocabulary.foreignText
    }

    fun answerOf(candidate: Vocabulary): String =
        if (direction == LearningDirection.FOREIGN_TO_NATIVE) {
            candidate.nativeText.trim()
        } else {
            candidate.foreignText.trim()
        }

    val candidates = pool.filter { it.id != vocabulary.id }
    fun usableDistractors(values: List<String>): List<String> = values
        .filter { it.isNotEmpty() && !it.equals(correct, ignoreCase = true) }
        .distinctBy { it.lowercase() }

    val sameLanguage = usableDistractors(
        candidates.filter { it.language == vocabulary.language }.map(::answerOf),
    )
    val otherLanguages = usableDistractors(
        candidates.filter { it.language != vocabulary.language }.map(::answerOf),
    ).filter { candidate ->
        sameLanguage.none { it.equals(candidate, ignoreCase = true) }
    }
    // Prefer same-language distractors; only fill the gap for small folder pools.
    val distractors = (
        sameLanguage.shuffled(random).take(3) +
            otherLanguages.shuffled(random)
        )
        .distinctBy { it.lowercase() }
        .take(3)

    return QuizQuestion(
        vocabularyId = vocabulary.id,
        prompt = prompt,
        language = vocabulary.language,
        category = vocabulary.category,
        correctAnswer = correct,
        options = if (answerMode == AnswerMode.CHOICE) {
            (distractors + correct).shuffled(random)
        } else {
            emptyList()
        },
        direction = direction,
        answerMode = answerMode,
    )
}
