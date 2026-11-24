package com.wortschatz.app.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wortschatz.app.R
import com.wortschatz.app.domain.settings.AppLanguage
import com.wortschatz.app.domain.settings.MascotType
import com.wortschatz.app.domain.settings.ThemeMode
import com.wortschatz.app.ui.components.DuoCard
import com.wortschatz.app.ui.components.DuoTopBar
import com.wortschatz.app.ui.components.Mascot
import com.wortschatz.app.ui.components.SectionHeader
import com.wortschatz.app.ui.theme.DuoTheme

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val colors = DuoTheme.colors

    Scaffold(
        containerColor = colors.background,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 700.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
            DuoTopBar(
                title = stringResource(R.string.settings_title),
                subtitle = stringResource(R.string.settings_subtitle),
                onBack = onBack,
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                SectionHeader(title = stringResource(R.string.settings_appearance))
                Spacer(Modifier.height(10.dp))
                ThemeMode.entries.forEach { mode ->
                    SettingChoiceRow(
                        icon = mode.icon(),
                        label = stringResource(mode.labelRes()),
                        description = stringResource(mode.descriptionRes()),
                        selected = preferences.themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(22.dp))
                SectionHeader(title = stringResource(R.string.settings_language))
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.settings_language_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(10.dp))
                AppLanguage.entries.forEach { language ->
                    SettingChoiceRow(
                        icon = Icons.Rounded.Language,
                        label = stringResource(
                            if (language == AppLanguage.GERMAN) {
                                R.string.settings_language_german
                            } else {
                                R.string.settings_language_english
                            },
                        ),
                        description = language.tag.uppercase(),
                        selected = preferences.appLanguage == language,
                        onClick = { viewModel.setAppLanguage(language) },
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(22.dp))
                SectionHeader(title = stringResource(R.string.settings_mascot))
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.settings_mascot_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    MascotType.entries.forEach { mascot ->
                        MascotChoiceCard(
                            mascot = mascot,
                            selected = preferences.mascot == mascot,
                            onClick = { viewModel.setMascot(mascot) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(preferences.mascot.taglineRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )

                Spacer(Modifier.height(24.dp))
                DuoCard(containerColor = colors.surfaceMuted) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = colors.greenInk,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.settings_footer),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                    }
                }
                Spacer(Modifier.height(30.dp))
            }
            }
        }
    }
}

@Composable
private fun SettingChoiceRow(
    icon: ImageVector,
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = DuoTheme.colors
    val container by animateColorAsState(
        targetValue = if (selected) colors.greenSoft else colors.surface,
        animationSpec = tween(180),
        label = "settingChoice",
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = container,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) colors.green else colors.outline,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        if (selected) colors.green.copy(alpha = 0.18f) else colors.surfaceMuted,
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) colors.greenInk else colors.textSecondary,
                    modifier = Modifier.size(19.dp),
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
            SelectionCheck(selected = selected)
        }
    }
}

@Composable
private fun MascotChoiceCard(
    mascot: MascotType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DuoTheme.colors
    Surface(
        modifier = modifier.selectable(
            selected = selected,
            role = Role.RadioButton,
            onClick = onClick,
        ),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) colors.greenSoft else colors.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) colors.green else colors.outline,
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Mascot(
                type = mascot,
                animate = false,
                modifier = Modifier.size(62.dp),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(mascot.labelRes()),
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) colors.greenInk else colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            SelectionCheck(selected = selected)
        }
    }
}

@Composable
private fun SelectionCheck(selected: Boolean) {
    val colors = DuoTheme.colors
    Box(
        modifier = Modifier
            .size(22.dp)
            .background(
                if (selected) colors.green else colors.surfaceMuted,
                CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.greenSolidInk,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system
    ThemeMode.LIGHT -> R.string.settings_theme_light
    ThemeMode.DARK -> R.string.settings_theme_dark
}

private fun ThemeMode.descriptionRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system_desc
    ThemeMode.LIGHT -> R.string.settings_theme_light_desc
    ThemeMode.DARK -> R.string.settings_theme_dark_desc
}

private fun ThemeMode.icon(): ImageVector = when (this) {
    ThemeMode.SYSTEM -> Icons.Rounded.BrightnessAuto
    ThemeMode.LIGHT -> Icons.Rounded.LightMode
    ThemeMode.DARK -> Icons.Rounded.DarkMode
}

private fun MascotType.labelRes(): Int = when (this) {
    MascotType.FOX -> R.string.mascot_fox
    MascotType.CHAMELEON -> R.string.mascot_chameleon
    MascotType.DOG -> R.string.mascot_dog
}

private fun MascotType.taglineRes(): Int = when (this) {
    MascotType.FOX -> R.string.mascot_fox_tagline
    MascotType.CHAMELEON -> R.string.mascot_chameleon_tagline
    MascotType.DOG -> R.string.mascot_dog_tagline
}
