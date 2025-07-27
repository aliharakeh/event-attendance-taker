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
    contactGroupState: ContactGroupState,
    onNavigateBack: () -> Unit,
    onNavigateToContactSelection: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var phoneContacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var hasContactsPermission by remember {
        mutableStateOf(
            ContactUtils.hasContactsPermission(
                context
            )
        )
    }
    val contacts by repository.getAllContacts().collectAsState(initial = emptyList())
    var group: ContactGroup? by remember { mutableStateOf(null) }

    // Load group data if editing existing group
    LaunchedEffect(groupId) {
        if (groupId != null) {
            group = repository.getContactGroup(groupId)
            group?.let {
                contactGroupState.groupName = it.name
                contactGroupState.groupDescription = it.description
            }
        }
    }

    // Ensure selected contacts are loaded once the contact list is available
    LaunchedEffect(group, contacts) {
        if (group != null && contacts.isNotEmpty() && contactGroupState.selectedContactIds.isEmpty()) {
            group?.let { grp ->
                val groupContacts = contacts.filter { it.id in grp.contactIds }
                contactGroupState.updateSelectedContacts(groupContacts)
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

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Toolbar
        AppToolbar(
            title = if (groupId == null) stringResource(R.string.add_contact_group_title) else stringResource(
                R.string.edit_contact_group_title
            ),
            onNavigationClick = {
                onNavigateBack()
            },
            actions = listOf(
                ToolbarActionPresets.saveAction(
                    onClick = {
                        coroutineScope.launch {
                            val selectedIds = contactGroupState.selectedContactIds.toList()

                            val updatedGroup = if (groupId == null) {
                                ContactGroup(
                                    name = contactGroupState.groupName.trim(),
                                    description = contactGroupState.groupDescription.trim(),
                                    contactIds = selectedIds
                                )
                            } else {
                                // Updating an existing group – preserve its id
                                group!!.copy(
                                    name = contactGroupState.groupName.trim(),
                                    description = contactGroupState.groupDescription.trim(),
                                    contactIds = selectedIds
                                )
                            }

                            if (groupId == null) {
                                repository.addContactGroup(updatedGroup)
                            } else {
                                repository.updateContactGroup(updatedGroup)
                            }

                            // Add new contacts to repository if they don't exist
                            contactGroupState.selectedContacts.forEach { contact ->
                                val existingContact = contacts.find { it.id == contact.id }
                                if (existingContact == null) {
                                    repository.addContact(contact)
                                }
                            }

                            onNavigateBack()
                        }
                    },
                    enabled = contactGroupState.groupName.isNotBlank()
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
                            value = contactGroupState.groupName,
                            onValueChange = { contactGroupState.groupName = it },
                            label = stringResource(R.string.group_name),
                            modifier = Modifier.fillMaxWidth()
                        )

                        AppTextField(
                            value = contactGroupState.groupDescription,
                            onValueChange = { contactGroupState.groupDescription = it },
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
                subtitle = stringResource(
                    R.string.contacts_selected,
                    contactGroupState.selectedContacts.size
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                actions = listOf(
                    ActionPresets.editAction(
                        onClick = {
                            if (hasContactsPermission) {
                                onNavigateToContactSelection(groupId)
                            } else {
                                permissionLauncher.launch(Manifest.permission.READ_CONTACTS).also {
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
                            items = contactGroupState.selectedContacts,
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
                            isDeletable = true,
                            onDelete = {
                                contactGroupState.removeContact(it.id)
                            },
                            deleteConfirmationTitle = stringResource(R.string.remove_contact),
                            deleteConfirmationMessage = { contactName ->
                                stringResource(R.string.remove_contact_confirmation, contactName)
                            },
                            emptyStateMessage = stringResource(R.string.no_contacts_selected_for_group),
                        )
                    }
                }
            )
        }
    }
}