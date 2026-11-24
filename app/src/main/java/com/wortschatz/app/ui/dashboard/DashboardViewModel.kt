package com.wortschatz.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wortschatz.app.data.repository.ProgressRepository
import com.wortschatz.app.data.repository.VocabularyRepository
import com.wortschatz.app.domain.model.DAILY_GOAL_XP
import com.wortschatz.app.domain.model.LanguageSummary
import com.wortschatz.app.domain.model.WeekDayProgress
import com.wortschatz.app.ui.appViewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val isLoading: Boolean = true,
    val streak: Int = 0,
    val todayXp: Int = 0,
    val dailyGoal: Int = DAILY_GOAL_XP,
    val todayMastered: Int = 0,
    val week: List<WeekDayProgress> = emptyList(),
    val wordCount: Int = 0,
    val languages: List<LanguageSummary> = emptyList(),
)

class DashboardViewModel(
    private val vocabularyRepository: VocabularyRepository,
    progressRepository: ProgressRepository,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        vocabularyRepository.observeAll(),
        progressRepository.observeSummary(),
    ) { words, progress ->
        DashboardUiState(
            isLoading = false,
            streak = progress.streak,
            todayXp = progress.today.xp,
            dailyGoal = progress.today.goal,
            todayMastered = progress.today.masteredWords,
            week = progress.week,
            wordCount = words.size,
            languages = words.groupingBy { it.language }
                .eachCount()
                .map { (language, count) -> LanguageSummary(language, count) }
                .sortedWith(
                    compareByDescending<LanguageSummary> { it.wordCount }
                        .thenBy { it.language },
                ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(),
    )

    companion object {
        val Factory = appViewModelFactory { container ->
            DashboardViewModel(container.vocabularyRepository, container.progressRepository)
        }
    }
}
