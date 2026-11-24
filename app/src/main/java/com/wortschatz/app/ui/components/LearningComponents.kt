package com.wortschatz.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wortschatz.app.R
import com.wortschatz.app.ui.theme.DuoTheme

enum class AnswerOptionState { Idle, Correct, Wrong, Muted }

/**
 * One answer choice. Colour + icon + accessibility state communicate the result, so the
 * feedback never relies on colour alone.
 */
@Composable
fun AnswerOption(
    text: String,
    letter: String,
    state: AnswerOptionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = DuoTheme.colors
    val shape = RoundedCornerShape(16.dp)
    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            AnswerOptionState.Idle -> colors.surface
            AnswerOptionState.Correct -> colors.greenSoft
            AnswerOptionState.Wrong -> colors.redSoft
            AnswerOptionState.Muted -> colors.surfaceMuted
        },
        animationSpec = tween(180),
        label = "optionBackground",
    )
    val borderColor by animateColorAsState(
        targetValue = when (state) {
            AnswerOptionState.Idle -> colors.outline
            AnswerOptionState.Correct -> colors.green
            AnswerOptionState.Wrong -> colors.red
            AnswerOptionState.Muted -> colors.outline.copy(alpha = 0.45f)
        },
        animationSpec = tween(180),
        label = "optionBorder",
    )
    val contentColor = when (state) {
        AnswerOptionState.Correct -> colors.greenInk
        AnswerOptionState.Wrong -> colors.redDeep
        AnswerOptionState.Muted -> colors.textSecondary
        AnswerOptionState.Idle -> colors.textPrimary
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (state == AnswerOptionState.Idle) 2.dp else 0.dp,
                shape = shape,
                clip = false,
                spotColor = colors.shadow,
            )
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = if (state == AnswerOptionState.Idle) 1.5.dp else 2.dp,
                color = borderColor,
                shape = shape,
            )
            .clickable(
                enabled = enabled && state == AnswerOptionState.Idle,
                role = Role.Button,
                onClick = onClick,
            )
            .heightIn(min = 62.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = when (state) {
                            AnswerOptionState.Correct -> colors.green.copy(alpha = 0.22f)
                            AnswerOptionState.Wrong -> colors.red.copy(alpha = 0.18f)
                            else -> colors.surfaceMuted
                        },
                        shape = RoundedCornerShape(9.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = letter,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor,
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
                modifier = Modifier.weight(1f),
            )
            when (state) {
                AnswerOptionState.Correct -> Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = stringResource(R.string.cd_correct),
                    tint = colors.greenInk,
                    modifier = Modifier.size(23.dp),
                )
                AnswerOptionState.Wrong -> Icon(
                    imageVector = Icons.Rounded.Cancel,
                    contentDescription = stringResource(R.string.cd_wrong),
                    tint = colors.redDeep,
                    modifier = Modifier.size(23.dp),
                )
                else -> Unit
            }
        }
    }
}

/**
 * Bottom feedback panel. It stays until the learner explicitly continues, which gives
 * screen-reader users time to hear the result and the correct translation.
 */
@Composable
fun AnswerFeedbackPanel(
    visible: Boolean,
    isCorrect: Boolean,
    title: String,
    message: String,
    hint: String?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DuoTheme.colors
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(160)) +
            slideInVertically(tween(220)) { it / 3 } +
            expandVertically(tween(220)),
        exit = fadeOut(tween(120)) + slideOutVertically(tween(160)) { it / 4 } + shrinkVertically(tween(160)),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (isCorrect) colors.greenSoft else colors.redSoft,
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (isCorrect) colors.green.copy(alpha = 0.5f) else colors.red.copy(alpha = 0.45f),
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                if (isCorrect) colors.green else colors.red,
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                            contentDescription = null,
                            tint = if (isCorrect) colors.greenSolidInk else Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (isCorrect) colors.greenInk else colors.redDeep,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isCorrect) colors.greenInk else colors.redDeep,
                        )
                    }
                }
                if (hint != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCorrect) colors.greenInk else colors.redDeep,
                        modifier = Modifier.padding(start = 54.dp),
                    )
                }
                Spacer(Modifier.height(14.dp))
                DuoButton(
                    text = stringResource(R.string.action_continue),
                    onClick = onContinue,
                )
            }
        }
    }
}

@Composable
fun FocusBadge(
    text: String,
    modifier: Modifier = Modifier,
    container: Color = DuoTheme.colors.streakSoft,
    content: Color = DuoTheme.colors.streakDeep,
) {
    Surface(
        shape = CircleShape,
        color = container,
        border = androidx.compose.foundation.BorderStroke(1.dp, content.copy(alpha = 0.25f)),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = content,
                textAlign = TextAlign.Center,
            )
        }
    }
}
