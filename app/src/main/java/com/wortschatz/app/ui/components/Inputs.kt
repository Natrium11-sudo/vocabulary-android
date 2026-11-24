package com.wortschatz.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.selection.selectable
import com.wortschatz.app.R
import com.wortschatz.app.ui.theme.DuoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    singleLine: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = DuoTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary.copy(alpha = 0.75f),
            )
        },
        singleLine = singleLine,
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error, style = MaterialTheme.typography.bodySmall) }
        } else {
            null
        },
        leadingIcon = leadingIcon,
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(14.dp),
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.green,
            unfocusedBorderColor = colors.outline,
            focusedLabelColor = colors.greenInk,
            unfocusedLabelColor = colors.textSecondary,
            cursorColor = colors.greenInk,
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface,
            errorContainerColor = colors.surface,
            errorBorderColor = colors.red,
            errorLabelColor = colors.redDeep,
            errorSupportingTextColor = colors.redDeep,
        ),
    )
}

@Composable
fun DuoSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DuoTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(stringResource(R.string.search_hint), style = MaterialTheme.typography.bodyMedium)
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        textStyle = MaterialTheme.typography.bodyLarge,
        leadingIcon = {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = colors.textSecondary)
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.action_clear_search),
                        tint = colors.textSecondary,
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.green,
            unfocusedBorderColor = colors.outline,
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface,
            cursorColor = colors.greenInk,
        ),
    )
}

/** Selectable pill used for language filters and session sizes. */
@Composable
fun DuoFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    val colors = DuoTheme.colors
    val shape = RoundedCornerShape(50)
    Surface(
        modifier = modifier
            .heightIn(min = 42.dp)
            .minimumInteractiveComponentSize()
            .selectable(selected = selected, enabled = enabled, onClick = onClick),
        shape = shape,
        color = when {
            selected -> colors.greenSoft
            else -> colors.surface
        },
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = when {
                selected -> colors.green
                enabled -> colors.outline
                else -> colors.disabled
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leading?.invoke()
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    selected -> colors.greenInk
                    enabled -> colors.textPrimary
                    else -> colors.disabledText
                },
            )
        }
    }
}
