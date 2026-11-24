package com.wortschatz.app.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wortschatz.app.R
import com.wortschatz.app.domain.model.Vocabulary
import com.wortschatz.app.domain.model.VocabularyFolderSummary
import com.wortschatz.app.domain.model.languageDisplayName
import com.wortschatz.app.ui.LocalAppLanguage
import com.wortschatz.app.ui.LocalizedApp
import com.wortschatz.app.ui.components.CategoryPill
import com.wortschatz.app.ui.components.DuoCard
import com.wortschatz.app.ui.components.DuoFilterChip
import com.wortschatz.app.ui.components.DuoIconButton
import com.wortschatz.app.ui.components.DuoSearchField
import com.wortschatz.app.ui.components.DuoTextField
import com.wortschatz.app.ui.components.DuoTopBar
import com.wortschatz.app.ui.components.EmptyState
import com.wortschatz.app.ui.components.LanguageFlag
import com.wortschatz.app.ui.theme.DuoTheme

@Composable
fun LibraryScreen(
    initialLanguage: String?,
    onBack: () -> Unit,
) {
    val factory = remember(initialLanguage) { LibraryViewModel.factory(initialLanguage) }
    val viewModel: LibraryViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DuoTheme.colors
    val appLanguage = LocalAppLanguage.current
    val snackbarHostState = remember { SnackbarHostState() }

    val wordAddedMessage = stringResource(R.string.word_added)
    val wordUpdatedMessage = stringResource(R.string.word_updated)
    val wordDeletedMessage = stringResource(R.string.word_deleted)
    val folderCreatedMessage = stringResource(R.string.folder_created)
    val folderDeletedMessage = stringResource(R.string.folder_deleted)
    val deleteTitle = stringResource(R.string.delete_word_title)
    val deleteConfirm = stringResource(R.string.action_delete)
    val cancelLabel = stringResource(R.string.action_cancel)
    val allFoldersLabel = stringResource(R.string.library_scope_all_folders)

    var showEditor by rememberSaveable { mutableStateOf(false) }
    var showFolderManager by rememberSaveable { mutableStateOf(false) }
    var editingWord by remember { mutableStateOf<Vocabulary?>(null) }
    var deleteTarget by remember { mutableStateOf<Vocabulary?>(null) }

    val activeFolderName = state.folders
        .firstOrNull { it.id == state.selectedFolderId }
        ?.name
    val groupedWords = remember(state.words) {
        state.words.groupBy { it.language }.toList()
    }

    LaunchedEffect(
        viewModel,
        wordAddedMessage,
        wordUpdatedMessage,
        wordDeletedMessage,
        folderCreatedMessage,
        folderDeletedMessage,
    ) {
        viewModel.events.collect { event ->
            val message = when (event) {
                is LibraryEvent.Saved -> if (event.isNew) wordAddedMessage else wordUpdatedMessage
                LibraryEvent.Deleted -> wordDeletedMessage
                LibraryEvent.FolderCreated -> folderCreatedMessage
                LibraryEvent.FolderDeleted -> folderDeletedMessage
            }
            snackbarHostState.showSnackbar(message)
        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            DuoTopBar(
                title = stringResource(R.string.library_title),
                subtitle = stringResource(R.string.library_subtitle),
                onBack = onBack,
                actions = {
                    DuoIconButton(
                        icon = Icons.Rounded.Folder,
                        contentDescription = stringResource(R.string.cd_manage_folders),
                        onClick = { showFolderManager = true },
                        tint = colors.goldDeep,
                        container = colors.streakSoft,
                    )
                    DuoIconButton(
                        icon = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.cd_add_word),
                        onClick = {
                            editingWord = null
                            showEditor = true
                        },
                        tint = colors.greenInk,
                        container = colors.greenSoft,
                    )
                },
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                DuoSearchField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp),
                ) {
                    item {
                        DuoFilterChip(
                            label = stringResource(R.string.filter_all_languages),
                            selected = state.selectedLanguage == null,
                            onClick = { viewModel.onLanguageSelected(null) },
                        )
                    }
                    items(state.languages, key = { it.language }) { language ->
                        DuoFilterChip(
                            label = languageDisplayName(language.language, appLanguage),
                            selected = state.selectedLanguage == language.language,
                            onClick = { viewModel.onLanguageSelected(language.language) },
                            leading = {
                                LanguageFlag(language = language.language, fontSize = 15.sp)
                            },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp),
                ) {
                    item {
                        DuoFilterChip(
                            label = stringResource(R.string.folder_all),
                            selected = state.selectedFolderId == null,
                            onClick = { viewModel.onFolderSelected(null) },
                            leading = {
                                Icon(
                                    Icons.Rounded.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                )
                            },
                        )
                    }
                    items(state.folders, key = { it.id }) { folder ->
                        DuoFilterChip(
                            label = stringResource(
                                R.string.folder_chip,
                                folder.name,
                                folder.wordCount,
                            ),
                            selected = state.selectedFolderId == folder.id,
                            onClick = { viewModel.onFolderSelected(folder.id) },
                            leading = {
                                Icon(
                                    Icons.Rounded.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                )
                            },
                        )
                    }
                    item {
                        DuoFilterChip(
                            label = stringResource(R.string.folder_add),
                            selected = false,
                            onClick = { showFolderManager = true },
                            leading = {
                                Icon(
                                    Icons.Rounded.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                )
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> CircularProgressIndicator(
                        color = colors.green,
                        modifier = Modifier.align(Alignment.Center),
                    )

                    state.words.isEmpty() && state.totalWords == 0 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            EmptyState(
                                title = stringResource(R.string.dashboard_empty_title),
                                body = stringResource(R.string.dashboard_empty_body),
                                actionLabel = stringResource(R.string.action_add_word),
                                onAction = {
                                    editingWord = null
                                    showEditor = true
                                },
                            )
                        }
                    }

                    state.words.isEmpty() &&
                        state.selectedFolderId != null &&
                        state.query.isBlank() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            EmptyState(
                                title = stringResource(R.string.folder_empty_title),
                                body = stringResource(R.string.folder_empty_body),
                                actionLabel = stringResource(R.string.action_add_word),
                                onAction = {
                                    editingWord = null
                                    showEditor = true
                                },
                            )
                        }
                    }

                    state.words.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            EmptyState(
                                title = stringResource(R.string.empty_search_title),
                                body = stringResource(R.string.empty_search_body),
                                actionLabel = stringResource(R.string.reset_filters),
                                onAction = viewModel::clearFilters,
                            )
                        }
                    }

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 2.dp,
                            bottom = 28.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        groupedWords.forEach { (language, words) ->
                            item(key = "language-$language") {
                                LanguageSectionHeader(
                                    language = language,
                                    wordCount = words.size,
                                    scopeLabel = activeFolderName ?: allFoldersLabel,
                                )
                            }
                            items(words, key = { it.id }) { word ->
                                VocabularyCard(
                                    word = word,
                                    folderName = state.folders
                                        .firstOrNull { it.id == word.folderId }
                                        ?.name,
                                    onEdit = {
                                        editingWord = word
                                        showEditor = true
                                    },
                                    onDelete = { deleteTarget = word },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        VocabularyEditorSheet(
            initial = editingWord,
            defaultLanguage = state.selectedLanguage.orEmpty(),
            defaultFolderId = state.selectedFolderId,
            knownLanguages = state.languages.map { it.language },
            folders = state.folders,
            onDismiss = {
                showEditor = false
                editingWord = null
            },
            onSave = { draft ->
                viewModel.save(editingWord?.id, draft)
                showEditor = false
                editingWord = null
            },
        )
    }

    if (showFolderManager) {
        FolderManagerDialog(
            folders = state.folders,
            onDismiss = { showFolderManager = false },
            onCreate = viewModel::createFolder,
            onDelete = viewModel::deleteFolder,
        )
    }

    deleteTarget?.let { target ->
        val deleteBody = stringResource(R.string.delete_word_body, target.nativeText)
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(deleteTitle) },
            text = { Text(deleteBody) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(target)
                        deleteTarget = null
                    },
                ) {
                    Text(
                        text = deleteConfirm,
                        color = colors.redDeep,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(
                        text = cancelLabel,
                        color = colors.textSecondary,
                    )
                }
            },
            containerColor = colors.surface,
            textContentColor = colors.textSecondary,
            titleContentColor = colors.textPrimary,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    tint = colors.redDeep,
                )
            },
        )
    }
}

