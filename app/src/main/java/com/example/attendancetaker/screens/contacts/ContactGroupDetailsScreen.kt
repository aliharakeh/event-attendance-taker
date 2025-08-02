package com.example.attendancetaker.screens.contacts

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.launch

import com.example.attendancetaker.R
import com.example.attendancetaker.data.entity.Contact
import com.example.attendancetaker.data.entity.ContactGroup
import com.example.attendancetaker.data.repository.AttendanceRepository
import com.example.attendancetaker.ui.components.ActionItem
import com.example.attendancetaker.ui.components.AppList
import com.example.attendancetaker.ui.components.AppListItem
import com.example.attendancetaker.ui.components.AppToolbar
import com.example.attendancetaker.ui.components.AppTextField
import com.example.attendancetaker.ui.components.AppTimePickerDialog
import com.example.attendancetaker.ui.components.AppTimeRangePicker
import com.example.attendancetaker.ui.components.AppNotesDialog
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun ContactGroupDetailsScreen(
    groupId: String,
    repository: AttendanceRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var group by remember { mutableStateOf<ContactGroup?>(null) }
    var contacts by remember { mutableStateOf(emptyList<Contact>()) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var selectedContactForNotes by remember { mutableStateOf<Contact?>(null) }

    // Load group and contacts data
    LaunchedEffect(groupId) {
        group = repository.getContactGroup(groupId)
        if (group == null) {
            onNavigateBack()
            return@LaunchedEffect
        }
        contacts = repository.getContactsFromGroups(listOf(group!!.id))
    }

    if (group == null) {
        return
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // App Toolbar with group info
        AppToolbar(
            title = group!!.name,
            subtitle = if (group!!.description.isNotEmpty()) {
                "${group!!.description} • ${contacts.size} members"
            } else {
                "${contacts.size} members"
            },
            onNavigationClick = onNavigateBack
        )

        // Contacts List
        AppList(
            items = contacts,
            onItemToListItem = { contact ->
                AppListItem(
                    id = contact.id,
                    title = contact.name,
                    subtitle = contact.phoneNumber,
                    content = {
                        ContactListItem(
                            contact = contact,
                            repository = repository,
                            context = context,
                            onContactUpdated = { updatedContact ->
                                // Update the contact in the local list
                                contacts = contacts.map {
                                    if (it.id == updatedContact.id) updatedContact else it
                                }
                            }
                        )
                    }
                )
            },
            showSearch = true,
            emptyStateMessage = stringResource(R.string.no_contacts_in_group),
            cardActions = { contact ->
                getContactActions(contact, context) {
                    selectedContactForNotes = contact
                    showNotesDialog = true
                }
            },
            modifier = Modifier.padding(16.dp)
        )
    }

    // Notes dialog
    selectedContactForNotes?.let { contact ->
        AppNotesDialog(
            isVisible = showNotesDialog,
            title = "Contact Notes",
            initialNotes = contact.notes ?: "",
            onSave = { newNotes ->
                val updatedContact = contact.copy(notes = newNotes.takeIf { it.isNotEmpty() })
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    repository.updateContact(updatedContact)
                }
                // Update the contact in the local list
                contacts = contacts.map {
                    if (it.id == updatedContact.id) updatedContact else it
                }
                showNotesDialog = false
                selectedContactForNotes = null
            },
            onDismiss = {
                showNotesDialog = false
                selectedContactForNotes = null
            },
            placeholder = "Add notes about this contact..."
        )
    }
}

@Composable
private fun ContactListItem(
    contact: Contact,
    repository: AttendanceRepository,
    context: Context,
    onContactUpdated: (Contact) -> Unit
) {
    var workTimeStart by remember { mutableStateOf(contact.workTimeStart?.let { LocalTime.parse(it) }) }
    var workTimeEnd by remember { mutableStateOf(contact.workTimeEnd?.let { LocalTime.parse(it) }) }

    // Function to save changes automatically
    fun saveChanges() {
        val updatedContact = contact.copy(
            workTimeStart = workTimeStart?.format(DateTimeFormatter.ofPattern("HH:mm")),
            workTimeEnd = workTimeEnd?.format(DateTimeFormatter.ofPattern("HH:mm"))
        )
        // Update in repository
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            repository.updateContact(updatedContact)
        }
        onContactUpdated(updatedContact)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        // Work time section
        Text(
            text = "Work",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        AppTimeRangePicker(
            startTime = workTimeStart,
            endTime = workTimeEnd,
            onStartTimeChange = {
                workTimeStart = it
                saveChanges()
            },
            onEndTimeChange = {
                workTimeEnd = it
                saveChanges()
            },
            startTimePlaceholder = "Start Time",
            endTimePlaceholder = "End Time"
        )

        // Notes section
        if (contact.notes?.isNotEmpty() == true) {
            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = contact.notes ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun getContactActions(
    contact: Contact,
    context: Context,
    onNotesClick: () -> Unit
): List<ActionItem> {
    return listOf(
        ActionItem(
            icon = Icons.Default.Whatsapp,
            contentDescription = "Send WhatsApp Message",
            tint = Color(0xFF25D366), // WhatsApp green
            onClick = { openWhatsAppMessage(context, contact.phoneNumber) }
        ),
        ActionItem(
            icon = Icons.Default.Call,
            contentDescription = "WhatsApp Call",
            tint = Color(0xFF0B5D9C),
            onClick = { openWhatsAppCall(context, contact.phoneNumber) }
        ),
        ActionItem(
            icon = Icons.AutoMirrored.Filled.Note,
            contentDescription = "Edit notes",
            tint = MaterialTheme.colorScheme.primary,
            onClick = onNotesClick
        )
    )
}

private fun openWhatsAppMessage(context: Context, phoneNumber: String) {
    try {
        // Clean the phone number (remove any non-numeric characters except +)
        val cleanedNumber = phoneNumber.replace(Regex("[^+\\d]"), "")

        // Try to open WhatsApp directly
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "https://wa.me/$cleanedNumber".toUri()
            setPackage("com.whatsapp")
        }

        // Check if WhatsApp is installed
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback to web WhatsApp
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = "https://wa.me/$cleanedNumber".toUri()
            }
            context.startActivity(webIntent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        // Fallback to regular SMS
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = "smsto:$phoneNumber".toUri()
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
            data = "https://wa.me/$cleanedNumber?call".toUri()
            setPackage("com.whatsapp")
        }

        // Check if WhatsApp is installed
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback to regular phone call
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = "tel:$phoneNumber".toUri()
            }
            context.startActivity(callIntent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        // Fallback to regular phone call
        val callIntent = Intent(Intent.ACTION_DIAL).apply {
            data = "tel:$phoneNumber".toUri()
        }
        try {
            context.startActivity(callIntent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}