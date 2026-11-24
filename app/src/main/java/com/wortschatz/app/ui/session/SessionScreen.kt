package com.wortschatz.app.ui.session

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wortschatz.app.R
import com.wortschatz.app.domain.model.categoryDisplayName
import com.wortschatz.app.domain.model.languageDisplayName
import com.wortschatz.app.domain.session.AnswerMode
import com.wortschatz.app.domain.session.LearningDirection
import com.wortschatz.app.domain.session.SessionSummary
import com.wortschatz.app.ui.LocalAppLanguage
import com.wortschatz.app.ui.components.AnswerFeedbackPanel
import com.wortschatz.app.ui.components.AnswerOption
import com.wortschatz.app.ui.components.AnswerOptionState
import com.wortschatz.app.ui.components.DuoButton
import com.wortschatz.app.ui.components.DuoCard
import com.wortschatz.app.ui.components.DuoProgressBar
import com.wortschatz.app.ui.components.FocusBadge
import com.wortschatz.app.ui.components.LanguageFlag
import com.wortschatz.app.ui.components.Mascot
import com.wortschatz.app.ui.theme.DuoTheme
import kotlinx.coroutines.launch

@Composable
fun SessionScreen(
    language: String,
    goal: Int,
    focusMode: Boolean,
    folderId: Long?,
    direction: LearningDirection,
    answerMode: AnswerMode,
    onCancel: () -> Unit,
    onFinished: (SessionSummary) -> Unit,
) {
    val factory = remember(language, goal, focusMode, folderId, direction, answerMode) {
        SessionViewModel.factory(
            language = language,
            goal = goal,
            focusMode = focusMode,
            folderId = folderId,
            direction = direction,
            answerMode = answerMode,
        )
    }
    val viewModel: SessionViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DuoTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusMessage = stringResource(R.string.session_focus_locked)

    val roundActive = !state.isLoading && !state.error && state.engine != null && state.summary == null

    // Screen pinning only starts once the round is loaded and ends on completion/error.
    FocusModeEffect(
        enabled = focusMode && roundActive,
        release = state.summary != null || state.error,
    )

    LaunchedEffect(state.summary) {
        state.summary?.let(onFinished)
    }

    // Focus mode: swallow the system back action and explain why nothing happens.
    BackHandler(enabled = focusMode && roundActive) {
        scope.launch { snackbarHostState.showSnackbar(focusMessage) }
    }
    // Normal mode: "Abbrechen" and system back both leave immediately, as requested.
    BackHandler(enabled = !focusMode) {
        onCancel()
    }

    Scaffold(
        containerColor = colors.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = colors.textPrimary,
                    contentColor = colors.surface,
                    shape = MaterialTheme.shapes.small,
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 680.dp)
                    .fillMaxSize(),
            ) {
                when {
                    state.isLoading -> LoadingState()
                    state.error -> ErrorState(onCancel = onCancel)
                    else -> {
                        QuizContent(
                            state = state,
                            focusMode = focusMode,
                            onCancel = onCancel,
                            onAnswer = viewModel::submitAnswer,
                            onTypedAnswerChange = viewModel::onTypedAnswerChanged,
                            onSubmitTyped = viewModel::submitTypedAnswer,
                            onContinue = viewModel::continueSession,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizContent(
    state: SessionUiState,
    focusMode: Boolean,
    onCancel: () -> Unit,
    onAnswer: (String) -> Unit,
    onTypedAnswerChange: (String) -> Unit,
    onSubmitTyped: () -> Unit,
    onContinue: () -> Unit,
) {
    val colors = DuoTheme.colors
    val appLanguage = LocalAppLanguage.current
    val keyboard = LocalSoftwareKeyboardController.current
    val engine = state.engine ?: return
    val question = state.question ?: return
    val feedback = state.feedback
    val answered = feedback != null
    val typedMode = question.answerMode == AnswerMode.TYPING
    val answerFieldDescription = stringResource(R.string.cd_answer_input)
    val progressLabel = stringResource(
        R.string.session_progress,
        engine.masteredCount,
        engine.totalWords,
    )

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (focusMode) {
                FocusBadge(text = stringResource(R.string.session_locked_chip))
            } else {
                Surface(
                    shape = CircleShape,
                    color = colors.surfaceMuted,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onCancel),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = stringResource(R.string.action_cancel),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary,
                        )
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                DuoProgressBar(
                    progress = engine.progress,
                    height = 14.dp,
                    contentDescription = progressLabel,
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = progressLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                    )
                    Text(
                        text = pluralStringResource(
                            R.plurals.vocabularies_count,
                            engine.remainingCount,
                            engine.remainingCount,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (typedMode) {
                    stringResource(R.string.session_instruction_typing)
                } else {
                    stringResource(R.string.session_instruction)
                },
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(8.dp))

            DuoCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = colors.surfaceMuted,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            LanguageFlag(language = question.language, fontSize = 15.sp)
                            Text(
                                text = buildString {
                                    append(languageDisplayName(question.language, appLanguage))
                                    if (question.category.isNotBlank()) {
                                        append(" · ")
                                        append(categoryDisplayName(question.category, appLanguage))
                                    }
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary,
                            )
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = question.prompt,
                        style = MaterialTheme.typography.displayMedium,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (question.direction == LearningDirection.FOREIGN_TO_NATIVE) {
                            stringResource(R.string.session_question_label)
                        } else {
                            stringResource(R.string.session_question_reverse)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            if (typedMode) {
                val answerLabel = stringResource(R.string.typed_answer_label)
                OutlinedTextField(
                    value = state.typedAnswer,
                    onValueChange = onTypedAnswerChange,
                    enabled = !answered,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = answerFieldDescription
                        },
                    label = {
                        Text(
                            text = answerLabel,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.typed_answer_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary.copy(alpha = 0.75f),
                        )
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    textStyle = MaterialTheme.typography.titleMedium,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        autoCorrectEnabled = false,
                        capitalization = KeyboardCapitalization.None,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboard?.hide()
                            onSubmitTyped()
                        },
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.green,
                        unfocusedBorderColor = colors.outline,
                        focusedLabelColor = colors.greenInk,
                        unfocusedLabelColor = colors.textSecondary,
                        cursorColor = colors.greenInk,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        disabledContainerColor = colors.surfaceMuted,
                    ),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.typed_answer_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(12.dp))
                DuoButton(
                    text = stringResource(R.string.action_check),
                    onClick = {
                        keyboard?.hide()
                        onSubmitTyped()
                    },
                    enabled = !answered && state.typedAnswer.isNotBlank(),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    question.options.forEachIndexed { index, option ->
                        val optionState = when {
                            !answered -> AnswerOptionState.Idle
                            option == question.correctAnswer -> AnswerOptionState.Correct
                            option == state.selectedAnswer -> AnswerOptionState.Wrong
                            else -> AnswerOptionState.Muted
                        }
                        AnswerOption(
                            text = option,
                            letter = ('A' + index).toString(),
                            state = optionState,
                            enabled = !answered,
                            onClick = { onAnswer(option) },
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        if (feedback != null && state.summary == null) {
            Spacer(Modifier.height(8.dp))
            val title = if (feedback.isCorrect) {
                stringResource(R.string.correct_title)
            } else {
                stringResource(R.string.wrong_title)
            }
            val message = if (feedback.isCorrect) {
                if (feedback.masteredNow) {
                    stringResource(R.string.correct_mastered)
                } else {
                    stringResource(R.string.correct_next)
                }
            } else {
                stringResource(R.string.wrong_body, feedback.correctAnswer)
            }
            AnswerFeedbackPanel(
                visible = true,
                isCorrect = feedback.isCorrect,
                title = title,
                message = message,
                hint = if (feedback.isCorrect) null else stringResource(R.string.wrong_hint),
                onContinue = onContinue,
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun LoadingState() {
    val colors = DuoTheme.colors
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = colors.green)
        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.loading_words),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun ErrorState(onCancel: () -> Unit) {
    val colors = DuoTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Mascot(modifier = Modifier.size(120.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.session_no_words),
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        DuoButton(
            text = stringResource(R.string.action_back),
            onClick = onCancel,
            style = com.wortschatz.app.ui.components.DuoButtonStyle.Secondary,
        )
    }
}
