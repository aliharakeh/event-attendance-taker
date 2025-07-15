package com.example.attendancetaker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
            Column {
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
            }
        }

        // Attendance List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(repository.contacts) { contact ->
                val attendanceRecord = repository.getAttendanceRecord(eventId, contact.id)
                AttendanceItem(
                    contact = contact,
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
    attendanceRecord: AttendanceRecord?,
    onAttendanceChange: (Boolean) -> Unit,
    onEditNotes: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (attendanceRecord?.isPresent == true) stringResource(R.string.present) else stringResource(R.string.absent),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
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
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.edit_notes),
                        color = Color.Black
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
                    contentColor = ButtonBlue // Blue for save button
                )
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ButtonRed // Red for cancel button
                )
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}