package com.wortschatz.app.domain.session

import com.wortschatz.app.domain.model.Vocabulary
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionEngineTest {

    private fun vocab(id: Long, native: String, language: String = "Französisch"): Vocabulary =
        Vocabulary(
            id = id,
            nativeText = native,
            foreignText = "foreign-$id",
            language = language,
            category = "Test",
        )

    @Test
    fun `two correct answers in a row master a word`() {
        val a = vocab(1, "Apfel")
        val b = vocab(2, "Brot")
        val start = startSession(listOf(a, b))

        val first = start.submitAnswer(true)
        assertEquals(1, first.newStreak)
        assertFalse(first.masteredNow)
        assertEquals(listOf(2L, 1L), first.state.queue)
        assertEquals(0, first.state.masteredCount)

        val second = first.state.submitAnswer(true)
        assertEquals(0, second.state.masteredCount)

        val third = second.state.submitAnswer(true) // back to word A
        assertTrue(third.masteredNow)
        assertEquals(2, third.newStreak)
        assertEquals(1, third.state.masteredCount)
        assertFalse(third.state.isComplete)

        val fourth = third.state.submitAnswer(true) // word B
        assertTrue(fourth.state.isComplete)
        assertEquals(2, fourth.state.masteredCount)
        assertTrue(fourth.state.isPerfect)
        assertEquals(1f, fourth.state.progress)
    }

    @Test
    fun `a wrong answer resets the streak immediately`() {
        val word = vocab(1, "Apfel")
        val start = startSession(listOf(word))

        val correctOnce = start.submitAnswer(true)
        assertEquals(1, correctOnce.newStreak)

        val wrong = correctOnce.state.submitAnswer(false)
        assertEquals(0, wrong.newStreak)
        assertFalse(wrong.masteredNow)
        assertEquals(1, wrong.state.mistakes)
        assertFalse(wrong.state.isComplete)
        assertEquals(listOf(1L), wrong.state.queue)

        val correctAgain = wrong.state.submitAnswer(true)
        assertEquals(1, correctAgain.newStreak)
        assertFalse(correctAgain.state.isComplete)

        val mastered = correctAgain.state.submitAnswer(true)
        assertTrue(mastered.state.isComplete)
        assertFalse(mastered.state.isPerfect)
    }

    @Test
    fun `the round never completes while one word has less than two correct answers`() {
        val start = startSession(listOf(vocab(1, "Apfel"), vocab(2, "Brot"), vocab(3, "Haus")))
        // Master the first two words, leave the third at streak 1.
        var state = start
        var current: String
        repeat(5) {
            current = state.currentWord!!.vocabulary.nativeText
            val isCorrect = current != "Haus" || state.words.first { it.vocabulary.nativeText == "Haus" }.streak >= 1
            state = state.submitAnswer(isCorrect).state
        }

        assertFalse(state.isComplete)
        assertTrue(state.remainingCount >= 1)
    }

    @Test
    fun `question offers four distinct translations including the correct one`() {
        val words = (1L..5L).map { vocab(it, "native-$it") }
        val otherLanguage = vocab(99, "nativo", language = "Spanisch")
        val question = buildQuestion(
            word = StudyWord(words.first()),
            pool = words + otherLanguage,
            random = Random(7),
        )

        assertEquals("foreign-1", question.prompt)
        assertEquals("native-1", question.correctAnswer)
        assertEquals(4, question.options.size)
        assertEquals(4, question.options.distinct().size)
        assertTrue(question.options.contains("native-1"))
        assertFalse(question.options.contains("nativo"))
    }

    @Test
    fun `native to foreign asks for the foreign translation`() {
        val words = (1L..5L).map { vocab(it, "native-$it") }
        val question = buildQuestion(
            word = StudyWord(words.first()),
            pool = words,
            random = Random(7),
            direction = LearningDirection.NATIVE_TO_FOREIGN,
            answerMode = AnswerMode.CHOICE,
        )

        assertEquals("native-1", question.prompt)
        assertEquals("foreign-1", question.correctAnswer)
        assertTrue(question.options.contains("foreign-1"))
        assertFalse(question.options.contains("native-1"))
        assertEquals(4, question.options.size)
    }

    @Test
    fun `typing mode returns no options and accepts forgiving input`() {
        val words = (1L..5L).map { vocab(it, "native-$it") }
        val question = buildQuestion(
            word = StudyWord(words.first()),
            pool = words,
            random = Random(7),
            answerMode = AnswerMode.TYPING,
        )

        assertTrue(question.options.isEmpty())
        assertTrue(isTypedAnswerCorrect("  LE PAIN ", "le pain"))
        assertTrue(isTypedAnswerCorrect("ecole", "école"))
        assertFalse(isTypedAnswerCorrect("le pained", "le pain"))
        assertFalse(isTypedAnswerCorrect("   ", "le pain"))
    }

    @Test
    fun `folder questions fall back to other languages for distractors`() {
        val words = listOf(
            vocab(1, "der Apfel", "Französisch"),
            vocab(2, "das Haus", "Spanisch"),
            vocab(3, "das Buch", "Englisch"),
        )
        val question = buildQuestion(
            word = StudyWord(words.first()),
            pool = words,
            random = Random(7),
        )

        assertTrue(question.options.size >= 2)
        assertTrue(question.options.contains("der Apfel"))
    }

    @Test
    fun `an empty session is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            startSession(emptyList())
        }
    }
}
