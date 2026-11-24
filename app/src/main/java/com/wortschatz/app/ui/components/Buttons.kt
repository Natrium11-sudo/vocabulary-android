package com.wortschatz.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.wortschatz.app.ui.theme.DuoTheme

/**
 * Primary CTA. The solid "3D" base is drawn underneath and the face moves down when
 * pressed, mimicking Duolingo's tactile buttons while remaining a plain accessible button
 * with a Role.Button and the usual text semantics.
 */
@Composable
fun DuoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: DuoButtonStyle = DuoButtonStyle.Primary,
    leadingIcon: ImageVector? = null,
) {
    val colors = DuoTheme.colors
    val skin = when (style) {
        DuoButtonStyle.Primary -> ButtonSkin(
            container = if (enabled) colors.green else colors.disabled,
            shadow = if (enabled) colors.greenDark else colors.shadow,
            content = if (enabled) colors.greenSolidInk else colors.disabledText,
        )
        DuoButtonStyle.Secondary -> ButtonSkin(
            container = if (enabled) colors.surface else colors.disabled,
            shadow = colors.shadow,
            content = if (enabled) colors.textPrimary else colors.disabledText,
            border = if (enabled) colors.outline else colors.disabled,
        )
        DuoButtonStyle.Gold -> ButtonSkin(
            container = if (enabled) colors.gold else colors.disabled,
            shadow = if (enabled) colors.streakDeep.copy(alpha = 0.35f) else colors.shadow,
            content = if (enabled) colors.goldSolidInk else colors.disabledText,
        )
    }

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressOffset by animateDpAsState(
        targetValue = if (pressed && enabled) 3.dp else 0.dp,
        label = "duoButtonPress",
    )
    val shape = RoundedCornerShape(16.dp)

    Box(modifier = modifier.fillMaxWidth().padding(bottom = 4.dp)) {
        // Solid bottom layer = the button's "shadow".
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 4.dp)
                .clip(shape)
                .background(skin.shadow),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, pressOffset.roundToPx()) }
                .heightIn(min = 56.dp)
                .clip(shape)
                .background(skin.container)
                .then(
                    if (skin.border != null) {
                        Modifier.border(2.dp, skin.border, shape)
                    } else {
                        Modifier
                    },
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = skin.content,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = skin.content,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private data class ButtonSkin(
    val container: Color,
    val shadow: Color,
    val content: Color,
    val border: Color? = null,
)

enum class DuoButtonStyle { Primary, Secondary, Gold }

/** 48dp circular icon action with a soft surface background. */
@Composable
fun DuoIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = DuoTheme.colors.textPrimary,
    container: Color = DuoTheme.colors.surfaceMuted,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(container)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
    }
}

/** Small text-only action, e.g. secondary paths in dialogs. */
@Composable
fun DuoTextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = DuoTheme.colors
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = if (enabled) colors.greenInk else colors.disabledText,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
    )
}

