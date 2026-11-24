package com.wortschatz.app.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wortschatz.app.R
import com.wortschatz.app.domain.model.DAILY_GOAL_XP
import com.wortschatz.app.domain.model.PracticeDay
import com.wortschatz.app.ui.components.DuoCard
import com.wortschatz.app.ui.components.DuoTextAction
import com.wortschatz.app.ui.components.DuoTopBar
import com.wortschatz.app.ui.theme.DuoTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(onBack: () -> Unit) {
    val viewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DuoTheme.colors
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]

    Scaffold(
        containerColor = colors.background,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = colors.green,
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .widthIn(max = 700.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    DuoTopBar(
                        title = stringResource(R.string.calendar_title),
                        subtitle = stringResource(R.string.calendar_subtitle),
                        onBack = onBack,
                    )

                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        MonthSelector(
                            month = state.month,
                            locale = locale,
                            canGoNext = state.canGoNext,
                            canGoToday = state.month != YearMonth.now(),
                            onPrevious = viewModel::previousMonth,
                            onNext = viewModel::nextMonth,
                            onToday = viewModel::goToCurrentMonth,
                        )
                        Spacer(Modifier.height(12.dp))

                        DuoCard {
                            Column(modifier = Modifier.padding(10.dp)) {
                                WeekdayHeader(locale = locale)
                                Spacer(Modifier.height(4.dp))
                                MonthGrid(
                                    month = state.month,
                                    locale = locale,
                                    dayForDate = state::practiceOn,
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        MonthStats(state = state)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = pluralStringResource(
                                R.plurals.study_days_total,
                                state.totalDays,
                                state.totalDays,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )

                        if (state.monthDays.isEmpty()) {
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = stringResource(R.string.calendar_empty_month),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                            )
                        }

                        Spacer(Modifier.height(18.dp))
                        Legend()
                        Spacer(Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthSelector(
    month: YearMonth,
    locale: Locale,
    canGoNext: Boolean,
    canGoToday: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
) {
    val colors = DuoTheme.colors
    val rawTitle = month.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
    val title = rawTitle.replaceFirstChar { character ->
        if (character.isLowerCase()) character.titlecase(locale) else character.toString()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = stringResource(R.string.calendar_previous_month),
                tint = colors.textPrimary,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        if (canGoToday) {
            DuoTextAction(
                text = stringResource(R.string.calendar_today),
                onClick = onToday,
            )
        }
        IconButton(
            onClick = onNext,
            enabled = canGoNext,
        ) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = stringResource(R.string.calendar_next_month),
                tint = if (canGoNext) colors.textPrimary else colors.disabledText,
            )
        }
    }
}

@Composable
private fun WeekdayHeader(locale: Locale) {
    val colors = DuoTheme.colors
    Row(modifier = Modifier.fillMaxWidth()) {
        DayOfWeek.entries.forEach { day ->
            Text(
                text = day.getDisplayName(TextStyle.SHORT, locale).take(2),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    locale: Locale,
    dayForDate: (LocalDate) -> PracticeDay?,
) {
    val firstDay = month.atDay(1)
    val leadingBlanks = firstDay.dayOfWeek.value - 1
    val cells: List<LocalDate?> = buildList {
        repeat(leadingBlanks) { add(null) }
        for (day in 1..month.lengthOfMonth()) add(month.atDay(day))
        while (size % 7 != 0) add(null)
    }
    val today = LocalDate.now()

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        cells.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                week.forEach { date ->
                    if (date == null) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        DayCell(
                            date = date,
                            practiceDay = dayForDate(date),
                            isToday = date == today,
                            locale = locale,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    practiceDay: PracticeDay?,
    isToday: Boolean,
    locale: Locale,
    modifier: Modifier = Modifier,
) {
    val colors = DuoTheme.colors
    val goalMet = (practiceDay?.xp ?: 0) >= DAILY_GOAL_XP
    val background = when {
        goalMet -> colors.gold.copy(alpha = 0.35f)
        practiceDay != null -> colors.greenSoft
        else -> Color.Transparent
    }
    val borderColor = when {
        goalMet -> colors.streak
        practiceDay != null -> colors.green
        isToday -> colors.streak
        else -> Color.Transparent
    }
    val dateDescription = date.format(
        DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale),
    )
    val description = if (practiceDay != null) {
        buildString {
            append(
                stringResource(
                    R.string.calendar_day_description,
                    dateDescription,
                    practiceDay.xp,
                    practiceDay.masteredWords,
                ),
            )
            if (goalMet) append(stringResource(R.string.cd_week_goal_met))
        }
    } else {
        dateDescription
    }

    Box(
        modifier = modifier
            .height(46.dp)
            .padding(1.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(background, CircleShape)
                .then(
                    if (borderColor != Color.Transparent) {
                        Modifier.border(
                            width = if (isToday) 2.dp else 1.dp,
                            color = borderColor,
                            shape = CircleShape,
                        )
                    } else {
                        Modifier
                    },
                )
                .clearAndSetSemantics { contentDescription = description },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Bold,
                    color = when {
                        goalMet -> colors.goldSolidInk
                        practiceDay != null -> colors.greenInk
                        isToday -> colors.streakDeep
                        else -> colors.textPrimary
                    },
                )
                if (goalMet) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = colors.goldSolidInk,
                        modifier = Modifier.size(10.dp),
                    )
                } else if (practiceDay != null) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(colors.green, CircleShape),
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthStats(state: CalendarUiState) {
    val colors = DuoTheme.colors
    DuoCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CalendarMetric(
                value = state.monthDays.size.toString(),
                label = stringResource(R.string.calendar_month_days),
                tint = colors.greenInk,
                modifier = Modifier.weight(1f),
            )
            CalendarMetric(
                value = "+${state.monthXp}",
                label = stringResource(R.string.calendar_month_xp),
                tint = colors.streakDeep,
                modifier = Modifier.weight(1f),
            )
            CalendarMetric(
                value = state.monthMastered.toString(),
                label = stringResource(R.string.calendar_month_mastered),
                tint = colors.blueDeep,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CalendarMetric(
    value: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = tint,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = DuoTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Legend() {
    val colors = DuoTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LegendItem(color = colors.green, label = stringResource(R.string.calendar_legend_practiced))
        LegendItem(
            color = colors.streak,
            label = stringResource(R.string.calendar_legend_goal),
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape),
        )
        Spacer(Modifier.size(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = DuoTheme.colors.textSecondary,
        )
    }
}

