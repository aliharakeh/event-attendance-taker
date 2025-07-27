package com.example.attendancetaker.screens.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
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
import com.example.attendancetaker.ui.components.AppToolbar
import com.example.attendancetaker.ui.components.AppList
import com.example.attendancetaker.ui.components.AppListItem
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun RecurringTemplatesScreen(
    repository: AttendanceRepository,
    onNavigateBack: () -> Unit,
    onNavigateToEventEdit: (Event?) -> Unit,
    modifier: Modifier = Modifier
) {
    val recurringEvents by repository.getRecurringEvents().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header with AppToolbar
        AppToolbar(
            title = stringResource(R.string.recurring_templates),
            onNavigationClick = onNavigateBack
        )

        // AppList with search and template items
        AppList(
            items = recurringEvents,
            onItemToListItem = { template ->
                AppListItem(
                    id = template.id,
                    title = template.name,
                    subtitle = if (template.description.isNotBlank()) template.description else null,
                    content = {
                        RecurringTemplateContent(
                            template = template,
                            repository = repository
                        )
                    }
                )
            },
            searchPlaceholder = stringResource(R.string.search_templates),
            showSearch = true,
            isEditable = true,
            isDeletable = true,
            onEdit = { template -> onNavigateToEventEdit(template) },
            onDelete = { template ->
                coroutineScope.launch {
                    repository.removeEvent(template.id)
                }
            },
            deleteConfirmationTitle = stringResource(R.string.delete_template),
            deleteConfirmationMessage = { templateName ->
                stringResource(R.string.delete_template_confirmation, templateName)
            },
            emptyStateMessage = stringResource(R.string.no_recurring_templates),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun RecurringTemplateContent(
    template: Event,
    repository: AttendanceRepository
) {
    var selectedGroups by remember { mutableStateOf(emptyList<ContactGroup>()) }
    var totalContacts by remember { mutableStateOf(0) }

    // Load related data
    LaunchedEffect(template.contactGroupIds) {
        selectedGroups = template.contactGroupIds.mapNotNull { groupId ->
            repository.getContactGroup(groupId)
        }
        totalContacts = repository.getContactsForEvent(template.id).size
    }

    Column {
        // Recurring icon row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                Icons.Default.Repeat,
                contentDescription = stringResource(R.string.cd_recurring_event),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Recurring Template",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Show recurring pattern info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(
                    R.string.recurring_template_pattern,
                    template.dayOfWeek?.getDisplayName(
                        TextStyle.FULL,
                        Locale.getDefault()
                    ) ?: "Unknown",
                    template.time.format(DateTimeFormatter.ofPattern("HH:mm"))
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Date range info
        if (template.startDate != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (template.endDate != null) {
                        stringResource(
                            R.string.recurring_date_range,
                            template.startDate!!.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                            template.endDate!!.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                        )
                    } else {
                        stringResource(
                            R.string.recurring_from_date,
                            template.startDate!!.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                        )
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Contact Groups Display
        if (selectedGroups.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically
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
    }
}