package com.wortschatz.app.ui.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import com.wortschatz.app.R
import com.wortschatz.app.domain.model.DailyProgress
import com.wortschatz.app.domain.model.LanguageSummary
import com.wortschatz.app.ui.components.DuoButton
import com.wortschatz.app.ui.components.DuoCard
import com.wortschatz.app.ui.components.DuoIconButton
import com.wortschatz.app.ui.components.DuoProgressBar
import com.wortschatz.app.ui.components.EmptyState
import com.wortschatz.app.ui.components.GoalRing
import com.wortschatz.app.ui.components.IconBadge
import com.wortschatz.app.ui.components.LanguageFlag
import com.wortschatz.app.ui.components.SectionHeader
import com.wortschatz.app.ui.components.StreakWeekStrip
import com.wortschatz.app.ui.components.rememberIsWideLayout
import com.wortschatz.app.domain.model.languageDisplayName
import com.wortschatz.app.ui.LocalAppLanguage
import com.wortschatz.app.ui.theme.DuoTheme

@Composable
fun DashboardScreen(
    onStartLearning: () -> Unit,
    onOpenLibrary: (String?) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCalendar: () -> Unit,
) {
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DuoTheme.colors
    val isWide = rememberIsWideLayout()

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
                        .widthIn(max = 780.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                ) {
                    Header(streak = state.streak, onOpenSettings = onOpenSettings)
                    Spacer(Modifier.height(18.dp))
                    StreakCard(
                        streak = state.streak,
                        dailyGoal = state.dailyGoal,
                        goalReachedToday = state.todayXp >= state.dailyGoal,
                        week = state.week,
                        onOpenCalendar = onOpenCalendar,
                    )
                    Spacer(Modifier.height(16.dp))

                    val goalCard: @Composable () -> Unit = {
                        DailyGoalCard(
                            today = DailyProgress(
                                xp = state.todayXp,
                                goal = state.dailyGoal,
                                masteredWords = state.todayMastered,
                            ),
                            masteredToday = state.todayMastered,
                        )
                    }
                    val startButton: @Composable () -> Unit = {
                        DuoButton(
                            text = stringResource(R.string.continue_learning),
                            onClick = onStartLearning,
                            leadingIcon = Icons.Rounded.School,
                        )
                    }
                    val libraryCard: @Composable () -> Unit = {
                        LibraryCard(
                            wordCount = state.wordCount,
                            languageCount = state.languages.size,
                            onClick = { onOpenLibrary(null) },
                        )
                    }

                    if (isWide) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                goalCard()
                                Spacer(Modifier.height(14.dp))
                                startButton()
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                libraryCard()
                                Spacer(Modifier.height(14.dp))
                                TodayTipCard(masteredToday = state.todayMastered)
                            }
                        }
                    } else {
                        goalCard()
                        Spacer(Modifier.height(16.dp))
                        startButton()
                        Spacer(Modifier.height(16.dp))
                        libraryCard()
                    }

                    Spacer(Modifier.height(26.dp))
                    if (state.wordCount == 0) {
                        SectionHeader(title = stringResource(R.string.your_languages))
                        Spacer(Modifier.height(10.dp))
                        DuoCard {
                            EmptyState(
                                title = stringResource(R.string.dashboard_empty_title),
                                body = stringResource(R.string.dashboard_empty_body),
                                actionLabel = stringResource(R.string.action_add_word),
                                onAction = { onOpenLibrary(null) },
                            )
                        }
                    } else {
                        LanguagesSection(
                            languages = state.languages,
                            onLanguageClick = onOpenLibrary,
                        )
                    }
                    Spacer(Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
private fun Header(
    streak: Int,
    onOpenSettings: () -> Unit,
) {
    val colors = DuoTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.dashboard_greeting),
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
            )
            Text(
                text = stringResource(R.string.dashboard_title),
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
            )
        }
        DuoIconButton(
            icon = Icons.Rounded.Settings,
            contentDescription = stringResource(R.string.cd_settings),
            onClick = onOpenSettings,
            tint = colors.textSecondary,
        )
        Spacer(Modifier.width(8.dp))
        val description = stringResource(R.string.streak_title) + ": " +
            pluralStringResource(R.plurals.days_count, streak, streak)
        Surface(
            shape = CircleShape,
            color = colors.streakSoft,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.streak.copy(alpha = 0.3f)),
            modifier = Modifier.clearAndSetSemantics { contentDescription = description },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    tint = colors.streak,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = streak.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.streakDeep,
                )
            }
        }
    }
}

