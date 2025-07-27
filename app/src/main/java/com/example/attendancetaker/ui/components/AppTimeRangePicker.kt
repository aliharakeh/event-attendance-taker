package com.example.attendancetaker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun AppTimeRangePicker(
    startTime: LocalTime?,
    endTime: LocalTime?,
    onStartTimeChange: (LocalTime?) -> Unit,
    onEndTimeChange: (LocalTime?) -> Unit,
    modifier: Modifier = Modifier,
    startTimePlaceholder: String = "Start Time",
    endTimePlaceholder: String = "End Time",
    enabled: Boolean = true,
    // Appearance configuration
    buttonStyle: AppIconButtonStyle = AppIconButtonStyle.ROUNDED_ICON_TEXT,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    fontSize: TextUnit = 14.sp,
    horizontalPadding: Dp = 12.dp,
    verticalPadding: Dp = 8.dp,
    cornerRadius: Dp = 12.dp,
    iconSize: Dp = 20.dp,
    iconTextSpacing: Dp = 6.dp,
    separatorText: String = "—",
    separatorColor: Color? = null,
    separatorPadding: Dp = 4.dp
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Start Time Button
        AppIconButton(
            style = buttonStyle,
            onClick = { if (enabled) showStartTimePicker = true },
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Schedule,
            text = startTime?.format(timeFormatter) ?: startTimePlaceholder,
            enabled = enabled,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            fontSize = fontSize,
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding,
            cornerRadius = cornerRadius,
            iconSize = iconSize,
            iconTextSpacing = iconTextSpacing,
            contentDescription = "Select $startTimePlaceholder"
        )

        // Separator
        Text(
            text = separatorText,
            style = MaterialTheme.typography.bodyLarge,
            color = separatorColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = separatorPadding)
        )

        // End Time Button
        AppIconButton(
            style = buttonStyle,
            onClick = { if (enabled) showEndTimePicker = true },
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Schedule,
            text = endTime?.format(timeFormatter) ?: endTimePlaceholder,
            enabled = enabled,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            fontSize = fontSize,
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding,
            cornerRadius = cornerRadius,
            iconSize = iconSize,
            iconTextSpacing = iconTextSpacing,
            contentDescription = "Select $endTimePlaceholder"
        )
    }

    // Start Time Picker Dialog
    if (showStartTimePicker) {
        AppTimePickerDialog(
            onTimeSelected = { time ->
                onStartTimeChange(time)
                showStartTimePicker = false
            },
            onDismiss = {
                showStartTimePicker = false
            },
            initialHour = startTime?.hour ?: LocalTime.now().hour,
            initialMinute = startTime?.minute ?: LocalTime.now().minute,
            title = startTimePlaceholder
        )
    }

    // End Time Picker Dialog
    if (showEndTimePicker) {
        AppTimePickerDialog(
            onTimeSelected = { time ->
                onEndTimeChange(time)
                showEndTimePicker = false
            },
            onDismiss = {
                showEndTimePicker = false
            },
            initialHour = endTime?.hour ?: LocalTime.now().hour,
            initialMinute = endTime?.minute ?: LocalTime.now().minute,
            title = endTimePlaceholder
        )
    }
}