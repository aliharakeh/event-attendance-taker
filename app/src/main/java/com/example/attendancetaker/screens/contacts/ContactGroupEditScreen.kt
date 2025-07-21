package com.example.attendancetaker.screens.contacts

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.attendancetaker.R
import com.example.attendancetaker.data.entity.Contact
import com.example.attendancetaker.data.entity.ContactGroup
import com.example.attendancetaker.data.repository.AttendanceRepository
import com.example.attendancetaker.ui.components.ActionPresets
import com.example.attendancetaker.ui.components.AppCard
import com.example.attendancetaker.ui.components.AppList
import com.example.attendancetaker.ui.components.AppListItem
import com.example.attendancetaker.ui.components.AppTextField
import com.example.attendancetaker.ui.components.AppToolbar
import com.example.attendancetaker.ui.components.ToolbarActionPresets
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
        modifier = modifier.fillMaxSize()
    ) {
        // Toolbar
        AppToolbar(
            title = if (group == null) stringResource(R.string.add_contact_group_title) else stringResource(
                R.string.edit_contact_group_title
            ),
            onNavigationClick = onNavigateBack,
            actions = listOf(
                ToolbarActionPresets.saveAction(
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

                            onNavigateBack()
                        }
                    },
                    enabled = groupName.isNotBlank()
                )
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Group details section
            AppCard(
                title = stringResource(R.string.group_details),
                modifier = Modifier.fillMaxWidth(),
                content = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppTextField(
                            value = groupName,
                            onValueChange = { groupName = it },
                            label = stringResource(R.string.group_name),
                            modifier = Modifier.fillMaxWidth()
                        )

                        AppTextField(
                            value = groupDescription,
                            onValueChange = { groupDescription = it },
                            label = stringResource(R.string.description_optional),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contact selection section
            AppCard(
                title = stringResource(R.string.select_contacts),
                subtitle = stringResource(R.string.contacts_selected, selectedContactIds.size),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                actions = listOf(
                    ActionPresets.editAction(
                        onClick = {
                            if (hasContactsPermission) {
                                onNavigateToContactSelection(groupId)
                            } else {
                                permissionLauncher.launch(Manifest.permission.READ_CONTACTS).apply {
                                    onNavigateToContactSelection(groupId)
                                }
                            }
                        }
                    )
                ),
                content = {
                    if (!hasContactsPermission) {
                        Text(
                            text = stringResource(R.string.contacts_permission_rationale),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        AppList(
                            items = selectedContacts,
                            onItemToListItem = { contact ->
                                AppListItem(
                                    id = contact.id,
                                    title = contact.name,
                                    subtitle = contact.phoneNumber
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            showSearch = true,
                            selectedItems = selectedContactIds,
                            onSelectionChange = { contactId, isSelected ->
                                selectedContactIds = if (isSelected) {
                                    selectedContactIds + contactId
                                } else {
                                    selectedContactIds - contactId
                                }
                            },
                            isEditable = false,
                            emptyStateMessage = stringResource(R.string.no_contacts_selected_for_group),
                            onItemClick = {
                                // Optional: Navigate to contact details or edit
                            }
                        )
                    }
                }
            )
        }
    }
}