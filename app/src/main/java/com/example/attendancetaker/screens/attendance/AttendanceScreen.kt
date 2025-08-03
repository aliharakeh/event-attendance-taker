package com.example.attendancetaker.screens.attendance

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.attendancetaker.R
import com.example.attendancetaker.data.entity.AttendanceRecord
import com.example.attendancetaker.data.repository.AttendanceRepository
import com.example.attendancetaker.data.entity.Contact
import com.example.attendancetaker.data.entity.ContactGroup
import com.example.attendancetaker.data.entity.Event
import com.example.attendancetaker.ui.components.AppActionRow
import com.example.attendancetaker.ui.components.ActionItem
import com.example.attendancetaker.ui.components.AppCard
import com.example.attendancetaker.ui.components.AppIconButton
import com.example.attendancetaker.ui.components.AppIconButtonStyle
import com.example.attendancetaker.ui.components.AppList
import com.example.attendancetaker.ui.components.AppListItem
import com.example.attendancetaker.ui.components.AppNotesDialog
import com.example.attendancetaker.ui.components.AppTextContent
import com.example.attendancetaker.ui.components.AppToolbar
import com.example.attendancetaker.ui.components.ToolbarAction
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
    var event by remember { mutableStateOf<Event?>(null) }
    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    var eventContacts by remember { mutableStateOf(emptyList<Contact>()) }
    var selectedGroups by remember { mutableStateOf(emptyList<ContactGroup>()) }
    var showNotesDialog by remember { mutableStateOf(false) }
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
        // Header using AppToolbar
        AppToolbar(
            title = event!!.name,
            subtitle = stringResource(R.string.attendance_tracking),
            onNavigationClick = onNavigateBack
        )

        // Show message if no contact groups are selected
        if (selectedGroups.isEmpty()) {
            AppCard(
                modifier = Modifier.padding(16.dp),
                title = stringResource(R.string.no_contact_groups_selected),
                content = {}
            )
        } else {
            // Attendance List using AppList
            AppList(
                items = eventContacts,
                onItemToListItem = { contact ->
                    val attendanceRecord = attendanceRecords.find { it.contactId == contact.id }
                    var contactGroups by remember { mutableStateOf(emptyList<ContactGroup>()) }

                    // Load contact groups for each contact
                    LaunchedEffect(contact.id, event!!.contactGroupIds) {
                        contactGroups = repository.getGroupsContainingContact(contact.id)
                            .filter { group -> event!!.contactGroupIds.contains(group.id) }
                    }

                    AppListItem(
                        id = contact.id,
                        title = contact.name,
                        subtitle = contact.phoneNumber,
                        content = {
                            AttendanceItemContent(
                                contact = contact,
                                contactGroups = contactGroups,
                                attendanceRecord = attendanceRecord,
                                onEditNotes = {
                                    selectedContact = contact
                                    showNotesDialog = true
                                }
                            )
                        }
                    )
                },
                cardActions = { contact ->
                    val attendanceRecord = attendanceRecords.find { it.contactId == contact.id }
                    listOf(
                        ActionItem(
                            icon = Icons.Default.Alarm, // Placeholder icon, won't be used
                            contentDescription = "Toggle Attendance",
                            tint = MaterialTheme.colorScheme.primary,
                            onClick = { /* Handled by template */ },
                            template = {
                                Switch(
                                    checked = attendanceRecord?.isPresent ?: false,
                                    onCheckedChange = { isPresent ->
                                        coroutineScope.launch {
                                            val record = attendanceRecord?.copy(isPresent = isPresent)
                                                ?: AttendanceRecord(
                                                    contactId = contact.id,
                                                    eventId = eventId,
                                                    isPresent = isPresent
                                                )
                                            repository.updateAttendanceRecord(record)
                                        }
                                    }
                                )
                            }
                        )
                    )
                },
                modifier = Modifier.padding(horizontal = 16.dp),
                showSearch = true,
                searchPlaceholder = stringResource(R.string.search_contacts),
                emptyStateMessage = stringResource(R.string.no_search_results)
            )
        }
    }

    // Notes Dialog using AppNotesDialog
    selectedContact?.let { contact ->
        val attendanceRecord = attendanceRecords.find { it.contactId == contact.id }
        AppNotesDialog(
            isVisible = showNotesDialog,
            title = stringResource(R.string.notes_for_contact, contact.name),
            initialNotes = attendanceRecord?.notes ?: "",
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
                    showNotesDialog = false
                }
            },
            onDismiss = {
                selectedContact = null
                showNotesDialog = false
            },
            placeholder = stringResource(R.string.attendance_notes_placeholder)
        )
    }
}

