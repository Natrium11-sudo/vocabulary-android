package com.wortschatz.app.ui.session

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

fun Activity.isTaskPinned(): Boolean {
    val manager = getSystemService(ActivityManager::class.java)
    return manager?.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
}

/**
 * Enters Android "screen pinning". For a normal (non device-owner) app the system shows
 * its own confirmation dialog the first time. If the user declines, the app simply stays
 * unpinned; the quiz itself is still back-button protected by the BackHandler in
 * SessionScreen.
 */
fun Activity.enterFocusMode(): Boolean {
    if (isTaskPinned()) return true
    return runCatching {
        startLockTask()
        true
    }.getOrDefault(false)
}

fun Activity.exitFocusMode() {
    if (isTaskPinned()) {
        runCatching { stopLockTask() }
    }
}

/**
 * Starts the lock when the round becomes active and releases it as soon as [release] is
 * true (all words mastered). The DisposableEffect is a safety net for activity teardown:
 * a pinned task must never outlive the quiz screen.
 */
@Composable
fun FocusModeEffect(
    enabled: Boolean,
    release: Boolean,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val shouldRelease by rememberUpdatedState(release)

    LaunchedEffect(enabled, activity) {
        if (enabled) {
            activity?.enterFocusMode()
        }
    }

    LaunchedEffect(release, activity) {
        if (shouldRelease) {
            activity?.exitFocusMode()
        }
    }

    DisposableEffect(activity) {
        onDispose {
            // Keep pinning across configuration changes (rotation). The system task stays
            // pinned and the recreated activity simply re-attaches to it. Only a real
            // teardown or leaving the quiz screen releases the lock.
            if (activity != null && !activity.isChangingConfigurations) {
                activity.exitFocusMode()
            }
        }
    }
}