@Composable
private fun FolderManagerDialog(
    folders: List<VocabularyFolderSummary>,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    onDelete: (Long) -> Unit,
) {
    val colors = DuoTheme.colors
    val appLanguage = LocalAppLanguage.current
    var newName by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    val requiredError = stringResource(R.string.folder_name_required)
    val existsError = stringResource(R.string.folder_name_exists)

    fun tryCreate() {
        val trimmed = newName.trim()
        nameError = when {
            trimmed.isEmpty() -> requiredError
            folders.any { it.name.equals(trimmed, ignoreCase = true) } -> existsError
            else -> null
        }
        if (nameError == null) {
            onCreate(trimmed)
            newName = ""
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            LocalizedApp(appLanguage) {
                Text(stringResource(R.string.folder_manage_title))
            }
        },
        text = {
            LocalizedApp(appLanguage) {
                Column {
                    Text(
                        text = stringResource(R.string.folder_manage_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                    )
                    Spacer(Modifier.height(12.dp))
                    DuoTextField(
                        value = newName,
                        onValueChange = {
                            newName = it
                            nameError = null
                        },
                        label = stringResource(R.string.folder_name_label),
                        placeholder = stringResource(R.string.folder_name_placeholder),
                        error = nameError,
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { tryCreate() },
                        enabled = newName.isNotBlank(),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = colors.greenInk,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.folder_add),
                            color = colors.greenInk,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    if (folders.isEmpty()) {
                        Text(
                            text = stringResource(R.string.folder_empty_list),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 260.dp)
                                .verticalScroll(rememberScrollState()),
                        ) {
                            folders.forEach { folder ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Folder,
                                        contentDescription = null,
                                        tint = colors.goldDeep,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = folder.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = pluralStringResource(
                                                R.plurals.words_count,
                                                folder.wordCount,
                                                folder.wordCount,
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.textSecondary,
                                        )
                                    }
                                    IconButton(onClick = { onDelete(folder.id) }) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = stringResource(
                                                R.string.folder_delete_cd,
                                                folder.name,
                                            ),
                                            tint = colors.redDeep,
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.folder_delete_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                    }
                }
            }
        },
        confirmButton = {
            LocalizedApp(appLanguage) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.action_done),
                        color = colors.greenInk,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
        containerColor = colors.surface,
        textContentColor = colors.textSecondary,
        titleContentColor = colors.textPrimary,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Folder,
                contentDescription = null,
                tint = colors.goldDeep,
            )
        },
    )
}

