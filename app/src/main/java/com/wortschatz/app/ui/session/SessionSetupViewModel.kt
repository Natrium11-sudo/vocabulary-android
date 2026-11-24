package com.wortschatz.app.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wortschatz.app.data.repository.FolderRepository
import com.wortschatz.app.data.repository.VocabularyRepository
import com.wortschatz.app.domain.model.LanguageSummary
import com.wortschatz.app.domain.model.VocabularyFolderSummary
import com.wortschatz.app.domain.session.AnswerMode
import com.wortschatz.app.domain.session.LearningDirection
import com.wortschatz.app.domain.session.LearningMode
import com.wortschatz.app.ui.appViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class SetupSource {
    LANGUAGE,
    FOLDER,
}

/** How many words the next round should contain. */
sealed interface SessionGoal {
    /** A fixed number, either from a preset or typed in by the user. */
    data class Words(val count: Int, val custom: Boolean = false) : SessionGoal

    /** Every word of the selected collection, e.g. a whole folder. */
    data object All : SessionGoal
}

data class SessionSetupUiState(
    val isLoading: Boolean = true,
    val languages: List<LanguageSummary> = emptyList(),
    val folders: List<VocabularyFolderSummary> = emptyList(),
    val source: SetupSource = SetupSource.LANGUAGE,
    val selectedLanguage: String? = null,
    val selectedFolder: VocabularyFolderSummary? = null,
    val goal: SessionGoal = SessionGoal.Words(10),
    val customGoalInput: String = "",
    val focusMode: Boolean = false,
    val mode: LearningMode = LearningMode.Default,
) {
    val availableWords: Int
        get() = when (source) {
            SetupSource.LANGUAGE ->
                languages.firstOrNull { it.language == selectedLanguage }?.wordCount ?: 0
            SetupSource.FOLDER -> selectedFolder?.wordCount ?: 0
        }

    val isCustomGoal: Boolean
        get() = (goal as? SessionGoal.Words)?.custom == true

    /** Never ask for more words than the selected collection actually contains. */
    val effectiveGoal: Int
        get() = when (val selected = goal) {
            SessionGoal.All -> availableWords
            is SessionGoal.Words -> selected.count.coerceIn(0, availableWords)
        }

    val canStart: Boolean
        get() = availableWords >= 2 && effectiveGoal >= 2

    val collectionLabel: String?
        get() = when (source) {
            SetupSource.LANGUAGE -> selectedLanguage
            SetupSource.FOLDER -> selectedFolder?.name
        }
}

private data class SetupSelection(
    val source: SetupSource = SetupSource.LANGUAGE,
    val language: String? = null,
    val folderId: Long? = null,
    val goal: SessionGoal = SessionGoal.Words(10),
    val customGoalInput: String = "",
    val focusMode: Boolean = false,
    val direction: LearningDirection = LearningDirection.FOREIGN_TO_NATIVE,
    val answerMode: AnswerMode = AnswerMode.CHOICE,
)

class SessionSetupViewModel(
    repository: VocabularyRepository,
    folderRepository: FolderRepository,
) : ViewModel() {
    private val selection = MutableStateFlow(SetupSelection())

    val uiState: StateFlow<SessionSetupUiState> = combine(
        repository.observeLanguageSummaries(),
        folderRepository.observeFolders(),
        selection,
    ) { languages, folders, selected ->
        val effectiveLanguage = selected.language?.takeIf { language ->
            languages.any { it.language == language }
        } ?: languages.firstOrNull()?.language
        val effectiveFolder = selected.folderId
            ?.let { id -> folders.firstOrNull { it.id == id } }
            ?: folders.firstOrNull()

        SessionSetupUiState(
            isLoading = false,
            languages = languages,
            folders = folders,
            source = selected.source,
            selectedLanguage = effectiveLanguage,
            selectedFolder = effectiveFolder,
            goal = selected.goal,
            customGoalInput = selected.customGoalInput,
            focusMode = selected.focusMode,
            mode = LearningMode(
                direction = selected.direction,
                answerMode = selected.answerMode,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SessionSetupUiState(),
    )

    fun onSourceSelected(source: SetupSource) {
        selection.value = selection.value.copy(source = source)
    }

    fun onLanguageSelected(language: String) {
        selection.value = selection.value.copy(language = language)
    }

    fun onFolderSelected(folderId: Long) {
        selection.value = selection.value.copy(folderId = folderId)
    }

    fun onGoalSelected(goal: Int) {
        selection.value = selection.value.copy(goal = SessionGoal.Words(goal))
    }

    fun onAllWordsSelected() {
        selection.value = selection.value.copy(goal = SessionGoal.All)
    }

    fun onCustomGoalSelected() {
        val current = selection.value
        val input = current.customGoalInput.ifBlank { DEFAULT_CUSTOM_GOAL.toString() }
        selection.value = current.copy(
            goal = SessionGoal.Words(
                count = input.toIntOrNull() ?: DEFAULT_CUSTOM_GOAL,
                custom = true,
            ),
            customGoalInput = input,
        )
    }

    fun onCustomGoalChanged(input: String) {
        val digits = input.filter { it.isDigit() }.take(4)
        selection.value = selection.value.copy(
            goal = SessionGoal.Words(digits.toIntOrNull() ?: 0, custom = true),
            customGoalInput = digits,
        )
    }

    fun onFocusModeChanged(enabled: Boolean) {
        selection.value = selection.value.copy(focusMode = enabled)
    }

    fun onDirectionSelected(direction: LearningDirection) {
        selection.value = selection.value.copy(direction = direction)
    }

    fun onAnswerModeSelected(answerMode: AnswerMode) {
        selection.value = selection.value.copy(answerMode = answerMode)
    }

    companion object {
        private const val DEFAULT_CUSTOM_GOAL = 25

        val Factory = appViewModelFactory { container ->
            SessionSetupViewModel(
                repository = container.vocabularyRepository,
                folderRepository = container.folderRepository,
            )
        }
    }
}
