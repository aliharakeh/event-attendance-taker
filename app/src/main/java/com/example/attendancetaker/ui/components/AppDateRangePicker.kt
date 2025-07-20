package com.example.attendancetaker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    onStartDateClick: (() -> Unit)? = null,
    onEndDateClick: (() -> Unit)? = null
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Start Date Field
        OutlinedTextField(
            value = startDate?.format(dateFormatter) ?: "",
            onValueChange = { /* Read-only field */ },
            label = { Text(startDatePlaceholder) },
            placeholder = { Text(startDatePlaceholder) },
            readOnly = true,
            enabled = enabled,
            trailingIcon = {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Select $startDatePlaceholder",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = enabled) {
                    onStartDateClick?.invoke()
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

        // End Date Field
        OutlinedTextField(
            value = endDate?.format(dateFormatter) ?: "",
            onValueChange = { /* Read-only field */ },
            label = { Text(endDatePlaceholder) },
            placeholder = { Text(endDatePlaceholder) },
            readOnly = true,
            enabled = enabled,
            trailingIcon = {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Select $endDatePlaceholder",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = enabled) {
                    onEndDateClick?.invoke()
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
}