package com.example.attendancetaker.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import com.example.attendancetaker.data.AttendanceRecord
import com.example.attendancetaker.data.AttendanceRepository
import com.example.attendancetaker.data.Contact
import com.example.attendancetaker.ui.theme.ButtonBlue
import com.example.attendancetaker.ui.theme.ButtonNeutral
import kotlinx.coroutines.launch

enum class SummaryLanguage(val displayName: String, val code: String) {
    ENGLISH("English", "en"),
    ARABIC("العربية", "ar")
}

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
    var searchQuery by remember { mutableStateOf("") }
    var showSummaryDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val attendanceRecords by repository.getAttendanceForEvent(eventId).collectAsState(initial = emptyList())

    // Filter contacts based on search query
    val filteredContacts by remember {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                eventContacts
            } else {
                eventContacts.filter { contact ->
                    contact.name.contains(searchQuery, ignoreCase = true) ||
                    contact.phoneNumber.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

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
            IconButton(onClick = { showSummaryDialog = true }) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = stringResource(R.string.summary),
                    tint = MaterialTheme.colorScheme.primary
                )
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
            // Search Field
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.search_contacts_placeholder)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = stringResource(R.string.clear_search),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                singleLine = true
            )

            // Results count
            if (searchQuery.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.search_results_count, filteredContacts.size, eventContacts.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Attendance List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredContacts) { contact ->
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

    // Summary Dialog
    if (showSummaryDialog) {
        AttendanceSummaryDialog(
            eventName = event!!.name,
            contacts = eventContacts,
            attendanceRecords = attendanceRecords,
            onDismiss = { showSummaryDialog = false }
        )
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

            Spacer(modifier = Modifier.height(12.dp))

            // WhatsApp buttons (reusing from ContactGroupDetailsScreen)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Message button
                Button(
                    onClick = { openWhatsAppMessage(context, contact.phoneNumber) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366) // WhatsApp green
                    )
                ) {
                    Icon(
                        Icons.Default.Whatsapp,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Call button
                Button(
                    onClick = { openWhatsAppCall(context, contact.phoneNumber) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0B5D9C)
                    )
                ) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
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

@Composable
fun AttendanceSummaryDialog(
    eventName: String,
    contacts: List<Contact>,
    attendanceRecords: List<AttendanceRecord>,
    onDismiss: () -> Unit
) {
    var showPresent by remember { mutableStateOf(true) }
    var showAbsent by remember { mutableStateOf(true) }
    var summaryLanguage by remember { mutableStateOf(SummaryLanguage.ENGLISH) }
    var showLanguageDropdown by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Separate contacts into present and absent lists
    val presentContacts = contacts.filter { contact ->
        attendanceRecords.find { it.contactId == contact.id }?.isPresent == true
    }
    val absentContacts = contacts.filter { contact ->
        val record = attendanceRecords.find { it.contactId == contact.id }
        record?.isPresent != true
    }

    // Generate summary text based on current filter settings
    val summaryText = remember(showPresent, showAbsent, presentContacts, absentContacts, summaryLanguage) {
        generateSummaryText(eventName, presentContacts, absentContacts, showPresent, showAbsent, summaryLanguage)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.attendance_summary))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Filter checkboxes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = showPresent,
                            onCheckedChange = { showPresent = it }
                        )
                        Text(
                            text = stringResource(R.string.show_present),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = showAbsent,
                            onCheckedChange = { showAbsent = it }
                        )
                        Text(
                            text = stringResource(R.string.show_absent),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Language selector
                Text(
                    text = stringResource(R.string.summary_language),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showLanguageDropdown = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = summaryLanguage.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showLanguageDropdown,
                        onDismissRequest = { showLanguageDropdown = false }
                    ) {
                        SummaryLanguage.values().forEach { language ->
                            DropdownMenuItem(
                                text = { Text(language.displayName) },
                                onClick = {
                                    summaryLanguage = language
                                    showLanguageDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Summary content
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = eventName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = getTotalAttendeesText(contacts.size, summaryLanguage),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (showPresent && presentContacts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = getPresentAttendeesText(presentContacts.size, summaryLanguage),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            presentContacts.forEach { contact ->
                                Text(
                                    text = "• ${contact.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                )
                            }
                        }

                        if (showAbsent && absentContacts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = getAbsentAttendeesText(absentContacts.size, summaryLanguage),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                            absentContacts.forEach { contact ->
                                Text(
                                    text = "• ${contact.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Copy button
                TextButton(
                    onClick = {
                        copyToClipboard(context, summaryText)
                        Toast.makeText(context, context.getString(R.string.summary_copied), Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.copy_summary))
                }

                // Share button
                TextButton(
                    onClick = {
                        shareText(context, summaryText)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.share_summary))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ButtonNeutral
                )
            ) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

private fun generateSummaryText(
    eventName: String,
    presentContacts: List<Contact>,
    absentContacts: List<Contact>,
    showPresent: Boolean,
    showAbsent: Boolean,
    language: SummaryLanguage
): String {
    val builder = StringBuilder()

    // Use hardcoded strings based on selected language
    when (language) {
        SummaryLanguage.ENGLISH -> {
            builder.appendLine("Attendance Summary")
            builder.appendLine(eventName)
            builder.appendLine()
            builder.appendLine("Total: ${presentContacts.size + absentContacts.size}")
            builder.appendLine()

            if (showPresent && presentContacts.isNotEmpty()) {
                builder.appendLine("Present (${presentContacts.size})")
                presentContacts.forEach { contact ->
                    builder.appendLine("• ${contact.name}")
                }
                builder.appendLine()
            }

            if (showAbsent && absentContacts.isNotEmpty()) {
                builder.appendLine("Absent (${absentContacts.size})")
                absentContacts.forEach { contact ->
                    builder.appendLine("• ${contact.name}")
                }
            }
        }
        SummaryLanguage.ARABIC -> {
            builder.appendLine("ملخص الحضور")
            builder.appendLine(eventName)
            builder.appendLine()
            builder.appendLine("المجموع: ${presentContacts.size + absentContacts.size}")
            builder.appendLine()

            if (showPresent && presentContacts.isNotEmpty()) {
                builder.appendLine("الحاضرون (${presentContacts.size})")
                presentContacts.forEach { contact ->
                    builder.appendLine("• ${contact.name}")
                }
                builder.appendLine()
            }

            if (showAbsent && absentContacts.isNotEmpty()) {
                builder.appendLine("الغائبون (${absentContacts.size})")
                absentContacts.forEach { contact ->
                    builder.appendLine("• ${contact.name}")
                }
            }
        }
    }

    return builder.toString().trim()
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Attendance Summary", text)
    clipboard.setPrimaryClip(clip)
}

private fun shareText(context: Context, text: String) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_summary)))
}

private fun getTotalAttendeesText(count: Int, language: SummaryLanguage): String {
    return when (language) {
        SummaryLanguage.ENGLISH -> "Total: $count"
        SummaryLanguage.ARABIC -> "المجموع: $count"
    }
}

private fun getPresentAttendeesText(count: Int, language: SummaryLanguage): String {
    return when (language) {
        SummaryLanguage.ENGLISH -> "Present ($count)"
        SummaryLanguage.ARABIC -> "الحاضرون ($count)"
    }
}

private fun getAbsentAttendeesText(count: Int, language: SummaryLanguage): String {
    return when (language) {
        SummaryLanguage.ENGLISH -> "Absent ($count)"
        SummaryLanguage.ARABIC -> "الغائبون ($count)"
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