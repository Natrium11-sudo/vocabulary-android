package com.wortschatz.app.ui.result

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wortschatz.app.R
import com.wortschatz.app.domain.model.languageDisplayName
import com.wortschatz.app.domain.session.SessionSummary
import com.wortschatz.app.ui.LocalAppLanguage
import com.wortschatz.app.ui.components.Confetti
import com.wortschatz.app.ui.components.DuoButton
import com.wortschatz.app.ui.components.DuoButtonStyle
import com.wortschatz.app.ui.components.Mascot
import com.wortschatz.app.ui.components.StatTile
import com.wortschatz.app.ui.theme.DuoTheme

@Composable
fun ResultScreen(
    summary: SessionSummary,
    onHome: () -> Unit,
    onAgain: () -> Unit,
) {
    BackHandler(onBack = onHome)
    val colors = DuoTheme.colors
    val appLanguage = LocalAppLanguage.current
    val background = Brush.verticalGradient(
        colors = listOf(
            if (summary.perfect) colors.gold.copy(alpha = 0.28f) else colors.greenSoft,
            colors.background,
        ),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        if (summary.perfect) {
            Confetti(modifier = Modifier.fillMaxSize())
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 620.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(10.dp))
            if (summary.perfect) {
                PerfectBadge()
                Spacer(Modifier.height(10.dp))
            }
            Mascot(modifier = Modifier.size(148.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (summary.perfect) {
                    stringResource(R.string.result_perfect)
                } else {
                    stringResource(R.string.result_headline)
                },
                style = MaterialTheme.typography.displaySmall,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.result_subtitle,
                    languageDisplayName(summary.language, appLanguage),
                    summary.totalWords,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            if (summary.perfect) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.result_perfect_body, summary.perfectBonus),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.goldDeep,
                )
            }

            Spacer(Modifier.height(22.dp))
            StatsGrid(summary)
            Spacer(Modifier.height(24.dp))

            DuoButton(
                text = stringResource(R.string.action_go_home),
                onClick = onHome,
            )
            Spacer(Modifier.height(10.dp))
            DuoButton(
                text = stringResource(R.string.action_another_round),
                onClick = onAgain,
                style = if (summary.perfect) DuoButtonStyle.Gold else DuoButtonStyle.Secondary,
            )
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun PerfectBadge() {
    val colors = DuoTheme.colors
    Surface(
        shape = CircleShape,
        color = colors.gold,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, colors.streakDeep.copy(alpha = 0.25f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.EmojiEvents,
                contentDescription = null,
                tint = colors.goldSolidInk,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(R.string.result_perfect_badge),
                style = MaterialTheme.typography.labelMedium,
                color = colors.goldSolidInk,
            )
        }
    }
}

@Composable
private fun StatsGrid(summary: SessionSummary) {
    val colors = DuoTheme.colors
    val seconds = (summary.durationMillis / 1000).coerceAtLeast(0)
    val duration = if (seconds >= 60) {
        stringResource(R.string.duration_minutes, seconds / 60, (seconds % 60).toInt())
    } else {
        stringResource(R.string.duration_seconds, seconds)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatTile(
                label = stringResource(R.string.result_stat_words),
                value = pluralStringResource(
                    R.plurals.vocabularies_count,
                    summary.totalWords,
                    summary.totalWords,
                ),
                icon = Icons.Rounded.CheckCircle,
                tint = colors.greenInk,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = stringResource(R.string.result_stat_accuracy),
                value = stringResource(R.string.result_accuracy_value, summary.accuracyPercent),
                icon = Icons.Rounded.TrackChanges,
                tint = colors.blueDeep,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatTile(
                label = stringResource(R.string.result_stat_xp),
                value = stringResource(R.string.result_xp_value, summary.xpEarned),
                icon = Icons.Rounded.Bolt,
                tint = colors.streakDeep,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = stringResource(R.string.result_stat_time),
                value = duration,
                icon = Icons.Rounded.Timer,
                tint = colors.blueDeep,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
