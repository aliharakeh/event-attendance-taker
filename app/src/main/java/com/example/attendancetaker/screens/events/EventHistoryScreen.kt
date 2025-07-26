package com.example.attendancetaker.screens.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.attendancetaker.R
import com.example.attendancetaker.data.repository.AttendanceRepository
import com.example.attendancetaker.data.entity.ContactGroup
import com.example.attendancetaker.data.entity.Event
import com.example.attendancetaker.ui.components.AppDateRangePicker
import com.example.attendancetaker.ui.components.ActionItem
import com.example.attendancetaker.ui.components.AppCard
import com.example.attendancetaker.ui.components.AppConfirmDialog
import com.example.attendancetaker.ui.components.AppIconButton
import com.example.attendancetaker.ui.components.AppIconButtonStyle
import com.example.attendancetaker.ui.components.AppList
import com.example.attendancetaker.ui.components.AppListItem
import com.example.attendancetaker.ui.components.AppSearchField
import com.example.attendancetaker.ui.components.AppToolbar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch
import com.example.attendancetaker.ui.theme.ButtonRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventHistoryScreen(
    repository: AttendanceRepository,
    onNavigateBack: () -> Unit,
    onNavigateToAttendance: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var fromDate by remember { mutableStateOf<LocalDate?>(null) }
    var toDate by remember { mutableStateOf<LocalDate?>(null) }

    // Get events based on whether date filter is enabled
    val pastEvents by remember(fromDate, toDate) {
        if (fromDate != null && toDate != null) {
            repository.getPastEventsInDateRange(fromDate!!, toDate!!)
        } else {
            repository.getPastEvents()
        }
    }.collectAsState(initial = emptyList())

    // Filter past events based on search query
    val filteredPastEvents = pastEvents.filter { event ->
        if (searchQuery.isBlank()) {
            true
        } else {
            event.name.contains(searchQuery, ignoreCase = true) ||
                    event.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header with back button using AppToolbar
        AppToolbar(
            title = stringResource(R.string.event_history),
            onNavigationClick = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

            // Date Range Filter using AppDateRangePicker
            AppDateRangePicker(
                startDate = fromDate,
                endDate = toDate,
                onStartDateChange = { fromDate = it },
                onEndDateChange = { toDate = it },
                startDatePlaceholder = stringResource(R.string.from_date),
                endDatePlaceholder = stringResource(R.string.to_date),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Events List using AppList
            AppList(
                items = filteredPastEvents,
                onItemToListItem = { event ->
                    AppListItem(
                        id = event.id,
                        title = event.name,
                        subtitle = if (event.description.isNotBlank()) event.description else null,
                        content = {
                            PastEventContent(
                                event = event,
                                repository = repository,
                                onViewAttendance = { onNavigateToAttendance(event.id) }
                            )
                        }
                    )
                },
                showSearch = true, // We already have search above
                emptyStateMessage = when {
                    // No results from search query
                    pastEvents.isNotEmpty() && searchQuery.isNotBlank() ->
                        stringResource(R.string.no_search_results)
                    // No results from date range filter
                    pastEvents.isEmpty() && fromDate != null && toDate != null ->
                        stringResource(R.string.no_events_in_date_range)
                    // No past events at all
                    else -> stringResource(R.string.no_past_events)
                },
                isDeletable = true,
                onDelete = { event ->
                    // Delete will be handled by the confirmation dialog in the card content
                }
            )
        }
    }
}

@Composable
fun PastEventContent(
    event: Event,
    repository: AttendanceRepository,
    onViewAttendance: () -> Unit
) {
    var selectedGroups by remember { mutableStateOf(emptyList<ContactGroup>()) }
    var totalContacts by remember { mutableStateOf(0) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Load related data
    LaunchedEffect(event.contactGroupIds) {
        selectedGroups = event.contactGroupIds.mapNotNull { groupId ->
            repository.getContactGroup(groupId)
        }
        totalContacts = repository.getContactsForEvent(event.id).size
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Event status and metadata
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (event.isRecurring || event.isGeneratedFromRecurring) {
                    Icon(
                        Icons.Default.Repeat,
                        contentDescription = stringResource(R.string.cd_recurring_event),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Past Event Badge
                AppIconButton(
                    style = AppIconButtonStyle.ROUNDED_TEXT_ONLY,
                    text = stringResource(R.string.past_event_badge),
                    onClick = { },
                    enabled = false,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    horizontalPadding = 8.dp,
                    verticalPadding = 4.dp
                )

                // Delete Button
                AppIconButton(
                    style = AppIconButtonStyle.NO_BACKGROUND_ICON_ONLY,
                    icon = Icons.Default.Delete,
                    onClick = { showDeleteConfirmation = true },
                    contentColor = ButtonRed,
                    contentDescription = stringResource(R.string.cd_delete),
                    iconSize = 20.dp
                )
            }
        }

        // Date and Time Display
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (event.isRecurring) {
                // Show day of week and time for recurring events
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Repeat,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = event.dayOfWeek?.getDisplayName(
                            TextStyle.FULL,
                            Locale.getDefault()
                        ) ?: "Unknown",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Show fixed date for regular events
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = event.date?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                            ?: "No date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = event.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Contact Groups Display
        if (selectedGroups.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(
                        R.string.selected_groups_contacts,
                        selectedGroups.joinToString(", ") { it.name },
                        totalContacts
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // View Attendance Button
        Button(
            onClick = onViewAttendance,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.People, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.view_attendance))
        }
    }

    // Delete Confirmation Dialog using AppConfirmDialog
    AppConfirmDialog(
        isVisible = showDeleteConfirmation,
        title = stringResource(R.string.delete_event),
        message = stringResource(R.string.delete_event_confirmation, event.name),
        onConfirm = {
            coroutineScope.launch {
                repository.removeEvent(event.id)
            }
        },
        onDismiss = { showDeleteConfirmation = false },
        isDestructive = true
    )
}