@Composable
private fun StreakCard(
    streak: Int,
    dailyGoal: Int,
    goalReachedToday: Boolean,
    week: List<com.wortschatz.app.domain.model.WeekDayProgress>,
    onOpenCalendar: () -> Unit,
) {
    val colors = DuoTheme.colors
    DuoCard(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(role = Role.Button, onClick = onOpenCalendar),
        containerColor = colors.streakSoft,
        borderColor = colors.streak.copy(alpha = 0.3f),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedFlame(active = streak > 0)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.streak_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.streakDeep,
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = streak.toString(),
                            style = MaterialTheme.typography.displayMedium,
                            color = colors.streakDeep,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = pluralStringResource(R.plurals.days_count, streak, streak),
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.streakDeep,
                            modifier = Modifier.padding(bottom = 7.dp),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(colors.gold.copy(alpha = 0.55f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = stringResource(R.string.calendar_open),
                        tint = colors.goldSolidInk,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = when {
                    goalReachedToday -> stringResource(R.string.streak_done)
                    streak == 0 -> stringResource(R.string.streak_empty)
                    else -> stringResource(R.string.streak_keep_alive, dailyGoal)
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.streakDeep.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.week_label),
                style = MaterialTheme.typography.labelSmall,
                color = colors.streakDeep.copy(alpha = 0.8f),
            )
            Spacer(Modifier.height(6.dp))
            StreakWeekStrip(week = week)
        }
    }
}

@Composable
private fun AnimatedFlame(active: Boolean) {
    val colors = DuoTheme.colors
    val transition = rememberInfiniteTransition(label = "flame")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (active) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "flameScale",
    )
    Box(
        modifier = Modifier
            .size(58.dp)
            .background(colors.gold.copy(alpha = 0.9f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.LocalFireDepartment,
            contentDescription = null,
            tint = colors.streak,
            modifier = Modifier
                .size(34.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
        )
    }
}

@Composable
private fun DailyGoalCard(today: DailyProgress, masteredToday: Int) {
    val colors = DuoTheme.colors
    val progressLabel = stringResource(R.string.cd_daily_goal, today.xp, today.goal)
    DuoCard {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GoalRing(
                progress = today.progress,
                modifier = Modifier
                    .size(94.dp)
                    .clearAndSetSemantics { contentDescription = progressLabel },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(today.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary,
                    )
                    Text(
                        text = "XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                    )
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.daily_goal_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.daily_goal_xp, today.xp, today.goal),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.greenInk,
                )
                Spacer(Modifier.height(8.dp))
                DuoProgressBar(
                    progress = today.progress,
                    height = 12.dp,
                    contentDescription = progressLabel,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (today.isGoalReached) {
                        stringResource(R.string.daily_goal_reached)
                    } else {
                        stringResource(R.string.daily_goal_remaining, today.remainingXp)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (today.isGoalReached) colors.greenInk else colors.textSecondary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.today_mastered_progress,
                        pluralStringResource(
                            R.plurals.vocabularies_count,
                            masteredToday,
                            masteredToday,
                        ),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun LibraryCard(wordCount: Int, languageCount: Int, onClick: () -> Unit) {
    val colors = DuoTheme.colors
    DuoCard(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(
                icon = Icons.AutoMirrored.Rounded.MenuBook,
                tint = colors.greenInk,
                background = colors.greenSoft,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dashboard_library_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = pluralStringResource(R.plurals.words_count, wordCount, wordCount) +
                        " · " +
                        pluralStringResource(
                            R.plurals.languages_count,
                            languageCount,
                            languageCount,
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun TodayTipCard(masteredToday: Int) {
    val colors = DuoTheme.colors
    DuoCard(containerColor = colors.blueSoft, borderColor = colors.blue.copy(alpha = 0.25f)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = stringResource(R.string.dashboard_subtitle),
                style = MaterialTheme.typography.titleMedium,
                color = colors.blueDeep,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.goal_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.blueDeep.copy(alpha = 0.9f),
            )
            if (masteredToday > 0) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(
                        R.string.today_mastered_progress,
                        pluralStringResource(
                            R.plurals.vocabularies_count,
                            masteredToday,
                            masteredToday,
                        ),
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.blueDeep,
                )
            }
        }
    }
}

@Composable
private fun LanguagesSection(
    languages: List<LanguageSummary>,
    onLanguageClick: (String) -> Unit,
) {
    val maxCount = languages.maxOfOrNull { it.wordCount }?.coerceAtLeast(1) ?: 1
    SectionHeader(title = stringResource(R.string.your_languages))
    Spacer(Modifier.height(12.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(languages, key = { it.language }) { language ->
            LanguageMiniCard(
                summary = language,
                maxCount = maxCount,
                onClick = { onLanguageClick(language.language) },
            )
        }
    }
}

@Composable
private fun LanguageMiniCard(
    summary: LanguageSummary,
    maxCount: Int,
    onClick: () -> Unit,
) {
    val colors = DuoTheme.colors
    DuoCard(
        modifier = Modifier
            .width(158.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            LanguageFlag(language = summary.language, fontSize = 26.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                text = languageDisplayName(summary.language, LocalAppLanguage.current),
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = pluralStringResource(
                    R.plurals.words_count,
                    summary.wordCount,
                    summary.wordCount,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(10.dp))
            DuoProgressBar(
                progress = summary.wordCount.toFloat() / maxCount,
                height = 6.dp,
                modifier = Modifier.clearAndSetSemantics { },
            )
        }
    }
}
