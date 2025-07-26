package com.example.attendancetaker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    enabled: Boolean = true
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Start Time Field
        OutlinedTextField(
            value = startTime?.format(timeFormatter) ?: "",
            onValueChange = { /* Read-only field */ },
            label = { Text(startTimePlaceholder) },
            placeholder = { Text(startTimePlaceholder) },
            readOnly = true,
            enabled = enabled,
            trailingIcon = {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = "Select $startTimePlaceholder",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = enabled) {
                    showStartTimePicker = true
                },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        )

        // Separator
        Text(
            text = "—",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // End Time Field
        OutlinedTextField(
            value = endTime?.format(timeFormatter) ?: "",
            onValueChange = { /* Read-only field */ },
            label = { Text(endTimePlaceholder) },
            placeholder = { Text(endTimePlaceholder) },
            readOnly = true,
            enabled = enabled,
            trailingIcon = {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = "Select $endTimePlaceholder",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = enabled) {
                    showEndTimePicker = true
                },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
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