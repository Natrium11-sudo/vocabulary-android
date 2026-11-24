package com.wortschatz.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wortschatz.app.data.repository.FolderRepository
import com.wortschatz.app.data.repository.VocabularyRepository
import com.wortschatz.app.domain.model.LanguageSummary
import com.wortschatz.app.domain.model.Vocabulary
import com.wortschatz.app.domain.model.VocabularyDraft
import com.wortschatz.app.domain.model.VocabularyFolderSummary
import com.wortschatz.app.domain.model.canonicalLanguageName
import com.wortschatz.app.domain.model.categorySearchTerms
import com.wortschatz.app.domain.model.languageSearchTerms
import com.wortschatz.app.ui.appViewModelFactory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryUiState(
    val isLoading: Boolean = true,
    val words: List<Vocabulary> = emptyList(),
    val languages: List<LanguageSummary> = emptyList(),
    val folders: List<VocabularyFolderSummary> = emptyList(),
    val totalWords: Int = 0,
    val query: String = "",
    val selectedLanguage: String? = null,
    val selectedFolderId: Long? = null,
)

sealed interface LibraryEvent {
    data class Saved(val isNew: Boolean) : LibraryEvent
    data object Deleted : LibraryEvent
    data object FolderCreated : LibraryEvent
    data object FolderDeleted : LibraryEvent
}

class LibraryViewModel(
    private val repository: VocabularyRepository,
    private val folderRepository: FolderRepository,
    initialLanguage: String? = null,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedLanguage = MutableStateFlow(initialLanguage)
    private val selectedFolderId = MutableStateFlow<Long?>(null)
    private val _events = MutableSharedFlow<LibraryEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    val uiState: StateFlow<LibraryUiState> = combine(
        repository.observeAll(),
        folderRepository.observeFolders(),
        query,
        selectedLanguage,
        selectedFolderId,
    ) { allWords, folders, searchQuery, languageFilter, folderFilter ->
        val effectiveLanguage = languageFilter?.takeIf { filter ->
            allWords.any { it.language == filter }
        }
        val effectiveFolderId = folderFilter?.takeIf { id ->
            folders.any { it.id == id }
        }
        val folderNames = folders.associate { it.id to it.name }
        val normalizedQuery = searchQuery.trim()
        val filtered = allWords
            .asSequence()
            .filter { word ->
                effectiveLanguage == null || word.language == effectiveLanguage
            }
            .filter { word ->
                effectiveFolderId == null || word.folderId == effectiveFolderId
            }
            .filter { word ->
                normalizedQuery.isEmpty() ||
                    word.nativeText.contains(normalizedQuery, ignoreCase = true) ||
                    word.foreignText.contains(normalizedQuery, ignoreCase = true) ||
                    languageSearchTerms(word.language).any {
                        it.contains(normalizedQuery, ignoreCase = true)
                    } ||
                    categorySearchTerms(word.category).any {
                        it.contains(normalizedQuery, ignoreCase = true)
                    } ||
                    folderNames[word.folderId]?.contains(
                        normalizedQuery,
                        ignoreCase = true,
                    ) == true
            }
            .toList()

        LibraryUiState(
            isLoading = false,
            words = filtered,
            languages = allWords.groupingBy { it.language }
                .eachCount()
                .map { (language, count) -> LanguageSummary(language, count) }
                .sortedBy { it.language },
            folders = folders,
            totalWords = allWords.size,
            query = searchQuery,
            selectedLanguage = effectiveLanguage,
            selectedFolderId = effectiveFolderId,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState(),
    )

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onLanguageSelected(language: String?) {
        selectedLanguage.value = language
    }

    fun onFolderSelected(folderId: Long?) {
        selectedFolderId.value = folderId
    }

    fun save(existingId: Long?, draft: VocabularyDraft) {
        val existing = uiState.value.words.firstOrNull { it.id == existingId }
        viewModelScope.launch {
            repository.upsert(
                Vocabulary(
                    id = existingId ?: 0L,
                    nativeText = draft.nativeText.trim(),
                    foreignText = draft.foreignText.trim(),
                    language = canonicalLanguageName(draft.language),
                    category = draft.category.trim(),
                    folderId = draft.folderId,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                ),
            )
            _events.emit(LibraryEvent.Saved(isNew = existingId == null))
        }
    }

    fun delete(vocabulary: Vocabulary) {
        viewModelScope.launch {
            repository.delete(vocabulary)
            _events.emit(LibraryEvent.Deleted)
        }
    }

    fun createFolder(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        if (uiState.value.folders.any { it.name.equals(trimmed, ignoreCase = true) }) return
        viewModelScope.launch {
            folderRepository.create(trimmed)
            _events.emit(LibraryEvent.FolderCreated)
        }
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            folderRepository.delete(folderId)
            _events.emit(LibraryEvent.FolderDeleted)
        }
    }

    fun clearFilters() {
        query.update { "" }
        selectedLanguage.value = null
        selectedFolderId.value = null
    }

    companion object {
        fun factory(initialLanguage: String?): ViewModelProvider.Factory =
            appViewModelFactory { container ->
                LibraryViewModel(
                    repository = container.vocabularyRepository,
                    folderRepository = container.folderRepository,
                    initialLanguage = canonicalLanguageName(initialLanguage.orEmpty())
                        .takeIf { it.isNotBlank() },
                )
            }
    }
}
