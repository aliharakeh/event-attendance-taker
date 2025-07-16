package com.example.attendancetaker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.attendancetaker.R
import com.example.attendancetaker.data.AttendanceRecord
import com.example.attendancetaker.data.AttendanceRepository
import com.example.attendancetaker.data.Contact
import com.example.attendancetaker.ui.theme.ButtonBlue
import com.example.attendancetaker.ui.theme.ButtonRed
import com.example.attendancetaker.ui.theme.ButtonNeutral

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    eventId: String,
    repository: AttendanceRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val event = repository.events.find { it.id == eventId }
    var selectedContact by remember { mutableStateOf<Contact?>(null) }

    if (event == null) {
        // Event not found, navigate back
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
        return
    }

    // Get contacts from the event's contact groups (with duplicates filtered)
    val eventContacts = repository.getContactsForEvent(eventId)
    val selectedGroups = event.contactGroupIds.mapNotNull { groupId ->
        repository.getContactGroup(groupId)
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.attendance_tracking),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (selectedGroups.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
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
                            text = "${selectedGroups.joinToString(", ") { it.name }} (${eventContacts.size} contacts)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Show message if no contact groups are selected
        if (selectedGroups.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "No contact groups selected for this event. Please edit the event to add contact groups.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            // Attendance List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(eventContacts) { contact ->
                    val attendanceRecord = repository.getAttendanceRecord(eventId, contact.id)
                    val contactGroups = repository.getGroupsContainingContact(contact.id)
                        .filter { group -> event.contactGroupIds.contains(group.id) }

                    AttendanceItem(
                        contact = contact,
                        contactGroups = contactGroups,
                        attendanceRecord = attendanceRecord,
                        onAttendanceChange = { isPresent ->
                            val record = attendanceRecord?.copy(isPresent = isPresent)
                                ?: AttendanceRecord(
                                    contactId = contact.id,
                                    eventId = eventId,
                                    isPresent = isPresent
                                )
                            repository.updateAttendanceRecord(record)
                        },
                        onEditNotes = { selectedContact = contact }
                    )
                }
            }
        }
    }

    // Notes Dialog
    selectedContact?.let { contact ->
        val attendanceRecord = repository.getAttendanceRecord(eventId, contact.id)
        NotesDialog(
            contact = contact,
            currentNotes = attendanceRecord?.notes ?: "",
            onDismiss = { selectedContact = null },
            onSave = { notes ->
                val record = attendanceRecord?.copy(notes = notes)
                    ?: AttendanceRecord(
                        contactId = contact.id,
                        eventId = eventId,
                        notes = notes
                    )
                repository.updateAttendanceRecord(record)
                selectedContact = null
            }
        )
    }
}

@Composable
fun AttendanceItem(
    contact: Contact,
    contactGroups: List<com.example.attendancetaker.data.ContactGroup>,
    attendanceRecord: AttendanceRecord?,
    onAttendanceChange: (Boolean) -> Unit,
    onEditNotes: () -> Unit
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = contact.phoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (contactGroups.isNotEmpty()) {
                        Text(
                            text = "Groups: ${contactGroups.joinToString(", ") { it.name }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (attendanceRecord?.isPresent == true) stringResource(R.string.present) else stringResource(R.string.absent),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = attendanceRecord?.isPresent ?: false,
                        onCheckedChange = onAttendanceChange
                    )
                }
            }

            if (!attendanceRecord?.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.notes_prefix) + (attendanceRecord?.notes ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditNotes) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.edit_notes),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesDialog(
    contact: Contact,
    currentNotes: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var notes by remember { mutableStateOf(currentNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.notes_for_contact, contact.name))
        },
        text = {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.attendance_notes)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                placeholder = { Text(stringResource(R.string.attendance_notes_placeholder)) }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(notes.trim()) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ButtonBlue
                )
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ButtonNeutral
                )
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}