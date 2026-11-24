package com.wortschatz.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wortschatz.app.data.repository.ProgressRepository
import com.wortschatz.app.domain.model.PracticeDay
import com.wortschatz.app.ui.appViewModelFactory
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CalendarUiState(
    val isLoading: Boolean = true,
    val month: YearMonth = YearMonth.now(),
    val days: List<PracticeDay> = emptyList(),
    val totalDays: Int = 0,
) {
    private val byDate: Map<LocalDate, PracticeDay> = days.associateBy { it.date }
    private val today = LocalDate.now()

    val monthDays: List<PracticeDay> = days.filter {
        YearMonth.from(it.date) == month
    }
    val monthXp: Int = monthDays.sumOf { it.xp }
    val monthMastered: Int = monthDays.sumOf { it.masteredWords }
    val canGoNext: Boolean = month < YearMonth.from(today)

    fun practiceOn(date: LocalDate): PracticeDay? = byDate[date]
}

class CalendarViewModel(
    repository: ProgressRepository,
) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<CalendarUiState> = combine(
        repository.observePracticeDays(),
        month,
    ) { days, selectedMonth ->
        CalendarUiState(
            isLoading = false,
            month = selectedMonth,
            days = days,
            totalDays = days.size,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState(),
    )

    fun previousMonth() {
        month.value = month.value.minusMonths(1)
    }

    fun nextMonth() {
        if (month.value < YearMonth.now()) {
            month.value = month.value.plusMonths(1)
        }
    }

    fun goToCurrentMonth() {
        month.value = YearMonth.now()
    }

    companion object {
        val Factory = appViewModelFactory { container ->
            CalendarViewModel(container.progressRepository)
        }
    }
}
