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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.attendancetaker.data.repository.AttendanceRepository
import com.example.attendancetaker.data.entity.Contact
import com.example.attendancetaker.data.entity.Event
import com.example.attendancetaker.screens.CheckboxRow
import com.example.attendancetaker.screens.DatePickerDialog
import com.example.attendancetaker.screens.SelectedContactGroupItem
import com.example.attendancetaker.screens.TimePickerDialog
import com.example.attendancetaker.ui.components.ActionPresets
import com.example.attendancetaker.ui.components.AppActionRow
import com.example.attendancetaker.ui.components.AppCard
import com.example.attendancetaker.ui.components.AppTextField
import com.example.attendancetaker.ui.components.AppToolbar
import com.example.attendancetaker.ui.components.ToolbarActionPresets
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
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Recurring event state
    var isRecurring by remember { mutableStateOf(false) }
    var recurringEndDate by remember { mutableStateOf<LocalDate?>(null) }
    var showRecurringEndDatePicker by remember { mutableStateOf(false) }
    var hasEndDate by remember { mutableStateOf(false) }

    val contactGroups by repository.getAllContactGroups().collectAsState(initial = emptyList())
    var contactsForGroups by remember { mutableStateOf(mapOf<String, List<Contact>>()) }

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
        val contactsMap = mutableMapOf<String, List<Contact>>()
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
        // App Toolbar
        AppToolbar(
            title = if (event == null) stringResource(R.string.add_event_title) else stringResource(R.string.edit_event_title),
            onNavigationClick = onNavigateBack,
            actions = listOf(
                ToolbarActionPresets.saveAction(
                    onClick = {
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

                            onNavigateBack()
                        }
                    },
                    enabled = eventName.isNotBlank()
                )
            )
        )

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Event details section
            AppCard(
                title = stringResource(R.string.event_details),
                content = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppTextField(
                            value = eventName,
                            onValueChange = { eventName = it },
                            label = stringResource(R.string.event_name),
                            modifier = Modifier.fillMaxWidth()
                        )

                        AppTextField(
                            value = eventDescription,
                            onValueChange = { eventDescription = it },
                            label = stringResource(R.string.description_optional),
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            singleLine = false
                        )

                        // Date and Time Selection Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Date selection
                            AppTextField(
                                value = eventDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                onValueChange = { },
                                label = stringResource(R.string.date),
                                readOnly = true,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showDatePicker = true }
                            )

                            // Time selection
                            AppTextField(
                                value = eventTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                                onValueChange = { },
                                label = stringResource(R.string.time),
                                readOnly = true,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showTimePicker = true }
                            )
                        }

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

                                // End date option
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
                                    AppTextField(
                                        value = recurringEndDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) ?: "",
                                        onValueChange = { },
                                        label = stringResource(R.string.end_date),
                                        readOnly = true,
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.CalendarToday,
                                                contentDescription = null
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showRecurringEndDatePicker = true }
                                    )
                                }
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contact groups selection section
            AppCard(
                title = stringResource(R.string.select_contact_groups),
                actions = listOf(
                    ActionPresets.addAction(
                        onClick = { onNavigateToContactGroupSelection(eventId) }
                    )
                ),
                content = {
                    Column {
                        Text(
                            text = stringResource(R.string.contact_groups_selected, selectedGroupIds.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (selectedContactGroups.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))

                            selectedContactGroups.forEach { group ->
                                SelectedContactGroupItem(
                                    group = group,
                                    contacts = contactsForGroups[group.id] ?: emptyList(),
                                    onRemove = { selectedGroupIds = selectedGroupIds - group.id }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.no_contact_groups_selected_for_event),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
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


}