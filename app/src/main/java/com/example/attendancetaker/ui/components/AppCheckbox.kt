package com.example.attendancetaker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * A reusable checkbox component with label that follows Material Design 3 principles.
 *
 * @param checked Whether the checkbox is checked
 * @param onCheckedChange Callback when the checkbox state changes
 * @param label The text label for the checkbox
 * @param modifier Modifier to be applied to the component
 * @param enabled Whether the checkbox is enabled
 * @param labelColor The color of the label text
 * @param checkboxColor The color of the checkbox when checked
 * @param uncheckedColor The color of the checkbox when unchecked
 * @param labelMaxLines Maximum number of lines for the label text
 * @param labelOverflow How to handle text overflow in the label
 */
@Composable
fun AppCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    checkboxColor: Color = MaterialTheme.colorScheme.primary,
    uncheckedColor: Color = MaterialTheme.colorScheme.outline,
    labelMaxLines: Int = Int.MAX_VALUE,
    labelOverflow: TextOverflow = TextOverflow.Ellipsis
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = CheckboxDefaults.colors(
                checkedColor = checkboxColor,
                uncheckedColor = uncheckedColor,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                disabledCheckedColor = checkboxColor.copy(alpha = 0.38f),
                disabledUncheckedColor = uncheckedColor.copy(alpha = 0.38f),
                disabledIndeterminateColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            ),
            modifier = Modifier.padding(end = 12.dp)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) labelColor else labelColor.copy(alpha = 0.38f),
            maxLines = labelMaxLines,
            overflow = labelOverflow,
            modifier = Modifier.weight(1f)
        )
    }
}