@Composable
private fun LanguageSectionHeader(
    language: String,
    wordCount: Int,
    scopeLabel: String,
) {
    val colors = DuoTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LanguageFlag(language = language, fontSize = 24.sp)
        Spacer(Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = languageDisplayName(language, LocalAppLanguage.current),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
            )
            Text(
                text = pluralStringResource(
                    R.plurals.words_count,
                    wordCount,
                    wordCount,
                ) + " · " + scopeLabel,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun VocabularyCard(
    word: Vocabulary,
    folderName: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = DuoTheme.colors
    val appLanguage = LocalAppLanguage.current
    DuoCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LanguageFlag(language = word.language, fontSize = 18.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = languageDisplayName(word.language, appLanguage),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (word.category.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        CategoryPill(text = word.category)
                    }
                }
                DuoIconButton(
                    icon = Icons.Rounded.Edit,
                    contentDescription = stringResource(R.string.cd_edit_word, word.nativeText),
                    onClick = onEdit,
                    tint = colors.textSecondary,
                )
                Spacer(Modifier.width(6.dp))
                DuoIconButton(
                    icon = Icons.Rounded.DeleteOutline,
                    contentDescription = stringResource(R.string.cd_delete_word, word.nativeText),
                    onClick = onDelete,
                    tint = colors.redDeep,
                    container = colors.redSoft,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = word.nativeText,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = word.foreignText,
                style = MaterialTheme.typography.titleMedium,
                color = colors.greenInk,
            )
            if (folderName != null) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Folder,
                        contentDescription = null,
                        tint = colors.goldDeep,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = folderName,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
