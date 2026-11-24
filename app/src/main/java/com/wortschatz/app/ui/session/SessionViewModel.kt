package com.wortschatz.app.ui.session

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wortschatz.app.data.repository.ProgressRepository
import com.wortschatz.app.data.repository.VocabularyRepository
import com.wortschatz.app.domain.model.Vocabulary
import com.wortschatz.app.domain.model.XP_PERFECT_SESSION_BONUS
import com.wortschatz.app.domain.model.XP_PER_CORRECT_ANSWER
import com.wortschatz.app.domain.session.AnswerMode
import com.wortschatz.app.domain.session.LearningDirection
import com.wortschatz.app.domain.session.LearningMode
import com.wortschatz.app.domain.session.QuizQuestion
import com.wortschatz.app.domain.session.SessionState
import com.wortschatz.app.domain.session.SessionSummary
import com.wortschatz.app.domain.session.buildQuestion
import com.wortschatz.app.domain.session.isTypedAnswerCorrect
import com.wortschatz.app.domain.session.startSession
import com.wortschatz.app.domain.session.submitAnswer
import com.wortschatz.app.ui.appViewModelFactory
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnswerFeedback(
    val isCorrect: Boolean,
    val correctAnswer: String,
    val newStreak: Int,
    val masteredNow: Boolean,
)

data class SessionUiState(
    val isLoading: Boolean = true,
    val language: String = "",
    val goal: Int = 0,
    val focusMode: Boolean = false,
    val folderId: Long? = null,
    val mode: LearningMode = LearningMode.Default,
    val engine: SessionState? = null,
    val question: QuizQuestion? = null,
    val selectedAnswer: String? = null,
    val typedAnswer: String = "",
    val feedback: AnswerFeedback? = null,
    val error: Boolean = false,
    val summary: SessionSummary? = null,
)

class SessionViewModel(
    private val language: String,
    private val goal: Int,
    private val focusMode: Boolean,
    private val folderId: Long?,
    private val mode: LearningMode,
    private val vocabularyRepository: VocabularyRepository,
    private val progressRepository: ProgressRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SessionUiState(
            language = language,
            goal = goal,
            focusMode = focusMode,
            folderId = folderId,
            mode = mode,
        ),
    )
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    private var pool: List<Vocabulary> = emptyList()
    private val random = Random.Default
    private val startedAt = SystemClock.elapsedRealtime()
    private var bonusAwarded = false

    init {
        loadRound()
    }

    private fun loadRound() {
        viewModelScope.launch {
            val allWords = vocabularyRepository.getAll()
            val selectedWords = if (folderId != null) {
                allWords.filter { it.folderId == folderId }
            } else {
                allWords.filter { it.language == language }
            }
            if (selectedWords.size < 2) {
                _uiState.update { it.copy(isLoading = false, error = true) }
                return@launch
            }

            // A folder can mix languages, so distractors may come from the full deck.
            pool = allWords
            val sessionWords = selectedWords.shuffled(random).take(goal.coerceAtLeast(1))
            if (sessionWords.size < 2) {
                _uiState.update { it.copy(isLoading = false, error = true) }
                return@launch
            }

            val engine = startSession(sessionWords)
            val firstQuestion = engine.currentWord?.let { word ->
                buildQuestion(
                    word = word,
                    pool = pool,
                    random = random,
                    direction = mode.direction,
                    answerMode = mode.answerMode,
                )
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    engine = engine,
                    question = firstQuestion,
                )
            }
        }
    }

    fun onTypedAnswerChanged(value: String) {
        if (_uiState.value.feedback != null || _uiState.value.summary != null) return
        _uiState.update { it.copy(typedAnswer = value) }
    }

    /** Multiple-choice answers arrive as the selected option. */
    fun submitAnswer(answer: String) {
        val question = _uiState.value.question ?: return
        if (question.answerMode == AnswerMode.TYPING) return
        evaluateAnswer(answer, isCorrect = answer == question.correctAnswer)
    }

    /** Typed answers are compared with forgiving casing/whitespace/diacritics. */
    fun submitTypedAnswer() {
        val current = _uiState.value
        val question = current.question ?: return
        if (question.answerMode != AnswerMode.TYPING) return
        val input = current.typedAnswer
        if (input.isBlank()) return
        evaluateAnswer(
            answer = input,
            isCorrect = isTypedAnswerCorrect(input, question.correctAnswer),
        )
    }

    private fun evaluateAnswer(answer: String, isCorrect: Boolean) {
        val current = _uiState.value
        val engine = current.engine ?: return
        val question = current.question ?: return
        if (current.feedback != null || current.summary != null) return

        val outcome = engine.submitAnswer(isCorrect)
        val completed = outcome.state.isComplete
        val durationMillis = SystemClock.elapsedRealtime() - startedAt

        _uiState.update {
            it.copy(
                engine = outcome.state,
                selectedAnswer = answer,
                feedback = AnswerFeedback(
                    isCorrect = outcome.wasCorrect,
                    correctAnswer = question.correctAnswer,
                    newStreak = outcome.newStreak,
                    masteredNow = outcome.masteredNow,
                ),
            )
        }

        viewModelScope.launch {
            // Persist before publishing the summary: the summary triggers navigation and
            // releasing screen pinning, which clears this ViewModel. Awaiting the writes
            // here guarantees the last answer and the perfect bonus reach Room.
            runCatching {
                progressRepository.recordAnswer(
                    isCorrect = outcome.wasCorrect,
                    masteredNow = outcome.masteredNow,
                )
                if (completed && outcome.state.isPerfect && !bonusAwarded) {
                    bonusAwarded = true
                    progressRepository.awardPerfectSessionBonus()
                }
            }

            if (completed) {
                val bonus = if (outcome.state.isPerfect) XP_PERFECT_SESSION_BONUS else 0
                _uiState.update {
                    it.copy(
                        summary = SessionSummary(
                            language = language,
                            totalWords = outcome.state.totalWords,
                            correctAnswers = outcome.state.correctAnswers,
                            answers = outcome.state.answers,
                            durationMillis = durationMillis,
                            xpEarned = outcome.state.correctAnswers * XP_PER_CORRECT_ANSWER + bonus,
                            perfect = outcome.state.isPerfect,
                        ),
                    )
                }
            }
        }
    }

    fun continueSession() {
        val current = _uiState.value
        if (current.summary != null) return
        val nextWord = current.engine?.currentWord ?: return
        val nextQuestion = buildQuestion(
            word = nextWord,
            pool = pool,
            random = random,
            direction = mode.direction,
            answerMode = mode.answerMode,
        )
        _uiState.update {
            it.copy(
                question = nextQuestion,
                selectedAnswer = null,
                typedAnswer = "",
                feedback = null,
            )
        }
    }

    companion object {
        fun factory(
            language: String,
            goal: Int,
            focusMode: Boolean,
            folderId: Long?,
            direction: LearningDirection,
            answerMode: AnswerMode,
        ) = appViewModelFactory { container ->
            SessionViewModel(
                language = language,
                goal = goal,
                focusMode = focusMode,
                folderId = folderId,
                mode = LearningMode(direction = direction, answerMode = answerMode),
                vocabularyRepository = container.vocabularyRepository,
                progressRepository = container.progressRepository,
            )
        }
    }
}
