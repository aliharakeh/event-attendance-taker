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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.attendancetaker.R
import com.example.attendancetaker.data.entity.Contact
import com.example.attendancetaker.data.entity.ContactGroup
import com.example.attendancetaker.data.entity.Event
import com.example.attendancetaker.data.repository.AttendanceRepository
import com.example.attendancetaker.screens.CheckboxRow
import com.example.attendancetaker.screens.DatePickerDialog
import com.example.attendancetaker.screens.TimePickerDialog
import com.example.attendancetaker.ui.components.ActionPresets
import com.example.attendancetaker.ui.components.AppCard
import com.example.attendancetaker.ui.components.AppList
import com.example.attendancetaker.ui.components.AppListItem
import com.example.attendancetaker.ui.components.AppTextField
import com.example.attendancetaker.ui.components.AppToolbar
import com.example.attendancetaker.ui.components.ToolbarActionPresets
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditScreen(
    eventId: String?,
    repository: AttendanceRepository,
    eventState: EventState,
    onNavigateBack: () -> Unit,
    onNavigateToContactGroupSelection: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val contactGroups by repository.getAllContactGroups().collectAsState(initial = emptyList())
    var contactsForGroups by remember { mutableStateOf(mapOf<String, List<Contact>>()) }
    var event: Event? by remember { mutableStateOf(null) }

    // Load event data
    LaunchedEffect(eventId) {
        if (eventId != null) {
            event = repository.getEventById(eventId)
            event?.let {
                eventState.initializeFromEvent(it)
            }
        }
    }

    // Ensure selected groups are loaded once the contact groups list is available
    LaunchedEffect(event, contactGroups) {
        if (event != null && contactGroups.isNotEmpty() && eventState.selectedGroupIds.isEmpty()) {
            event?.let { evt ->
                val eventGroups = contactGroups.filter { it.id in evt.contactGroupIds }
                eventState.updateSelectedGroups(eventGroups)
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

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // App Toolbar
        AppToolbar(
            title = if (eventId == null) stringResource(R.string.add_event_title) else stringResource(
                R.string.edit_event_title
            ),
            onNavigationClick = onNavigateBack,
            actions = listOf(
                ToolbarActionPresets.saveAction(
                    onClick = {
                        coroutineScope.launch {
                            val updatedEvent = if (eventId == null) {
                                Event(
                                    name = eventState.eventName.trim(),
                                    description = eventState.eventDescription.trim(),
                                    date = if (eventState.isRecurring) null else eventState.eventDate,
                                    time = eventState.eventTime,
                                    isRecurring = eventState.isRecurring,
                                    dayOfWeek = if (eventState.isRecurring) eventState.eventDate.dayOfWeek else null,
                                    startDate = if (eventState.isRecurring) eventState.eventDate else null,
                                    endDate = if (eventState.isRecurring && eventState.hasEndDate) eventState.recurringEndDate else null,
                                    isActive = true,
                                    contactGroupIds = eventState.selectedGroupIds.toList()
                                )
                            } else {
                                // Updating an existing event – preserve its id & recurringEventId
                                event!!.copy(
                                    name = eventState.eventName.trim(),
                                    description = eventState.eventDescription.trim(),
                                    date = if (eventState.isRecurring) null else eventState.eventDate,
                                    time = eventState.eventTime,
                                    isRecurring = eventState.isRecurring,
                                    dayOfWeek = if (eventState.isRecurring) eventState.eventDate.dayOfWeek else null,
                                    startDate = if (eventState.isRecurring) eventState.eventDate else null,
                                    endDate = if (eventState.isRecurring && eventState.hasEndDate) eventState.recurringEndDate else null,
                                    isActive = true,
                                    contactGroupIds = eventState.selectedGroupIds.toList()
                                )
                            }

                            if (eventId == null) {
                                repository.addEvent(updatedEvent)
                            } else {
                                repository.updateEvent(updatedEvent)
                            }

                            onNavigateBack()
                        }
                    },
                    enabled = eventState.eventName.isNotBlank()
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
                            value = eventState.eventName,
                            onValueChange = { eventState.eventName = it },
                            label = stringResource(R.string.event_name),
                            modifier = Modifier.fillMaxWidth()
                        )

                        AppTextField(
                            value = eventState.eventDescription,
                            onValueChange = { eventState.eventDescription = it },
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
                                value = eventState.eventDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
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
                                    .clickable { eventState.showDatePicker = true }
                            )

                            // Time selection
                            AppTextField(
                                value = eventState.eventTime.format(DateTimeFormatter.ofPattern("h:mm a")),
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
                                    .clickable { eventState.showTimePicker = true }
                            )
                        }

                        // Recurring event option - only show for new events or recurring templates
                        val shouldShowRecurringSettings = eventId == null || eventState.isRecurring

                        if (shouldShowRecurringSettings) {
                            CheckboxRow(
                                text = stringResource(R.string.make_recurring_event),
                                checked = eventState.isRecurring,
                                onCheckedChange = { eventState.isRecurring = it }
                            )

                            if (eventState.isRecurring) {
                                // Show which day it will repeat on
                                Text(
                                    text = stringResource(
                                        R.string.will_repeat_every,
                                        eventState.eventDate.dayOfWeek.name.lowercase()
                                            .replaceFirstChar { it.uppercase() }),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // End date option
                                CheckboxRow(
                                    text = stringResource(R.string.set_end_date),
                                    checked = eventState.hasEndDate,
                                    onCheckedChange = {
                                        eventState.hasEndDate = it
                                        if (!it) {
                                            eventState.recurringEndDate = null
                                        }
                                    }
                                )

                                if (eventState.hasEndDate) {
                                    // End date selection
                                    AppTextField(
                                        value = eventState.recurringEndDate?.format(
                                            DateTimeFormatter.ofPattern("MMM dd, yyyy")
                                        ) ?: "",
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
                                            .clickable {
                                                eventState.showRecurringEndDatePicker = true
                                            }
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
                subtitle = stringResource(
                    R.string.contact_groups_selected,
                    eventState.selectedGroups.size
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                actions = listOf(
                    ActionPresets.editAction(
                        onClick = { onNavigateToContactGroupSelection(eventId) }
                    )
                ),
                content = {
                    AppList(
                        items = eventState.selectedGroups,
                        onItemToListItem = { group ->
                            val contacts = contactsForGroups[group.id] ?: emptyList()
                            AppListItem(
                                id = group.id,
                                title = group.name,
                                subtitle = if (group.description.isNotEmpty()) {
                                    "${group.description} • ${stringResource(R.string.members_count, contacts.size)}"
                                } else {
                                    stringResource(R.string.members_count, contacts.size)
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        showSearch = true,
                        isDeletable = true,
                        onDelete = { group ->
                            eventState.removeGroup(group.id)
                        },
                        emptyStateMessage = stringResource(R.string.no_contact_groups_selected_for_event),
                    )
                }
            )
        }
    }

    // Date Picker
    if (eventState.showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                eventState.eventDate = date
                eventState.showDatePicker = false
            },
            onDismiss = { eventState.showDatePicker = false }
        )
    }

    // Time Picker
    if (eventState.showTimePicker) {
        TimePickerDialog(
            onTimeSelected = { time ->
                eventState.eventTime = time
                eventState.showTimePicker = false
            },
            onDismiss = { eventState.showTimePicker = false }
        )
    }

    // Recurring End Date Picker
    if (eventState.showRecurringEndDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                eventState.recurringEndDate = date
                eventState.showRecurringEndDatePicker = false
            },
            onDismiss = { eventState.showRecurringEndDatePicker = false }
        )
    }
}

