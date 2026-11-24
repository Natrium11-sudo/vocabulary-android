package com.wortschatz.app.ui.settings

import androidx.lifecycle.ViewModel
import com.wortschatz.app.data.SettingsStore
import com.wortschatz.app.domain.settings.AppLanguage
import com.wortschatz.app.domain.settings.MascotType
import com.wortschatz.app.domain.settings.ThemeMode
import com.wortschatz.app.domain.settings.UserPreferences
import com.wortschatz.app.ui.appViewModelFactory
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val settingsStore: SettingsStore,
) : ViewModel() {
    val preferences: StateFlow<UserPreferences> = settingsStore.preferences

    fun setThemeMode(mode: ThemeMode) = settingsStore.setThemeMode(mode)

    fun setAppLanguage(language: AppLanguage) = settingsStore.setAppLanguage(language)

    fun setMascot(mascot: MascotType) = settingsStore.setMascot(mascot)

    companion object {
        val Factory = appViewModelFactory { container ->
            SettingsViewModel(container.settingsStore)
        }
    }
}
