package com.wortschatz.app.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.wortschatz.app.R
import com.wortschatz.app.domain.model.Vocabulary
import com.wortschatz.app.domain.model.VocabularyDraft
import com.wortschatz.app.domain.model.VocabularyFolderSummary
import com.wortschatz.app.domain.model.canonicalLanguageName
import com.wortschatz.app.domain.model.languageDisplayName
import com.wortschatz.app.ui.LocalAppLanguage
import com.wortschatz.app.ui.LocalizedApp
import com.wortschatz.app.ui.components.DuoButton
import com.wortschatz.app.ui.components.DuoFilterChip
import com.wortschatz.app.ui.components.DuoTextField
import com.wortschatz.app.ui.components.rememberIsWideLayout
import com.wortschatz.app.ui.theme.DuoTheme

private val suggestedLanguages = listOf(
    "Deutsch",
    "Englisch",
    "Französisch",
    "Italienisch",
    "Latein",
    "Portugiesisch",
    "Spanisch",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VocabularyEditorSheet(
    initial: Vocabulary?,
    defaultLanguage: String,
    defaultFolderId: Long?,
    knownLanguages: List<String>,
    folders: List<VocabularyFolderSummary>,
    onDismiss: () -> Unit,
    onSave: (VocabularyDraft) -> Unit,
) {
    val colors = DuoTheme.colors
    val appLanguage = LocalAppLanguage.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isWide = rememberIsWideLayout()
    val keyboard = LocalSoftwareKeyboardController.current

    val startingLanguage = initial?.language?.takeIf { it.isNotBlank() } ?: defaultLanguage
    var nativeText by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.nativeText.orEmpty())
    }
    var foreignText by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.foreignText.orEmpty())
    }
    var language by rememberSaveable(initial?.id, startingLanguage, appLanguage) {
        mutableStateOf(languageDisplayName(startingLanguage, appLanguage))
    }
    var category by rememberSaveable(initial?.id) {
        mutableStateOf(initial?.category.orEmpty())
    }
    var folderId by rememberSaveable(initial?.id, defaultFolderId) {
        mutableStateOf(initial?.folderId ?: defaultFolderId)
    }

    var nativeError by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }
    var foreignError by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }
    var languageError by rememberSaveable(initial?.id) { mutableStateOf<String?>(null) }

    val nativeErrorMessage = stringResource(R.string.form_error_native)
    val foreignErrorMessage = stringResource(R.string.form_error_foreign)
    val languageErrorMessage = stringResource(R.string.form_error_language)

    val foreignFocus = remember { FocusRequester() }
    val languageFocus = remember { FocusRequester() }
    val categoryFocus = remember { FocusRequester() }
    val languageOptions = remember(knownLanguages, appLanguage) {
        (suggestedLanguages + knownLanguages.map(::canonicalLanguageName))
            .distinct()
            .map { canonical -> canonical to languageDisplayName(canonical, appLanguage) }
            .distinctBy { (_, display) -> display.lowercase() }
            .sortedBy { (_, display) -> display }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        contentColor = colors.textPrimary,
        scrimColor = colors.scrim,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.outline) },
    ) {
        LocalizedApp(appLanguage) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
        ) {
            Text(
                text = stringResource(
                    if (initial == null) R.string.add_word_title else R.string.edit_word_title,
                ),
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(16.dp))

            if (isWide) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DuoTextField(
                        value = nativeText,
                        onValueChange = {
                            nativeText = it
                            nativeError = null
                        },
                        label = stringResource(R.string.field_native),
                        placeholder = stringResource(R.string.placeholder_native),
                        error = nativeError,
                        imeAction = ImeAction.Next,
                        keyboardActions = KeyboardActions(onNext = { foreignFocus.requestFocus() }),
                        modifier = Modifier.weight(1f),
                    )
                    DuoTextField(
                        value = foreignText,
                        onValueChange = {
                            foreignText = it
                            foreignError = null
                        },
                        label = stringResource(R.string.field_foreign),
                        placeholder = stringResource(R.string.placeholder_foreign),
                        error = foreignError,
                        imeAction = ImeAction.Next,
                        keyboardActions = KeyboardActions(onNext = { languageFocus.requestFocus() }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(foreignFocus),
                    )
                }
            } else {
                DuoTextField(
                    value = nativeText,
                    onValueChange = {
                        nativeText = it
                        nativeError = null
                    },
                    label = stringResource(R.string.field_native),
                    placeholder = stringResource(R.string.placeholder_native),
                    error = nativeError,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(onNext = { foreignFocus.requestFocus() }),
                )
                Spacer(Modifier.height(10.dp))
                DuoTextField(
                    value = foreignText,
                    onValueChange = {
                        foreignText = it
                        foreignError = null
                    },
                    label = stringResource(R.string.field_foreign),
                    placeholder = stringResource(R.string.placeholder_foreign),
                    error = foreignError,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(onNext = { languageFocus.requestFocus() }),
                    modifier = Modifier.focusRequester(foreignFocus),
                )
            }

            Spacer(Modifier.height(12.dp))
            DuoTextField(
                value = language,
                onValueChange = {
                    language = it
                    languageError = null
                },
                label = stringResource(R.string.field_language),
                placeholder = stringResource(R.string.placeholder_language),
                error = languageError,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(onNext = { categoryFocus.requestFocus() }),
                modifier = Modifier.focusRequester(languageFocus),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.language_suggestions),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                languageOptions.forEach { (canonical, display) ->
                    DuoFilterChip(
                        label = display,
                        selected = canonicalLanguageName(language).equals(
                            canonical,
                            ignoreCase = true,
                        ),
                        onClick = {
                            language = display
                            languageError = null
                        },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            DuoTextField(
                value = category,
                onValueChange = { category = it },
                label = stringResource(R.string.field_category),
                placeholder = stringResource(R.string.placeholder_category),
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
                modifier = Modifier.focusRequester(categoryFocus),
            )

            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.field_folder),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(6.dp))
            if (folders.isEmpty()) {
                Text(
                    text = stringResource(R.string.folder_editor_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DuoFilterChip(
                        label = stringResource(R.string.folder_none),
                        selected = folderId == null,
                        onClick = { folderId = null },
                    )
                    folders.forEach { folder ->
                        DuoFilterChip(
                            label = folder.name,
                            selected = folderId == folder.id,
                            onClick = { folderId = folder.id },
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            DuoButton(
                text = stringResource(R.string.action_save),
                leadingIcon = Icons.Rounded.Check,
                onClick = {
                    nativeError = if (nativeText.isBlank()) nativeErrorMessage else null
                    foreignError = if (foreignText.isBlank()) foreignErrorMessage else null
                    languageError = if (language.isBlank()) languageErrorMessage else null
                    if (nativeError == null && foreignError == null && languageError == null) {
                        keyboard?.hide()
                        onSave(
                            VocabularyDraft(
                                nativeText = nativeText,
                                foreignText = foreignText,
                                language = language,
                                category = category,
                                folderId = folderId,
                            ),
                        )
                    }
                },
            )
            Spacer(Modifier.height(8.dp))
        }
        }
    }
}
