package com.wortschatz.app

import android.app.ActivityManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wortschatz.app.ui.LocalizedApp
import com.wortschatz.app.ui.WortschatzApp
import com.wortschatz.app.ui.components.LocalMascot
import com.wortschatz.app.ui.theme.WortschatzTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsStore = (application as WortschatzApplication).container.settingsStore

        setContent {
            val preferences by settingsStore.preferences.collectAsStateWithLifecycle()
            val darkTheme = preferences.themeMode.isDark(isSystemInDarkTheme())
            val view = LocalView.current

            SideEffect {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }

            LocalizedApp(language = preferences.appLanguage) {
                CompositionLocalProvider(LocalMascot provides preferences.mascot) {
                    WortschatzTheme(darkTheme = darkTheme) {
                        WortschatzApp()
                    }
                }
            }
        }
    }

    /**
     * Safety net: if the activity is destroyed while the task is pinned (for example a
     * crash during a focus round), release the pin so the user is not trapped.
     * The normal release path is SessionScreen, which calls stopLockTask() as soon as
     * every vocabulary reached streak >= 2.
     */
    override fun onDestroy() {
        // Do not release the pin merely because of a rotation: the task stays pinned and
        // the recreated activity continues the round. Only a genuine teardown releases it.
        if (!isChangingConfigurations && isTaskPinned()) {
            runCatching { stopLockTask() }
        }
        super.onDestroy()
    }

    private fun isTaskPinned(): Boolean {
        val manager = getSystemService(ActivityManager::class.java)
        return manager?.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
    }
}
