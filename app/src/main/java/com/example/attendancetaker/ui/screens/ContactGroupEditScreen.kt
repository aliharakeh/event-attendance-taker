package com.example.attendancetaker.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.attendancetaker.data.AttendanceRepository
import com.example.attendancetaker.data.Contact
import com.example.attendancetaker.data.ContactGroup
import com.example.attendancetaker.ui.theme.ButtonBlue
import com.example.attendancetaker.ui.theme.ButtonRed
import com.example.attendancetaker.ui.theme.ButtonNeutral
import com.example.attendancetaker.utils.ContactUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactGroupEditScreen(
    group: ContactGroup?,
    repository: AttendanceRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var groupName by remember { mutableStateOf(group?.name ?: "") }
    var groupDescription by remember { mutableStateOf(group?.description ?: "") }
    var selectedContactIds by remember { mutableStateOf(group?.contactIds?.toSet() ?: emptySet()) }
    var phoneContacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var hasContactsPermission by remember { mutableStateOf(ContactUtils.hasContactsPermission(context)) }
    var showSaveConfirmation by remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasContactsPermission = isGranted
        if (isGranted) {
            phoneContacts = ContactUtils.getPhoneContacts(context)
        }
    }

    // Load phone contacts when permission is available
    LaunchedEffect(hasContactsPermission) {
        if (hasContactsPermission) {
            phoneContacts = ContactUtils.getPhoneContacts(context)
        }
    }

    // Combine phone contacts with existing repository contacts
    val allAvailableContacts = remember(phoneContacts, repository.contacts) {
        val phoneContactMap = phoneContacts.associateBy { it.id }
        val repoContactMap = repository.contacts.associateBy { it.id }

        // Merge both lists, prioritizing repository contacts for duplicates
        val combinedMap = phoneContactMap + repoContactMap
        combinedMap.values.toList()
    }

    // Filter contacts based on search query
    val filteredContacts = remember(allAvailableContacts, searchQuery) {
        if (searchQuery.isBlank()) {
            allAvailableContacts
        } else {
            allAvailableContacts.filter { contact ->
                contact.name.contains(searchQuery, ignoreCase = true) ||
                contact.phoneNumber.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with back button and save button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = if (group == null) "Add Contact Group" else "Edit Contact Group",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
            )

            Button(
                onClick = { showSaveConfirmation = true },
                enabled = groupName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonBlue)
            ) {
                Text("Save")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Group details section
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Group Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = groupDescription,
                    onValueChange = { groupDescription = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "${selectedContactIds.size} contacts selected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contact selection section
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
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
                    Text(
                        text = "Select Contacts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    if (!hasContactsPermission) {
                        Button(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ButtonBlue)
                        ) {
                            Text("Allow Access")
                        }
                    }
                }

                if (!hasContactsPermission) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "This app needs permission to access your contacts to help you select members for this group.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search contacts...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (filteredContacts.isEmpty()) {
                        if (allAvailableContacts.isEmpty()) {
                            Text(
                                text = "No contacts available. Please allow contact access or add contacts manually.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "No contacts match your search.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Contact list
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredContacts) { contact ->
                                ContactSelectionItem(
                                    contact = contact,
                                    isSelected = selectedContactIds.contains(contact.id),
                                    onSelectionChanged = { isSelected ->
                                        selectedContactIds = if (isSelected) {
                                            selectedContactIds + contact.id
                                        } else {
                                            selectedContactIds - contact.id
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Save confirmation dialog
    if (showSaveConfirmation) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmation = false },
            title = { Text("Save Changes") },
            text = {
                Text("Do you want to save the changes to this contact group?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Save the group
                        val updatedGroup = if (group == null) {
                            ContactGroup(
                                name = groupName.trim(),
                                description = groupDescription.trim(),
                                contactIds = selectedContactIds.toList()
                            )
                        } else {
                            group.copy(
                                name = groupName.trim(),
                                description = groupDescription.trim(),
                                contactIds = selectedContactIds.toList()
                            )
                        }

                        if (group == null) {
                            repository.addContactGroup(updatedGroup)
                        } else {
                            repository.updateContactGroup(updatedGroup)
                        }

                        // Add new contacts to repository if they don't exist
                        selectedContactIds.forEach { contactId ->
                            val existingContact = repository.contacts.find { it.id == contactId }
                            if (existingContact == null) {
                                val availableContact = allAvailableContacts.find { it.id == contactId }
                                availableContact?.let { repository.addContact(it) }
                            }
                        }

                        showSaveConfirmation = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ButtonBlue)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSaveConfirmation = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = ButtonNeutral)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ContactSelectionItem(
    contact: Contact,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 3.dp else 1.dp
        ),
        border = if (isSelected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null,
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChanged,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    checkmarkColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = contact.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Selection indicator
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}