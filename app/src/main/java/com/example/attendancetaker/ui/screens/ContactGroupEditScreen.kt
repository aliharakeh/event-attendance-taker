package com.example.attendancetaker.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.attendancetaker.data.AttendanceRepository
import com.example.attendancetaker.data.Contact
import com.example.attendancetaker.data.ContactGroup
import com.example.attendancetaker.ui.theme.ButtonBlue
import com.example.attendancetaker.ui.theme.ButtonNeutral
import com.example.attendancetaker.utils.ContactUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactGroupEditScreen(
    groupId: String?,
    repository: AttendanceRepository,
    onNavigateBack: () -> Unit,
    onNavigateToContactSelection: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var group by remember { mutableStateOf<ContactGroup?>(null) }
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var selectedContactIds by remember { mutableStateOf(emptySet<String>()) }
    var phoneContacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var hasContactsPermission by remember {
        mutableStateOf(
            ContactUtils.hasContactsPermission(
                context
            )
        )
    }
    var showSaveConfirmation by remember { mutableStateOf(false) }

    val contacts by repository.getAllContacts().collectAsState(initial = emptyList())

    // Load group data if editing existing group
    LaunchedEffect(groupId) {
        if (groupId != null) {
            group = repository.getContactGroup(groupId)
            group?.let {
                groupName = it.name
                groupDescription = it.description
                selectedContactIds = it.contactIds.toSet()
            }
        }
    }

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
    val allAvailableContacts = remember(phoneContacts, contacts) {
        val phoneContactMap = phoneContacts.associateBy { it.id }
        val repoContactMap = contacts.associateBy { it.id }

        // Merge both lists, prioritizing repository contacts for duplicates
        val combinedMap = phoneContactMap + repoContactMap
        combinedMap.values.toList()
    }

    // Get selected contacts for display
    val selectedContacts = remember(selectedContactIds, allAvailableContacts) {
        allAvailableContacts.filter { selectedContactIds.contains(it.id) }
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
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }

            Text(
                text = if (group == null) stringResource(R.string.add_contact_group_title) else stringResource(R.string.edit_contact_group_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )

            Button(
                onClick = { showSaveConfirmation = true },
                enabled = groupName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonBlue)
            ) {
                Text(stringResource(R.string.save))
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
                    text = stringResource(R.string.group_details),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text(stringResource(R.string.group_name)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = groupDescription,
                    onValueChange = { groupDescription = it },
                    label = { Text(stringResource(R.string.description_optional)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contact selection section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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
                        text = stringResource(R.string.select_contacts),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedButton(
                        onClick = {
                            if (hasContactsPermission) {
                                onNavigateToContactSelection(groupId)
                            } else {
                                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = if (hasContactsPermission)
                                stringResource(R.string.add)
                            else
                                stringResource(R.string.allow_access)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.contacts_selected, selectedContactIds.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                if (!hasContactsPermission) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.contacts_permission_rationale),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (selectedContacts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedContacts) { contact ->
                            SelectedContactItem(
                                contact = contact,
                                onRemove = {
                                    selectedContactIds = selectedContactIds - contact.id
                                }
                            )
                        }
                    }
                } else if (hasContactsPermission) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_contacts_selected_for_group),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Save confirmation dialog
    if (showSaveConfirmation) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmation = false },
            title = { Text(stringResource(R.string.save_changes)) },
            text = {
                Text(stringResource(R.string.save_changes_question))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            // Save the group
                            val updatedGroup = if (group == null) {
                                ContactGroup(
                                    name = groupName.trim(),
                                    description = groupDescription.trim(),
                                    contactIds = selectedContactIds.toList()
                                )
                            } else {
                                group!!.copy(
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
                                val existingContact = contacts.find { it.id == contactId }
                                if (existingContact == null) {
                                    val availableContact =
                                        allAvailableContacts.find { it.id == contactId }
                                    availableContact?.let { repository.addContact(it) }
                                }
                            }

                            showSaveConfirmation = false
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ButtonBlue)
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSaveConfirmation = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = ButtonNeutral)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}



@Composable
fun SelectedContactItem(
    contact: Contact,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = contact.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.remove_contact),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

