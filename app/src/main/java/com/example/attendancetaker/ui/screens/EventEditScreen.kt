package com.example.attendancetaker.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.attendancetaker.R
import com.example.attendancetaker.data.AttendanceRepository
import com.example.attendancetaker.data.ContactGroup
import com.example.attendancetaker.data.Event
import com.example.attendancetaker.data.RecurringEvent
import com.example.attendancetaker.ui.theme.ButtonBlue
import com.example.attendancetaker.ui.theme.ButtonNeutral
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditScreen(
    eventId: String?,
    repository: AttendanceRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var event by remember { mutableStateOf<Event?>(null) }
    var eventName by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf(LocalDate.now()) }
    var eventTime by remember { mutableStateOf(LocalTime.now()) }
    var selectedGroupIds by remember { mutableStateOf(emptySet<String>()) }
    var searchQuery by remember { mutableStateOf("") }
    var showSaveConfirmation by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Recurring event state
    var isRecurring by remember { mutableStateOf(false) }
    var recurringEndDate by remember { mutableStateOf<LocalDate?>(null) }
    var showRecurringEndDatePicker by remember { mutableStateOf(false) }
    var hasEndDate by remember { mutableStateOf(false) }

    val contactGroups by repository.getAllContactGroups().collectAsState(initial = emptyList())
    var contactsForGroups by remember { mutableStateOf(mapOf<String, List<com.example.attendancetaker.data.Contact>>()) }

    // Load event data if editing
    LaunchedEffect(eventId) {
        if (eventId != null) {
            event = repository.getEventById(eventId)
            event?.let {
                eventName = it.name
                eventDescription = it.description
                eventDate = it.date
                eventTime = it.time
                selectedGroupIds = it.contactGroupIds.toSet()
            }
        }
    }

    // Load contacts for each group
    LaunchedEffect(contactGroups) {
        val contactsMap = mutableMapOf<String, List<com.example.attendancetaker.data.Contact>>()
        contactGroups.forEach { group ->
            contactsMap[group.id] = repository.getContactsFromGroups(listOf(group.id))
        }
        contactsForGroups = contactsMap
    }

    // Filter contact groups based on search query
    val filteredGroups = remember(contactGroups, searchQuery) {
        if (searchQuery.isBlank()) {
            contactGroups
        } else {
            contactGroups.filter { group ->
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
                text = if (event == null) stringResource(R.string.add_event_title) else stringResource(R.string.edit_event_title),
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
                        text = stringResource(R.string.event_details),
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

                    // Date and Time Selection
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
                                value = eventDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.make_recurring_event),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (isRecurring) {

                        // Show which day it will repeat on
                        Text(
                            text = stringResource(R.string.will_repeat_every, eventDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }),
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
                                    if (!it) recurringEndDate = null
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
                                    .clickable { showRecurringEndDatePicker = true }
                            ) {
                                OutlinedTextField(
                                    value = recurringEndDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
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
                    }

                    Text(
                        text = stringResource(R.string.contact_groups_selected, selectedGroupIds.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contact group selection section
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
                        if (contactGroups.isEmpty()) {
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
                                contacts = contactsForGroups[group.id] ?: emptyList(),
                                isSelected = selectedGroupIds.contains(group.id),
                                onSelectionChanged = { isSelected: Boolean ->
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
                eventDate = date
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

    // Recurring End Date Picker
    if (showRecurringEndDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                recurringEndDate = date
                showRecurringEndDatePicker = false
            },
            onDismiss = { showRecurringEndDatePicker = false }
        )
    }

    // Save confirmation dialog
    if (showSaveConfirmation) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmation = false },
            title = { Text(stringResource(R.string.save_event)) },
            text = {
                Text(stringResource(R.string.save_event_question))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            if (isRecurring) {
                                // Create a recurring event
                                val recurringEvent = RecurringEvent(
                                    name = eventName.trim(),
                                    description = eventDescription.trim(),
                                    time = eventTime,
                                    dayOfWeek = eventDate.dayOfWeek,
                                    contactGroupIds = selectedGroupIds.toList(),
                                    startDate = eventDate,
                                    endDate = if (hasEndDate) recurringEndDate else null,
                                    isActive = true
                                )
                                repository.addRecurringEvent(recurringEvent)
                            } else {
                                // Save as regular event
                                val updatedEvent = if (event == null) {
                                    Event(
                                        name = eventName.trim(),
                                        description = eventDescription.trim(),
                                        date = eventDate,
                                        time = eventTime,
                                        contactGroupIds = selectedGroupIds.toList()
                                    )
                                } else {
                                    event!!.copy(
                                        name = eventName.trim(),
                                        description = eventDescription.trim(),
                                        date = eventDate,
                                        time = eventTime,
                                        contactGroupIds = selectedGroupIds.toList()
                                    )
                                }

                                if (event == null) {
                                    repository.addEvent(updatedEvent)
                                } else {
                                    repository.updateEvent(updatedEvent)
                                }
                            }

                            showSaveConfirmation = false
                            onNavigateBack()
                        }
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

@Composable
fun ContactGroupSelectionItem(
    group: ContactGroup,
    contacts: List<com.example.attendancetaker.data.Contact>,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectionChanged(!isSelected) },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 3.dp else 1.dp
        ),
        border = if (isSelected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                if (group.description.isNotEmpty()) {
                    Text(
                        text = group.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${contacts.size} members",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(date)
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = ButtonBlue)
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = ButtonNeutral)
            ) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = LocalTime.now().hour,
        initialMinute = LocalTime.now().minute
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    onTimeSelected(time)
                },
                colors = ButtonDefaults.textButtonColors(contentColor = ButtonBlue)
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = ButtonNeutral)
            ) {
                Text("Cancel")
            }
        }
    )
}