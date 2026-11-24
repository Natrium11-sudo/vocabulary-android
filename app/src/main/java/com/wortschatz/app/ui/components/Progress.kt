package com.wortschatz.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wortschatz.app.R
import com.wortschatz.app.domain.model.WeekDayProgress
import com.wortschatz.app.ui.theme.DuoTheme
import java.time.format.TextStyle

/** Rounded progress bar with a subtle top highlight like Duolingo's lesson bar. */
@Composable
fun DuoProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    color: Color = DuoTheme.colors.green,
    trackColor: Color = DuoTheme.colors.surfaceMuted,
    contentDescription: String? = null,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 250f),
        label = "duoProgress",
    )
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .semantics {
                if (contentDescription != null) this.contentDescription = contentDescription
                progressBarRangeInfo = ProgressBarRangeInfo(animatedProgress, 0f..1f)
            },
    ) {
        val radius = CornerRadius(size.height / 2f, size.height / 2f)
        drawRoundRect(color = trackColor, cornerRadius = radius, size = size)
        val filledWidth = size.width * animatedProgress
        if (filledWidth > 0f) {
            drawRoundRect(
                color = color,
                size = Size(filledWidth.coerceAtLeast(size.height), size.height),
                cornerRadius = radius,
            )
            // Glossy strip on the upper half.
            drawRoundRect(
                color = Color.White.copy(alpha = 0.22f),
                topLeft = Offset(size.height * 0.5f, size.height * 0.22f),
                size = Size(
                    (filledWidth - size.height).coerceAtLeast(0f),
                    size.height * 0.22f,
                ),
                cornerRadius = CornerRadius(size.height * 0.12f, size.height * 0.12f),
            )
        }
    }
}

/** Circular daily goal ring with arbitrary centered content. */
@Composable
fun GoalRing(
    progress: Float,
    modifier: Modifier = Modifier,
    ringColor: Color = DuoTheme.colors.green,
    trackColor: Color = DuoTheme.colors.greenSoft,
    strokeWidth: Dp = 9.dp,
    content: @Composable () -> Unit,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 180f),
        label = "goalRing",
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - strokePx, size.height - strokePx),
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
            if (animatedProgress > 0f) {
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - strokePx, size.height - strokePx),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
        }
        content()
    }
}

/**
 * Seven compact day markers. Completed days show a flame, today gets a ring, and each
 * marker exposes one readable accessibility label instead of its individual glyphs.
 */
@Composable
fun StreakWeekStrip(
    week: List<WeekDayProgress>,
    modifier: Modifier = Modifier,
) {
    val colors = DuoTheme.colors
    val locale = LocalConfiguration.current.locales[0]
    val goalMetSuffix = stringResource(R.string.cd_week_goal_met)
    val todaySuffix = stringResource(R.string.cd_week_today)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        week.forEach { day ->
            val dayLabel = day.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).take(2)
            val description = buildString {
                append(day.dayOfWeek.getDisplayName(TextStyle.FULL, locale))
                append(": ")
                append(day.xp)
                append(" XP")
                if (day.goalMet) append(goalMetSuffix)
                if (day.isToday) append(todaySuffix)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .clearAndSetSemantics { contentDescription = description },
            ) {
                Text(
                    text = dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (day.isToday) colors.streakDeep else colors.textSecondary,
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            color = when {
                                day.goalMet -> colors.gold
                                day.isToday -> colors.streakSoft
                                else -> colors.surface.copy(alpha = 0.55f)
                            },
                            shape = CircleShape,
                        )
                        .then(
                            if (day.isToday) {
                                Modifier.border(2.dp, colors.streak, CircleShape)
                            } else {
                                Modifier
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (day.goalMet) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = colors.goldSolidInk,
                            modifier = Modifier.size(18.dp),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = if (day.isToday) colors.streak else colors.outline,
                                    shape = CircleShape,
                                ),
                        )
                    }
                }
            }
        }
    }
}

/** Small numeric badge used in result stats and dashboard hints. */
@Composable
fun GoalLabel(progress: Float, modifier: Modifier = Modifier) {
    Text(
        text = "${(progress * 100).toInt()} %",
        style = MaterialTheme.typography.labelMedium,
        color = DuoTheme.colors.textSecondary,
        modifier = modifier,
        textAlign = TextAlign.Center,
    )
}
