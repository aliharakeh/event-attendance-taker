package com.example.attendancetaker.screens.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.example.attendancetaker.R
import com.example.attendancetaker.data.AttendanceRepository
import com.example.attendancetaker.data.Event
import com.example.attendancetaker.screens.CheckboxRow
import com.example.attendancetaker.screens.ContactGroupSelectionCard
import com.example.attendancetaker.screens.DatePickerDialog
import com.example.attendancetaker.screens.DateTimeSelectionRow
import com.example.attendancetaker.screens.EventDetailsCard
import com.example.attendancetaker.screens.SaveConfirmationDialog
import com.example.attendancetaker.screens.TimePickerDialog
import com.example.attendancetaker.ui.theme.ButtonBlue
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
    onNavigateToContactGroupSelection: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var event by remember { mutableStateOf<Event?>(null) }
    var eventName by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf(LocalDate.now()) }
    var eventTime by remember { mutableStateOf(LocalTime.now()) }
    var selectedGroupIds by remember { mutableStateOf(emptySet<String>()) }
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
                if (it.isRecurring) {
                    // Editing a recurring event
                    isRecurring = true
                    eventTime = it.time
                    eventDate = it.startDate ?: LocalDate.now()
                    recurringEndDate = it.endDate
                    hasEndDate = it.endDate != null
                } else {
                    // Editing a regular event
                    eventDate = it.date ?: LocalDate.now()
                    eventTime = it.time
                }
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

    // Get selected contact groups for display
    val selectedContactGroups = remember(selectedGroupIds, contactGroups) {
        contactGroups.filter { selectedGroupIds.contains(it.id) }
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
            // Event details section using shared component
            EventDetailsCard(
                title = stringResource(R.string.event_details),
                eventName = eventName,
                onEventNameChange = { eventName = it },
                eventDescription = eventDescription,
                onEventDescriptionChange = { eventDescription = it },
                additionalContent = {
                    // Date and Time Selection using shared component
                    DateTimeSelectionRow(
                        selectedDate = eventDate,
                        onDateClick = { showDatePicker = true },
                        selectedTime = eventTime,
                        onTimeClick = { showTimePicker = true }
                    )

                    // Recurring event option - only show for new events or recurring templates
                    val shouldShowRecurringSettings = event == null || event!!.isRecurring

                    if (shouldShowRecurringSettings) {
                        CheckboxRow(
                            text = stringResource(R.string.make_recurring_event),
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it }
                        )

                        if (isRecurring) {
                            // Show which day it will repeat on
                            Text(
                                text = stringResource(R.string.will_repeat_every, eventDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // End date option using shared component
                            CheckboxRow(
                                text = stringResource(R.string.set_end_date),
                                checked = hasEndDate,
                                onCheckedChange = {
                                    hasEndDate = it
                                    if (!it) {
                                        recurringEndDate = null
                                    }
                                }
                            )

                            if (hasEndDate) {
                                // End date selection
                                OutlinedTextField(
                                    value = recurringEndDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) ?: "",
                                    onValueChange = { },
                                    label = { Text(stringResource(R.string.end_date)) },
                                    readOnly = true,
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = null
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showRecurringEndDatePicker = true },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contact groups selection section using shared component
            ContactGroupSelectionCard(
                selectedGroupIds = selectedGroupIds,
                selectedContactGroups = selectedContactGroups,
                contactsForGroups = contactsForGroups,
                onAddGroupsClick = { onNavigateToContactGroupSelection(eventId) },
                onRemoveGroup = { groupId ->
                    selectedGroupIds = selectedGroupIds - groupId
                }
            )
        }
    }

    // Date Picker using shared component
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                eventDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // Time Picker using shared component
    if (showTimePicker) {
        TimePickerDialog(
            onTimeSelected = { time ->
                eventTime = time
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    // Recurring End Date Picker using shared component
    if (showRecurringEndDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                recurringEndDate = date
                showRecurringEndDatePicker = false
            },
            onDismiss = { showRecurringEndDatePicker = false }
        )
    }

    // Save confirmation dialog using shared component
    SaveConfirmationDialog(
        showDialog = showSaveConfirmation,
        title = stringResource(R.string.save_event),
        message = stringResource(R.string.save_event_question),
        onConfirm = {
            coroutineScope.launch {
                val updatedEvent = if (event == null) {
                    Event(
                        name = eventName.trim(),
                        description = eventDescription.trim(),
                        date = if (isRecurring) null else eventDate,
                        time = eventTime,
                        isRecurring = isRecurring,
                        dayOfWeek = if (isRecurring) eventDate.dayOfWeek else null,
                        startDate = if (isRecurring) eventDate else null,
                        endDate = if (isRecurring && hasEndDate) recurringEndDate else null,
                        isActive = true,
                        contactGroupIds = selectedGroupIds.toList()
                    )
                } else {
                    event!!.copy(
                        name = eventName.trim(),
                        description = eventDescription.trim(),
                        date = if (isRecurring) null else eventDate,
                        time = eventTime,
                        isRecurring = isRecurring,
                        dayOfWeek = if (isRecurring) eventDate.dayOfWeek else null,
                        startDate = if (isRecurring) eventDate else null,
                        endDate = if (isRecurring && hasEndDate) recurringEndDate else null,
                        isActive = true,
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
            }
        },
        onDismiss = { showSaveConfirmation = false }
    )
}