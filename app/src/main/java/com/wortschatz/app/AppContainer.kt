package com.wortschatz.app

import android.content.Context
import com.wortschatz.app.data.SettingsStore
import com.wortschatz.app.data.local.AppDatabase
import com.wortschatz.app.data.repository.FolderRepository
import com.wortschatz.app.data.repository.ProgressRepository
import com.wortschatz.app.data.repository.SeedData
import com.wortschatz.app.data.repository.VocabularyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Plain manual dependency injection. It keeps the sample small and testable without
 * pulling in a DI framework for a handful of objects.
 */
class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)
    val settingsStore = SettingsStore(context)
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val vocabularyRepository = VocabularyRepository(database.vocabularyDao())
    val folderRepository = FolderRepository(
        database = database,
        folderDao = database.folderDao(),
        vocabularyDao = database.vocabularyDao(),
    )
    val progressRepository = ProgressRepository(database, database.studyDayDao())

    init {
        applicationScope.launch {
            if (!settingsStore.isSeeded) {
                runCatching {
                    vocabularyRepository.seedIfEmpty(SeedData.words)
                    settingsStore.isSeeded = true
                }
            }
        }
    }
}
