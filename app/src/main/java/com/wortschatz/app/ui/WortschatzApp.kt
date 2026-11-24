package com.wortschatz.app.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wortschatz.app.domain.session.AnswerMode
import com.wortschatz.app.domain.session.LearningDirection
import com.wortschatz.app.domain.session.SessionSummary
import com.wortschatz.app.ui.calendar.CalendarScreen
import com.wortschatz.app.ui.dashboard.DashboardScreen
import com.wortschatz.app.ui.library.LibraryScreen
import com.wortschatz.app.ui.navigation.Routes
import com.wortschatz.app.ui.result.ResultScreen
import com.wortschatz.app.ui.session.SessionScreen
import com.wortschatz.app.ui.session.SessionSetupScreen
import com.wortschatz.app.ui.settings.SettingsScreen

@Composable
fun WortschatzApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD,
        enterTransition = {
            fadeIn(tween(220)) + slideInHorizontally(tween(280)) { it / 12 }
        },
        exitTransition = { fadeOut(tween(160)) },
        popEnterTransition = {
            fadeIn(tween(220)) + slideInHorizontally(tween(280)) { -it / 12 }
        },
        popExitTransition = { fadeOut(tween(160)) },
    ) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onStartLearning = {
                    navController.navigate(Routes.SETUP) { launchSingleTop = true }
                },
                onOpenLibrary = { language ->
                    navController.navigate(Routes.library(language))
                },
                onOpenSettings = {
                    navController.navigate(Routes.SETTINGS) { launchSingleTop = true }
                },
                onOpenCalendar = {
                    navController.navigate(Routes.CALENDAR) { launchSingleTop = true }
                },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.CALENDAR) {
            CalendarScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.LIBRARY_ROUTE,
            arguments = listOf(
                navArgument("language") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            LibraryScreen(
                initialLanguage = entry.arguments?.getString("language"),
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SETUP) {
            SessionSetupScreen(
                onBack = { navController.popBackStack() },
                onOpenLibrary = { navController.navigate(Routes.library()) },
                onStart = { label, folderId, mode, goal, focusMode ->
                    navController.navigate(
                        Routes.session(
                            language = label,
                            goal = goal,
                            focusMode = focusMode,
                            folderId = folderId,
                            mode = mode,
                        ),
                    )
                },
            )
        }

        composable(
            route = Routes.SESSION_ROUTE,
            arguments = listOf(
                navArgument("language") { type = NavType.StringType },
                navArgument("goal") { type = NavType.IntType },
                navArgument("focus") { type = NavType.BoolType },
                navArgument("folderId") {
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument("direction") {
                    type = NavType.StringType
                    defaultValue = LearningDirection.FOREIGN_TO_NATIVE.name
                },
                navArgument("answerMode") {
                    type = NavType.StringType
                    defaultValue = AnswerMode.CHOICE.name
                },
            ),
        ) { entry ->
            val args = entry.arguments
            val rawFolderId = args?.getLong("folderId") ?: 0L
            val direction = runCatching {
                LearningDirection.valueOf(
                    args?.getString("direction") ?: LearningDirection.FOREIGN_TO_NATIVE.name,
                )
            }.getOrDefault(LearningDirection.FOREIGN_TO_NATIVE)
            val answerMode = runCatching {
                AnswerMode.valueOf(
                    args?.getString("answerMode") ?: AnswerMode.CHOICE.name,
                )
            }.getOrDefault(AnswerMode.CHOICE)

            SessionScreen(
                language = args?.getString("language").orEmpty(),
                goal = args?.getInt("goal") ?: 10,
                focusMode = args?.getBoolean("focus") ?: false,
                folderId = rawFolderId.takeIf { it != 0L },
                direction = direction,
                answerMode = answerMode,
                onCancel = { navController.popBackStack() },
                onFinished = { summary ->
                    navController.navigate(Routes.result(summary)) {
                        popUpTo(Routes.SETUP) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(
            route = Routes.RESULT_ROUTE,
            arguments = listOf(
                navArgument("language") { type = NavType.StringType },
                navArgument("total") { type = NavType.IntType },
                navArgument("correct") { type = NavType.IntType },
                navArgument("answers") { type = NavType.IntType },
                navArgument("duration") { type = NavType.LongType },
                navArgument("xp") { type = NavType.IntType },
                navArgument("perfect") { type = NavType.BoolType },
            ),
        ) { entry ->
            val args = entry.arguments
            val summary = SessionSummary(
                language = args?.getString("language").orEmpty(),
                totalWords = args?.getInt("total") ?: 0,
                correctAnswers = args?.getInt("correct") ?: 0,
                answers = args?.getInt("answers") ?: 0,
                durationMillis = args?.getLong("duration") ?: 0L,
                xpEarned = args?.getInt("xp") ?: 0,
                perfect = args?.getBoolean("perfect") ?: false,
            )
            ResultScreen(
                summary = summary,
                onHome = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onAgain = {
                    navController.navigate(Routes.SETUP) {
                        popUpTo(Routes.DASHBOARD) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}
