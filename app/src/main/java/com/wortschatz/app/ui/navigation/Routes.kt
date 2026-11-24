package com.wortschatz.app.ui.navigation

import android.net.Uri
import com.wortschatz.app.domain.session.LearningMode
import com.wortschatz.app.domain.session.SessionSummary

object Routes {
    const val DASHBOARD = "dashboard"
    const val SETUP = "setup"
    const val SETTINGS = "settings"
    const val CALENDAR = "calendar"

    const val LIBRARY_ROUTE = "library?language={language}"
    const val LIBRARY = "library?language="

    const val SESSION_ROUTE =
        "session/{language}/{goal}/{focus}/{folderId}/{direction}/{answerMode}"
    const val RESULT_ROUTE =
        "result/{language}/{total}/{correct}/{answers}/{duration}/{xp}/{perfect}"

    fun library(language: String? = null): String =
        if (language.isNullOrBlank()) "library" else "library?language=${Uri.encode(language)}"

    fun session(
        language: String,
        goal: Int,
        focusMode: Boolean,
        folderId: Long? = null,
        mode: LearningMode = LearningMode.Default,
    ): String = buildString {
        append("session/")
        append(Uri.encode(language))
        append('/')
        append(goal)
        append('/')
        append(focusMode)
        append('/')
        append(folderId ?: 0L)
        append('/')
        append(mode.direction.name)
        append('/')
        append(mode.answerMode.name)
    }

    fun result(summary: SessionSummary): String = buildString {
        append("result/")
        append(Uri.encode(summary.language))
        append('/')
        append(summary.totalWords)
        append('/')
        append(summary.correctAnswers)
        append('/')
        append(summary.answers)
        append('/')
        append(summary.durationMillis)
        append('/')
        append(summary.xpEarned)
        append('/')
        append(summary.perfect)
    }
}
