package com.example.attendancetaker.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.attendancetaker.R
import com.example.attendancetaker.data.AttendanceRecord
import com.example.attendancetaker.data.AttendanceRepository
import com.example.attendancetaker.data.Contact
import com.example.attendancetaker.ui.theme.ButtonBlue
import com.example.attendancetaker.ui.theme.ButtonNeutral
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    eventId: String,
    repository: AttendanceRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var event by remember { mutableStateOf<com.example.attendancetaker.data.Event?>(null) }
    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    var eventContacts by remember { mutableStateOf(emptyList<Contact>()) }
    var selectedGroups by remember { mutableStateOf(emptyList<com.example.attendancetaker.data.ContactGroup>()) }
    val coroutineScope = rememberCoroutineScope()

    val attendanceRecords by repository.getAttendanceForEvent(eventId).collectAsState(initial = emptyList())

    // Load event and related data
    LaunchedEffect(eventId) {
        event = repository.getEventById(eventId)
        if (event == null) {
            onNavigateBack()
            return@LaunchedEffect
        }

        eventContacts = repository.getContactsForEvent(eventId)
        selectedGroups = event!!.contactGroupIds.mapNotNull { groupId ->
            repository.getContactGroup(groupId)
        }
    }

    if (event == null) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event!!.name,
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
                    text = stringResource(R.string.no_contact_groups_selected),
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
                    val attendanceRecord = attendanceRecords.find { it.contactId == contact.id }
                    var contactGroups by remember { mutableStateOf(emptyList<com.example.attendancetaker.data.ContactGroup>()) }

                    // Load contact groups for each contact
                    LaunchedEffect(contact.id, event!!.contactGroupIds) {
                        contactGroups = repository.getGroupsContainingContact(contact.id)
                            .filter { group -> event!!.contactGroupIds.contains(group.id) }
                    }

                    AttendanceItem(
                        contact = contact,
                        contactGroups = contactGroups,
                        attendanceRecord = attendanceRecord,
                        onAttendanceChange = { isPresent ->
                            coroutineScope.launch {
                                val record = attendanceRecord?.copy(isPresent = isPresent)
                                    ?: AttendanceRecord(
                                        contactId = contact.id,
                                        eventId = eventId,
                                        isPresent = isPresent
                                    )
                                repository.updateAttendanceRecord(record)
                            }
                        },
                        onEditNotes = { selectedContact = contact }
                    )
                }
            }
        }
    }

    // Notes Dialog
    selectedContact?.let { contact ->
        val attendanceRecord = attendanceRecords.find { it.contactId == contact.id }
        NotesDialog(
            contact = contact,
            currentNotes = attendanceRecord?.notes ?: "",
            onDismiss = { selectedContact = null },
            onSave = { notes ->
                coroutineScope.launch {
                    val record = attendanceRecord?.copy(notes = notes)
                        ?: AttendanceRecord(
                            contactId = contact.id,
                            eventId = eventId,
                            notes = notes
                        )
                    repository.updateAttendanceRecord(record)
                    selectedContact = null
                }
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
    val context = LocalContext.current

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
                            text = stringResource(R.string.groups_list, contactGroups.joinToString(", ") { it.name }),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (attendanceRecord?.isPresent == true) stringResource(R.string.present) else stringResource(
                            R.string.absent
                        ),
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Contact action buttons
                Row {
                    // WhatsApp button
                    TextButton(onClick = {
                        openWhatsApp(context, contact.phoneNumber)
                    }) {
                        Icon(
                            Icons.Default.Message,
                            contentDescription = "WhatsApp",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "WhatsApp",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Call button
                    TextButton(onClick = {
                        makePhoneCall(context, contact.phoneNumber)
                    }) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = "Call",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Call",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Edit notes button
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

/**
 * Opens WhatsApp with the given phone number
 */
private fun openWhatsApp(context: Context, phoneNumber: String) {
    try {
        // Clean the phone number (remove any non-digit characters except +)
        val cleanPhoneNumber = phoneNumber.replace(Regex("[^\\d+]"), "")

        // Create WhatsApp intent
        val whatsappIntent = Intent(Intent.ACTION_VIEW)
        whatsappIntent.data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhoneNumber")

        // Check if WhatsApp is installed
        if (whatsappIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(whatsappIntent)
        } else {
            // Fallback to web WhatsApp
            val webWhatsappIntent = Intent(Intent.ACTION_VIEW)
            webWhatsappIntent.data = Uri.parse("https://web.whatsapp.com/send?phone=$cleanPhoneNumber")
            context.startActivity(webWhatsappIntent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Initiates a phone call to the given phone number
 */
private fun makePhoneCall(context: Context, phoneNumber: String) {
    try {
        // Clean the phone number (keep digits and + for international numbers)
        val cleanPhoneNumber = phoneNumber.replace(Regex("[^\\d+]"), "")

        // Log for debugging
        println("Attempting to call: $cleanPhoneNumber")

        // Create call intent - using ACTION_DIAL to avoid needing CALL_PHONE permission
        val callIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanPhoneNumber")
            // Add flags to ensure the intent works properly
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Start the activity - remove the resolveActivity check as it might be causing issues
        context.startActivity(callIntent)

    } catch (e: Exception) {
        println("Error making phone call: ${e.message}")
        e.printStackTrace()

        // Fallback: try with a simpler approach
        try {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("tel:${phoneNumber}"))
            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(fallbackIntent)
        } catch (fallbackException: Exception) {
            println("Fallback call also failed: ${fallbackException.message}")
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