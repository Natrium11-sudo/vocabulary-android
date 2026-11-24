package com.wortschatz.app.ui

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.wortschatz.app.domain.settings.AppLanguage
import java.util.Locale

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.GERMAN }

/**
 * Provides localized resources for the whole Compose tree without changing the system
 * locale. The wrapper extends the activity, so helpers such as findActivity() still work.
 */
@Composable
fun LocalizedApp(
    language: AppLanguage,
    content: @Composable () -> Unit,
) {
    val baseContext = LocalContext.current
    val baseConfiguration = LocalConfiguration.current
    val localizedContext = androidx.compose.runtime.remember(
        baseContext,
        baseConfiguration,
        language,
    ) {
        LocalizedContext(baseContext, Locale.forLanguageTag(language.tag))
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration,
        LocalAppLanguage provides language,
        content = content,
    )
}

private class LocalizedContext(
    base: Context,
    locale: Locale,
) : ContextWrapper(base) {
    private val localizedResources: Resources = base.createConfigurationContext(
        Configuration(base.resources.configuration).apply { setLocale(locale) },
    ).resources

    override fun getResources(): Resources = localizedResources
}
