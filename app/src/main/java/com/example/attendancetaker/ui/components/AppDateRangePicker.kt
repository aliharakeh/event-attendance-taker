package com.example.attendancetaker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AppDateRangePicker(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onStartDateChange: (LocalDate?) -> Unit,
    onEndDateChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    startDatePlaceholder: String = "Start Date",
    endDatePlaceholder: String = "End Date",
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
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Start Date Button
        AppIconButton(
            style = buttonStyle,
            onClick = { if (enabled) showStartDatePicker = true },
            modifier = Modifier.weight(1f),
            icon = Icons.Default.DateRange,
            text = startDate?.format(dateFormatter) ?: startDatePlaceholder,
            enabled = enabled,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            fontSize = fontSize,
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding,
            cornerRadius = cornerRadius,
            iconSize = iconSize,
            iconTextSpacing = iconTextSpacing,
            contentDescription = "Select $startDatePlaceholder"
        )

        // Separator
        Text(
            text = separatorText,
            style = MaterialTheme.typography.bodyLarge,
            color = separatorColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = separatorPadding)
        )

        // End Date Button
        AppIconButton(
            style = buttonStyle,
            onClick = { if (enabled) showEndDatePicker = true },
            modifier = Modifier.weight(1f),
            icon = Icons.Default.DateRange,
            text = endDate?.format(dateFormatter) ?: endDatePlaceholder,
            enabled = enabled,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            fontSize = fontSize,
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding,
            cornerRadius = cornerRadius,
            iconSize = iconSize,
            iconTextSpacing = iconTextSpacing,
            contentDescription = "Select $endDatePlaceholder"
        )
    }

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        AppDatePickerDialog(
            onDateSelected = { date ->
                onStartDateChange(date)
                showStartDatePicker = false
            },
            onDismiss = {
                showStartDatePicker = false
            },
            initialDateMillis = startDate?.atStartOfDay(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
                ?: System.currentTimeMillis()
        )
    }

    // End Date Picker Dialog
    if (showEndDatePicker) {
        AppDatePickerDialog(
            onDateSelected = { date ->
                onEndDateChange(date)
                showEndDatePicker = false
            },
            onDismiss = {
                showEndDatePicker = false
            },
            initialDateMillis = endDate?.atStartOfDay(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
                ?: System.currentTimeMillis()
        )
    }
}