package com.example.attendancetaker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.attendancetaker.data.AttendanceRepository
import com.example.attendancetaker.data.ContactGroup
import com.example.attendancetaker.data.Event
import com.example.attendancetaker.ui.theme.ButtonBlue
import com.example.attendancetaker.ui.theme.ButtonRed
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditScreen(
    event: Event?,
    repository: AttendanceRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var eventName by remember { mutableStateOf(event?.name ?: "") }
    var eventDescription by remember { mutableStateOf(event?.description ?: "") }
    var eventDate by remember { mutableStateOf(event?.date ?: LocalDate.now()) }
    var eventTime by remember { mutableStateOf(event?.time ?: LocalTime.now()) }
    var selectedGroupIds by remember { mutableStateOf(event?.contactGroupIds?.toSet() ?: emptySet()) }
    var searchQuery by remember { mutableStateOf("") }
    var showSaveConfirmation by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = if (event == null) "Add Event" else "Edit Event",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
            )

            Button(
                onClick = { showSaveConfirmation = true },
                enabled = eventName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonBlue)
            ) {
                Text("Save")
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
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Event Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedTextField(
                        value = eventName,
                        onValueChange = { eventName = it },
                        label = { Text("Event Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = eventDescription,
                        onValueChange = { eventDescription = it },
                        label = { Text("Description (Optional)") },
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
                                label = { Text("Date") },
                                readOnly = true,
                                enabled = false,
                                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
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
                                label = { Text("Time") },
                                readOnly = true,
                                enabled = false,
                                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
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

                    Text(
                        text = "${selectedGroupIds.size} contact groups selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contact group selection section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Select Contact Groups",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search contact groups...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (filteredGroups.isEmpty()) {
                        if (repository.contactGroups.isEmpty()) {
                            Text(
                                text = "No contact groups available. Create contact groups first.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "No contact groups match your search.",
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

    // Save confirmation dialog
    if (showSaveConfirmation) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmation = false },
            title = { Text("Save Event") },
            text = {
                Text("Do you want to save this event?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Save the event
                        val updatedEvent = if (event == null) {
                            Event(
                                name = eventName.trim(),
                                description = eventDescription.trim(),
                                date = eventDate,
                                time = eventTime,
                                contactGroupIds = selectedGroupIds.toList()
                            )
                        } else {
                            event.copy(
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

                        showSaveConfirmation = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ButtonBlue)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSaveConfirmation = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = ButtonRed)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ContactGroupSelectionItem(
    group: ContactGroup,
    repository: AttendanceRepository,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    val contacts = repository.getContactsFromGroups(listOf(group.id))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChanged
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
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
                    color = MaterialTheme.colorScheme.primary
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
                colors = ButtonDefaults.textButtonColors(contentColor = ButtonRed)
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
                colors = ButtonDefaults.textButtonColors(contentColor = ButtonRed)
            ) {
                Text("Cancel")
            }
        }
    )
}