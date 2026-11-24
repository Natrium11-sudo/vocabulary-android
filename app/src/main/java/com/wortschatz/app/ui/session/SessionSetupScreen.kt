package com.wortschatz.app.ui.session

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wortschatz.app.R
import com.wortschatz.app.domain.model.LanguageSummary
import com.wortschatz.app.domain.model.VocabularyFolderSummary
import com.wortschatz.app.domain.model.languageDisplayName
import com.wortschatz.app.domain.session.AnswerMode
import com.wortschatz.app.domain.session.LearningDirection
import com.wortschatz.app.domain.session.LearningMode
import com.wortschatz.app.ui.LocalAppLanguage
import com.wortschatz.app.ui.components.DuoButton
import com.wortschatz.app.ui.components.DuoCard
import com.wortschatz.app.ui.components.DuoFilterChip
import com.wortschatz.app.ui.components.DuoTopBar
import com.wortschatz.app.ui.components.EmptyState
import com.wortschatz.app.ui.components.LanguageFlag
import com.wortschatz.app.ui.components.Mascot
import com.wortschatz.app.ui.components.SectionHeader
import com.wortschatz.app.ui.theme.DuoTheme

private val goalOptions = listOf(5, 10, 15, 20)

@Composable
fun SessionSetupScreen(
    onBack: () -> Unit,
    onOpenLibrary: () -> Unit,
    onStart: (
        collectionLabel: String,
        folderId: Long?,
        mode: LearningMode,
        goal: Int,
        focusMode: Boolean,
    ) -> Unit,
) {
    val viewModel: SessionSetupViewModel = viewModel(factory = SessionSetupViewModel.Factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DuoTheme.colors
    val appLanguage = LocalAppLanguage.current
    val selectedLanguage = state.selectedLanguage

    Scaffold(
        containerColor = colors.background,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 700.dp)
                    .fillMaxSize(),
            ) {
                DuoTopBar(
                    title = stringResource(R.string.setup_title),
                    onBack = onBack,
                )

                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.green)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                    ) {
                        Intro()
                        Spacer(Modifier.height(22.dp))

                        SectionHeader(title = stringResource(R.string.setup_source))
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DuoFilterChip(
                                label = stringResource(R.string.source_language),
                                selected = state.source == SetupSource.LANGUAGE,
                                onClick = { viewModel.onSourceSelected(SetupSource.LANGUAGE) },
                                leading = {
                                    Icon(
                                        Icons.Rounded.Translate,
                                        contentDescription = null,
                                        modifier = Modifier.size(17.dp),
                                    )
                                },
                                modifier = Modifier.weight(1f),
                            )
                            DuoFilterChip(
                                label = stringResource(R.string.source_folder),
                                selected = state.source == SetupSource.FOLDER,
                                onClick = { viewModel.onSourceSelected(SetupSource.FOLDER) },
                                leading = {
                                    Icon(
                                        Icons.Rounded.Folder,
                                        contentDescription = null,
                                        modifier = Modifier.size(17.dp),
                                    )
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(Modifier.height(20.dp))

                        if (state.source == SetupSource.LANGUAGE) {
                            if (state.languages.isEmpty()) {
                                DuoCard {
                                    EmptyState(
                                        title = stringResource(R.string.no_languages_title),
                                        body = stringResource(R.string.no_languages_body),
                                        actionLabel = stringResource(R.string.setup_add_words),
                                        onAction = onOpenLibrary,
                                    )
                                }
                            } else {
                                SectionHeader(title = stringResource(R.string.section_language))
                                Spacer(Modifier.height(10.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    state.languages.forEach { language ->
                                        LanguageOptionCard(
                                            language = language,
                                            displayName = languageDisplayName(
                                                language.language,
                                                appLanguage,
                                            ),
                                            selected = language.language == state.selectedLanguage,
                                            onClick = {
                                                viewModel.onLanguageSelected(language.language)
                                            },
                                        )
                                    }
                                }
                            }
                        } else {
                            SectionHeader(title = stringResource(R.string.section_folder))
                            Spacer(Modifier.height(10.dp))
                            if (state.folders.isEmpty()) {
                                DuoCard {
                                    EmptyState(
                                        title = stringResource(R.string.setup_folder_empty_title),
                                        body = stringResource(R.string.setup_folder_empty_body),
                                        actionLabel = stringResource(
                                            R.string.setup_manage_folders,
                                        ),
                                        onAction = onOpenLibrary,
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    state.folders.forEach { folder ->
                                        FolderOptionCard(
                                            folder = folder,
                                            selected = folder.id == state.selectedFolder?.id,
                                            onClick = { viewModel.onFolderSelected(folder.id) },
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        SectionHeader(title = stringResource(R.string.section_direction))
                        Spacer(Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ModeOptionCard(
                                label = stringResource(R.string.direction_foreign_native),
                                description = stringResource(
                                    R.string.direction_foreign_native_desc,
                                ),
                                selected =
                                    state.mode.direction == LearningDirection.FOREIGN_TO_NATIVE,
                                onClick = {
                                    viewModel.onDirectionSelected(
                                        LearningDirection.FOREIGN_TO_NATIVE,
                                    )
                                },
                            )
                            ModeOptionCard(
                                label = stringResource(R.string.direction_native_foreign),
                                description = stringResource(
                                    R.string.direction_native_foreign_desc,
                                ),
                                selected =
                                    state.mode.direction == LearningDirection.NATIVE_TO_FOREIGN,
                                onClick = {
                                    viewModel.onDirectionSelected(
                                        LearningDirection.NATIVE_TO_FOREIGN,
                                    )
                                },
                            )
                        }

                        Spacer(Modifier.height(24.dp))
                        SectionHeader(title = stringResource(R.string.section_answer_mode))
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DuoFilterChip(
                                label = stringResource(R.string.answer_choice),
                                selected = state.mode.answerMode == AnswerMode.CHOICE,
                                onClick = { viewModel.onAnswerModeSelected(AnswerMode.CHOICE) },
                                modifier = Modifier.weight(1f),
                            )
                            DuoFilterChip(
                                label = stringResource(R.string.answer_typing),
                                selected = state.mode.answerMode == AnswerMode.TYPING,
                                onClick = { viewModel.onAnswerModeSelected(AnswerMode.TYPING) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (state.mode.answerMode == AnswerMode.CHOICE) {
                                stringResource(R.string.answer_choice_desc)
                            } else {
                                stringResource(R.string.answer_typing_desc)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )

                        Spacer(Modifier.height(24.dp))
                        SectionHeader(title = stringResource(R.string.section_goal))
                        Spacer(Modifier.height(10.dp))
                        val goal = state.goal
                        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            goalOptions.forEach { option ->
                                GoalOption(
                                    value = option,
                                    selected = goal == SessionGoal.Words(option),
                                    onClick = { viewModel.onGoalSelected(option) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        Spacer(Modifier.height(9.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            DuoFilterChip(
                                label = stringResource(
                                    R.string.goal_all_count,
                                    state.availableWords,
                                ),
                                selected = goal == SessionGoal.All,
                                onClick = viewModel::onAllWordsSelected,
                                modifier = Modifier.weight(1f),
                            )
                            DuoFilterChip(
                                label = stringResource(R.string.goal_custom),
                                selected = state.isCustomGoal,
                                onClick = viewModel::onCustomGoalSelected,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (state.isCustomGoal) {
                            Spacer(Modifier.height(10.dp))
                            val customCount = state.customGoalInput.toIntOrNull()
                            OutlinedTextField(
                                value = state.customGoalInput,
                                onValueChange = viewModel::onCustomGoalChanged,
                                modifier = Modifier.fillMaxWidth(),
                                label = {
                                    Text(
                                        text = stringResource(R.string.goal_custom_label),
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                },
                                singleLine = true,
                                isError = customCount == null || customCount < 2,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                shape = RoundedCornerShape(14.dp),
                                textStyle = MaterialTheme.typography.bodyLarge,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.green,
                                    unfocusedBorderColor = colors.outline,
                                    focusedLabelColor = colors.greenInk,
                                    unfocusedLabelColor = colors.textSecondary,
                                    cursorColor = colors.greenInk,
                                    focusedContainerColor = colors.surface,
                                    unfocusedContainerColor = colors.surface,
                                    errorContainerColor = colors.surface,
                                    errorBorderColor = colors.red,
                                    errorLabelColor = colors.redDeep,
                                ),
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        val goalMessage = when {
                            state.isCustomGoal &&
                                (state.customGoalInput.toIntOrNull() ?: 0) < 2 ->
                                stringResource(
                                    R.string.goal_custom_invalid,
                                    state.availableWords,
                                )
                            goal is SessionGoal.Words &&
                                state.effectiveGoal < goal.count ->
                                stringResource(R.string.goal_capped, state.availableWords)
                            else -> null
                        }
                        Text(
                            text = goalMessage ?: stringResource(R.string.goal_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (goalMessage != null) {
                                colors.streakDeep
                            } else {
                                colors.textSecondary
                            },
                        )

                        Spacer(Modifier.height(24.dp))
                        FocusModeCard(
                            enabled = state.focusMode,
                            onToggle = viewModel::onFocusModeChanged,
                        )

                        if (state.availableWords < 2) {
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = stringResource(R.string.not_enough_words),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.redDeep,
                            )
                            Spacer(Modifier.height(4.dp))
                            DuoButton(
                                text = stringResource(R.string.setup_add_words),
                                onClick = onOpenLibrary,
                                style = com.wortschatz.app.ui.components.DuoButtonStyle.Secondary,
                            )
                        }

                        Spacer(Modifier.height(26.dp))
                    }

                    Surface(color = colors.background) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                            val summaryLabel = state.collectionLabel
                            if (state.canStart && summaryLabel != null) {
                                val displayLabel = if (state.source == SetupSource.LANGUAGE) {
                                    languageDisplayName(summaryLabel, appLanguage)
                                } else {
                                    summaryLabel
                                }
                                Text(
                                    text = stringResource(
                                        R.string.start_summary,
                                        displayLabel,
                                        state.effectiveGoal,
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    textAlign = TextAlign.Center,
                                )
                            }
                            DuoButton(
                                text = stringResource(R.string.start_session),
                                leadingIcon = Icons.Rounded.School,
                                enabled = state.canStart,
                                onClick = {
                                    val label = state.collectionLabel ?: return@DuoButton
                                    onStart(
                                        label,
                                        if (state.source == SetupSource.FOLDER) {
                                            state.selectedFolder?.id
                                        } else {
                                            null
                                        },
                                        state.mode,
                                        state.effectiveGoal,
                                        state.focusMode,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Intro() {
    val colors = DuoTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Mascot(modifier = Modifier.size(92.dp))
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = stringResource(R.string.setup_headline),
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.setup_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun LanguageOptionCard(
    language: LanguageSummary,
    displayName: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = DuoTheme.colors
    val container by animateColorAsState(
        targetValue = if (selected) colors.greenSoft else colors.surface,
        animationSpec = tween(180),
        label = "languageContainer",
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = container,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) colors.green else colors.outline,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LanguageFlag(language = language.language, fontSize = 28.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text = pluralStringResource(
                        R.plurals.vocabularies_count,
                        language.wordCount,
                        language.wordCount,
                    ) + " · " + stringResource(R.string.library_scope_all_folders),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
            SelectionDot(selected = selected)
        }
    }
}

@Composable
private fun FolderOptionCard(
    folder: VocabularyFolderSummary,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = DuoTheme.colors
    val container by animateColorAsState(
        targetValue = if (selected) colors.greenSoft else colors.surface,
        animationSpec = tween(180),
        label = "folderContainer",
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = container,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) colors.green else colors.outline,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(colors.gold.copy(alpha = 0.22f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Folder,
                    contentDescription = null,
                    tint = colors.goldDeep,
                    modifier = Modifier.size(21.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text = pluralStringResource(
                        R.plurals.vocabularies_count,
                        folder.wordCount,
                        folder.wordCount,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
            SelectionDot(selected = selected)
        }
    }
}

@Composable
private fun SelectionDot(selected: Boolean) {
    val colors = DuoTheme.colors
    Box(
        modifier = Modifier
            .size(24.dp)
            .then(
                if (selected) {
                    Modifier.background(colors.green, CircleShape)
                } else {
                    Modifier.border(1.5.dp, colors.outline, CircleShape)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.greenSolidInk,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun ModeOptionCard(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = DuoTheme.colors
    val container by animateColorAsState(
        targetValue = if (selected) colors.blueSoft else colors.surface,
        animationSpec = tween(180),
        label = "modeContainer",
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = container,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) colors.blue else colors.outline,
        ),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) colors.blueDeep else colors.textPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .then(
                        if (selected) {
                            Modifier.background(colors.blue, CircleShape)
                        } else {
                            Modifier.border(1.5.dp, colors.outline, CircleShape)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = colors.blueDeep,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalOption(
    value: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DuoTheme.colors
    val container by animateColorAsState(
        targetValue = if (selected) colors.greenSoft else colors.surface,
        animationSpec = tween(180),
        label = "goalContainer",
    )
    Surface(
        modifier = modifier
            .heightIn(min = 62.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = container,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) colors.green else colors.outline,
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (selected) colors.greenInk else colors.textPrimary,
            )
        }
    }
}

@Composable
private fun FocusModeCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val colors = DuoTheme.colors
    val container by animateColorAsState(
        targetValue = if (enabled) colors.streakSoft else colors.surface,
        animationSpec = tween(200),
        label = "focusContainer",
    )
    DuoCard(
        containerColor = container,
        borderColor = if (enabled) colors.streak.copy(alpha = 0.5f) else colors.outline,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = enabled,
                    role = Role.Switch,
                    onValueChange = onToggle,
                )
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (enabled) colors.gold else colors.surfaceMuted,
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = if (enabled) colors.goldSolidInk else colors.textSecondary,
                        modifier = Modifier.size(21.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.focus_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                    )
                    Text(
                        text = if (enabled) {
                            stringResource(R.string.focus_active)
                        } else {
                            stringResource(R.string.focus_inactive)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enabled) colors.streakDeep else colors.textSecondary,
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.surface,
                        checkedTrackColor = colors.green,
                        checkedBorderColor = colors.greenDark,
                        uncheckedThumbColor = colors.surface,
                        uncheckedTrackColor = colors.surfaceMuted,
                        uncheckedBorderColor = colors.outline,
                    ),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.focus_body),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary.copy(alpha = 0.9f),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.focus_warning),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }
    }
}