@Composable
fun AttendanceItemContent(
    contact: Contact,
    contactGroups: List<ContactGroup>,
    attendanceRecord: AttendanceRecord?,
    onEditNotes: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Groups information
        if (contactGroups.isNotEmpty()) {
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
                    text = stringResource(R.string.groups_list, contactGroups.joinToString(", ") { it.name }),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Contact work and notes information
        if (contact.workTimeStart != null || contact.workTimeEnd != null || !contact.notes.isNullOrBlank()) {
            // Work time display
            if (contact.workTimeStart != null || contact.workTimeEnd != null) {
                val workTimeText = buildString {
                    if (contact.workTimeStart != null) append(contact.workTimeStart)
                    if (contact.workTimeStart != null && contact.workTimeEnd != null) append(" - ")
                    if (contact.workTimeEnd != null) append(contact.workTimeEnd)
                }
                AppTextContent(
                    title = "Work Time",
                    content = workTimeText
                )
            }

            // Contact notes display
            if (!contact.notes.isNullOrBlank()) {
                AppTextContent(
                    title = "Contact Notes",
                    content = contact.notes
                )
            }
        }

        // Attendance notes display
        if (!attendanceRecord?.notes.isNullOrBlank()) {
            AppTextContent(
                title = "Attendance Notes",
                content = attendanceRecord.notes,
                spacerHeight = 8.dp
            )
        }

        // Attendance status
        Text(
            text = if (attendanceRecord?.isPresent == true) stringResource(R.string.present) else stringResource(
                R.string.absent
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Action buttons using AppIconButton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // WhatsApp message button
            AppIconButton(
                style = AppIconButtonStyle.ROUNDED_ICON_ONLY,
                onClick = { openWhatsAppMessage(context, contact.phoneNumber) },
                icon = Icons.Default.Whatsapp,
                backgroundColor = Color(0xFF25D366),
                contentColor = Color.White,
                modifier = Modifier.weight(1f),
                contentDescription = "WhatsApp Message"
            )

            // Call button
            AppIconButton(
                style = AppIconButtonStyle.ROUNDED_ICON_ONLY,
                onClick = { openWhatsAppCall(context, contact.phoneNumber) },
                icon = Icons.Default.Call,
                backgroundColor = Color(0xFF0B5D9C),
                contentColor = Color.White,
                modifier = Modifier.weight(1f),
                contentDescription = "Call"
            )

            // Edit notes button
            AppIconButton(
                style = AppIconButtonStyle.NO_BACKGROUND_ICON_TEXT,
                onClick = onEditNotes,
                icon = Icons.Default.Edit,
                text = stringResource(R.string.edit_notes),
                contentColor = MaterialTheme.colorScheme.primary,
                contentDescription = stringResource(R.string.edit_notes)
            )
        }
    }
}

// WhatsApp functions reused from ContactGroupDetailsScreen
private fun openWhatsAppMessage(context: Context, phoneNumber: String) {
    try {
        // Clean the phone number (remove any non-numeric characters except +)
        val cleanedNumber = phoneNumber.replace(Regex("[^+\\d]"), "")

        // Try to open WhatsApp directly
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/$cleanedNumber")
            setPackage("com.whatsapp")
        }

        // Check if WhatsApp is installed
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback to web WhatsApp
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$cleanedNumber")
            }
            context.startActivity(webIntent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        // Fallback to regular SMS
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")
        }
        try {
            context.startActivity(smsIntent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}

private fun openWhatsAppCall(context: Context, phoneNumber: String) {
    try {
        // Clean the phone number
        val cleanedNumber = phoneNumber.replace(Regex("[^+\\d]"), "")

        // Try to open WhatsApp call directly
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/$cleanedNumber?call")
            setPackage("com.whatsapp")
        }

        // Check if WhatsApp is installed
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback to regular phone call
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            context.startActivity(callIntent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        // Fallback to regular phone call
        val callIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        try {
            context.startActivity(callIntent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}
