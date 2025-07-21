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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
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
import com.example.attendancetaker.ui.components.AppIconButton
import com.example.attendancetaker.ui.components.AppIconButtonStyle
import com.example.attendancetaker.ui.components.AppList
import com.example.attendancetaker.ui.components.AppListItem
import com.example.attendancetaker.ui.components.AppActionRow
import com.example.attendancetaker.ui.components.AppConfirmDialog
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun EventsScreen(
    repository: AttendanceRepository,
    onNavigateToAttendance: (String) -> Unit,
    onNavigateToEventEdit: (Event?) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToRecurringTemplates: () -> Unit,
    modifier: Modifier = Modifier
) {
    val events by repository.getCurrentAndFutureEvents().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    var eventToDelete by remember { mutableStateOf<Event?>(null) }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        // Navigation Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            AppIconButton(
                style = AppIconButtonStyle.NO_BACKGROUND_ICON_TEXT,
                icon = Icons.Default.Repeat,
                text = stringResource(R.string.templates),
                onClick = onNavigateToRecurringTemplates,
                contentDescription = stringResource(R.string.recurring_templates)
            )
            Spacer(modifier = Modifier.width(8.dp))
            AppIconButton(
                style = AppIconButtonStyle.NO_BACKGROUND_ICON_TEXT,
                icon = Icons.Default.History,
                text = stringResource(R.string.history),
                onClick = onNavigateToHistory,
                contentDescription = stringResource(R.string.event_history)
            )
        }

        // Events List with Search
        AppList(
            items = events,
            onItemToListItem = { event ->
                eventToListItem(
                    event = event,
                    repository = repository,
                    onTakeAttendance = { onNavigateToAttendance(event.id) }
                )
            },
            searchPlaceholder = stringResource(R.string.search_events),
            isEditable = true,
            isDeletable = true,
            onEdit = { event -> onNavigateToEventEdit(event) },
            onDelete = { event -> eventToDelete = event },
            modifier = Modifier.fillMaxSize()
        )
    }

    // Delete Confirmation Dialog
    AppConfirmDialog(
        isVisible = eventToDelete != null,
        title = stringResource(R.string.delete_event),
        message = stringResource(R.string.delete_event_confirmation, eventToDelete?.name ?: ""),
        onConfirm = {
            eventToDelete?.let { event ->
                coroutineScope.launch {
                    repository.removeEvent(event.id)
                }
            }
        },
        onDismiss = { eventToDelete = null },
        confirmButtonText = stringResource(R.string.delete),
        isDestructive = true
    )
}

@Composable
private fun eventToListItem(
    event: Event,
    repository: AttendanceRepository,
    onTakeAttendance: () -> Unit
): AppListItem {
    var selectedGroups by remember { mutableStateOf(emptyList<ContactGroup>()) }
    var totalContacts by remember { mutableStateOf(0) }

    // Load related data
    LaunchedEffect(event.contactGroupIds) {
        selectedGroups = event.contactGroupIds.mapNotNull { groupId ->
            repository.getContactGroup(groupId)
        }
        totalContacts = repository.getContactsForEvent(event.id).size
    }

    return AppListItem(
        id = event.id,
        title = event.name,
        subtitle = if (event.description.isNotBlank()) event.description else null,
        content = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Date and Time Display - different for regular vs recurring events
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    if (event.isRecurring) {
                        // Show day of week and time for recurring events
                        Icon(
                            Icons.Default.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.dayOfWeek?.getDisplayName(
                                TextStyle.FULL,
                                Locale.getDefault()
                            ) ?: "Unknown",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // Show fixed date and time for regular events
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.date?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                                ?: "No date",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Recurring icon indicator
                    if (event.isRecurring || event.isGeneratedFromRecurring) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            Icons.Default.Repeat,
                            contentDescription = stringResource(R.string.cd_recurring_event),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Contact Groups Display
                if (selectedGroups.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
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

                // Take Attendance Button
                Button(
                    onClick = onTakeAttendance,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.People, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.take_attendance))
                }
            }
        }
    )
}

