package com.example.attendancetaker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.attendancetaker.data.AttendanceRepository
import com.example.attendancetaker.data.RecurringEvent
import com.example.attendancetaker.ui.theme.ButtonBlue
import com.example.attendancetaker.ui.theme.ButtonNeutral
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.res.stringResource
import com.example.attendancetaker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringEventEditScreen(
    recurringEvent: RecurringEvent?,
    repository: AttendanceRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var eventName by remember { mutableStateOf(recurringEvent?.name ?: "") }
    var eventDescription by remember { mutableStateOf(recurringEvent?.description ?: "") }
    var eventTime by remember { mutableStateOf(recurringEvent?.time ?: LocalTime.now()) }
    var selectedDate by remember {
        mutableStateOf(
            // If editing, find next occurrence of the day, otherwise use today
            if (recurringEvent != null) {
                val today = LocalDate.now()
                val dayOfWeek = recurringEvent.dayOfWeek
                val daysUntilNext = (dayOfWeek.value - today.dayOfWeek.value + 7) % 7
                if (daysUntilNext == 0) today else today.plusDays(daysUntilNext.toLong())
            } else {
                LocalDate.now()
            }
        )
    }
    var selectedGroupIds by remember {
        mutableStateOf(
            recurringEvent?.contactGroupIds?.toSet() ?: emptySet()
        )
    }
    var searchQuery by remember { mutableStateOf("") }
    var showSaveConfirmation by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var hasEndDate by remember { mutableStateOf(recurringEvent?.endDate != null) }
    var endDate by remember { mutableStateOf(recurringEvent?.endDate) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var isActive by remember { mutableStateOf(recurringEvent?.isActive ?: true) }

    // Filter contact groups based on search query
    val filteredGroups = remember(repository.contactGroups, searchQuery) {
        if (searchQuery.isBlank()) {
            repository.contactGroups
        } else {
            repository.contactGroups.filter { group ->
                group.name.contains(searchQuery, ignoreCase = true) ||
                        group.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header with back button and save button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }

            Text(
                text = if (recurringEvent == null) stringResource(R.string.add_recurring_event) else stringResource(R.string.edit_recurring_event),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )

            Button(
                onClick = { showSaveConfirmation = true },
                enabled = eventName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonBlue)
            ) {
                Text(stringResource(R.string.save))
            }
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Event details section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.recurring_event_details),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedTextField(
                        value = eventName,
                        onValueChange = { eventName = it },
                        label = { Text(stringResource(R.string.event_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = eventDescription,
                        onValueChange = { eventDescription = it },
                        label = { Text(stringResource(R.string.description_optional)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    // Date and time selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Date selection
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDatePicker = true }
                        ) {
                            OutlinedTextField(
                                value = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                onValueChange = { },
                                label = { Text(stringResource(R.string.date)) },
                                readOnly = true,
                                enabled = false,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        // Time selection
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showTimePicker = true }
                        ) {
                            OutlinedTextField(
                                value = eventTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                                onValueChange = { },
                                label = { Text(stringResource(R.string.time)) },
                                readOnly = true,
                                enabled = false,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }

                    // Show which day it will repeat on
                    Text(
                        text = stringResource(R.string.will_repeat_every, selectedDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // End date option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = hasEndDate,
                            onCheckedChange = {
                                hasEndDate = it
                                if (!it) endDate = null
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.set_end_date),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (hasEndDate) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showEndDatePicker = true }
                        ) {
                            OutlinedTextField(
                                value = endDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                                    ?: stringResource(R.string.select_end_date),
                                onValueChange = { },
                                label = { Text(stringResource(R.string.end_date)) },
                                readOnly = true,
                                enabled = false,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }

                    // Active status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isActive,
                            onCheckedChange = { isActive = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.active_status),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Text(
                        text = stringResource(R.string.contact_groups_selected, selectedGroupIds.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contact group selection section (same as EventEditScreen)
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.select_contact_groups),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text(stringResource(R.string.search_contact_groups)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (filteredGroups.isEmpty()) {
                        if (repository.contactGroups.isEmpty()) {
                            Text(
                                text = stringResource(R.string.no_contact_groups_available),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.no_contact_groups_match_search),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Contact groups list
                        filteredGroups.forEach { group ->
                            ContactGroupSelectionItem(
                                group = group,
                                repository = repository,
                                isSelected = selectedGroupIds.contains(group.id),
                                onSelectionChanged = { isSelected ->
                                    selectedGroupIds = if (isSelected) {
                                        selectedGroupIds + group.id
                                    } else {
                                        selectedGroupIds - group.id
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // Add bottom padding to prevent content being cut off
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Date Picker
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // Time Picker
    if (showTimePicker) {
        TimePickerDialog(
            onTimeSelected = { time ->
                eventTime = time
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    // End Date Picker
    if (showEndDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                endDate = date
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }

    // Save confirmation dialog
    if (showSaveConfirmation) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmation = false },
            title = { Text(stringResource(R.string.save_recurring_event)) },
            text = {
                Text(stringResource(R.string.save_recurring_event_question))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Save the recurring event
                        val updatedRecurringEvent = if (recurringEvent == null) {
                            RecurringEvent(
                                name = eventName.trim(),
                                description = eventDescription.trim(),
                                time = eventTime,
                                dayOfWeek = selectedDate.dayOfWeek,
                                contactGroupIds = selectedGroupIds.toList(),
                                startDate = selectedDate,
                                endDate = if (hasEndDate) endDate else null,
                                isActive = isActive
                            )
                        } else {
                            recurringEvent.copy(
                                name = eventName.trim(),
                                description = eventDescription.trim(),
                                time = eventTime,
                                dayOfWeek = selectedDate.dayOfWeek,
                                contactGroupIds = selectedGroupIds.toList(),
                                endDate = if (hasEndDate) endDate else null,
                                isActive = isActive
                            )
                        }

                        if (recurringEvent == null) {
                            repository.addRecurringEvent(updatedRecurringEvent)
                        } else {
                            repository.updateRecurringEvent(updatedRecurringEvent)
                        }

                        showSaveConfirmation = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ButtonBlue)
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSaveConfirmation = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = ButtonNeutral)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}