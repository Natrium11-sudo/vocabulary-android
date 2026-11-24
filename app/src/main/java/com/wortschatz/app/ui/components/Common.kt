package com.wortschatz.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wortschatz.app.R
import androidx.compose.ui.res.stringResource
import com.wortschatz.app.domain.model.categoryDisplayName
import com.wortschatz.app.ui.LocalAppLanguage
import com.wortschatz.app.ui.theme.DuoTheme

fun isWideScreen(widthDp: Int): Boolean = widthDp >= 600

@Composable
fun rememberIsWideLayout(): Boolean {
    val configuration = LocalConfiguration.current
    return isWideScreen(configuration.screenWidthDp)
}

private fun flagFor(language: String): String = when (language.trim().lowercase()) {
    "französisch", "franzosisch", "french" -> "🇫🇷"
    "spanisch", "spanish" -> "🇪🇸"
    "englisch", "english" -> "🇬🇧"
    "italienisch", "italian" -> "🇮🇹"
    "deutsch", "german" -> "🇩🇪"
    "portugiesisch", "portuguese" -> "🇵🇹"
    "niederländisch", "niederlandisch", "dutch" -> "🇳🇱"
    "türkisch", "turkisch", "turkish" -> "🇹🇷"
    "griechisch", "greek" -> "🇬🇷"
    "latein", "latin" -> "🏛️"
    "japanisch", "japanese" -> "🇯🇵"
    "chinesisch", "chinese" -> "🇨🇳"
    "koreanisch", "korean" -> "🇰🇷"
    "russisch", "russian" -> "🇷🇺"
    "arabisch", "arabic" -> "🇸🇦"
    "schwedisch", "swedish" -> "🇸🇪"
    "polnisch", "polish" -> "🇵🇱"
    else -> "📚"
}

@Composable
fun LanguageFlag(
    language: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
) {
    Text(
        text = flagFor(language),
        fontSize = fontSize,
        modifier = modifier.clearAndSetSemantics { },
    )
}

@Composable
fun CategoryPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    val display = if (text.isBlank()) {
        stringResource(R.string.category_fallback)
    } else {
        categoryDisplayName(text, LocalAppLanguage.current)
    }
    Box(
        modifier = modifier
            .background(DuoTheme.colors.surfaceMuted, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = display,
            style = MaterialTheme.typography.labelSmall,
            color = DuoTheme.colors.textSecondary,
        )
    }
}

@Composable
fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Mascot(
            modifier = Modifier.size(132.dp),
            contentDescription = stringResource(R.string.cd_mascot),
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = DuoTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = DuoTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(24.dp))
            DuoButton(
                text = actionLabel,
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun StatTile(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    DuoCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(tint.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(19.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = DuoTheme.colors.textPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = DuoTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
fun IconBadge(
    icon: ImageVector,
    tint: Color,
    background: Color,
    modifier: Modifier = Modifier,
    size: Int = 44,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size((size * 0.5f).dp),
        )
    }
}

@Composable
fun LabelValueRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = DuoTheme.colors.textSecondary)
        Text(value, style = MaterialTheme.typography.titleSmall, color = DuoTheme.colors.textPrimary)
    }
}
