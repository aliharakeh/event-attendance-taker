package com.example.attendancetaker.screens.events

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.attendancetaker.R
import com.example.attendancetaker.data.repository.AttendanceRepository
import com.example.attendancetaker.data.entity.Contact
import com.example.attendancetaker.data.entity.ContactGroup
import com.example.attendancetaker.data.entity.Event
import com.example.attendancetaker.ui.components.ActionItem
import com.example.attendancetaker.ui.components.AppList
import com.example.attendancetaker.ui.components.AppListItem
import com.example.attendancetaker.ui.components.AppToolbar
import com.example.attendancetaker.ui.components.ToolbarAction
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactGroupSelectionScreen(
    eventId: String?,
    repository: AttendanceRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var event by remember { mutableStateOf<Event?>(null) }
    var selectedGroupIds by remember { mutableStateOf(emptySet<String>()) }
    var contactsForGroups by remember { mutableStateOf(mapOf<String, List<Contact>>()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val allContactGroups by repository.getAllContactGroups().collectAsState(initial = emptyList())

    // Load event data if editing existing event
    LaunchedEffect(eventId) {
        isLoading = true
        try {
            if (eventId != null && eventId != "new") {
                event = repository.getEventById(eventId)
                selectedGroupIds = event?.contactGroupIds?.toSet() ?: emptySet()
            }
        } finally {
            isLoading = false
        }
    }

    // Load contacts for each group
    LaunchedEffect(allContactGroups) {
        val contactsMap = mutableMapOf<String, List<Contact>>()
        allContactGroups.forEach { group ->
            contactsMap[group.id] = repository.getContactsFromGroups(listOf(group.id))
        }
        contactsForGroups = contactsMap
    }

    // Save function
    val saveSelection: () -> Unit = {
        coroutineScope.launch {
            isSaving = true
            try {
                if (event != null) {
                    // Update existing event
                    val updatedEvent = event!!.copy(
                        contactGroupIds = selectedGroupIds.toList()
                    )
                    repository.updateEvent(updatedEvent)
                    snackbarHostState.showSnackbar("Contact groups updated successfully")
                    onNavigateBack()
                } else {
                    snackbarHostState.showSnackbar("No event found to update")
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Failed to save: ${e.message}")
            } finally {
                isSaving = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (isLoading) {
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                AppList(
                    items = allContactGroups,
                    onItemToListItem = { group ->
                        val contacts = contactsForGroups[group.id] ?: emptyList()
                        AppListItem(
                            id = group.id,
                            title = group.name,
                            subtitle = if (group.description.isNotEmpty()) {
                                "${group.description} • ${stringResource(R.string.members_count, contacts.size)}"
                            } else {
                                stringResource(R.string.members_count, contacts.size)
                            },
                            isSelected = selectedGroupIds.contains(group.id)
                        )
                    },
                    searchPlaceholder = stringResource(R.string.search_contact_groups),
                    isSelectable = true,
                    selectedItems = selectedGroupIds,
                    globalAction = listOf(
                        ActionItem(
                            icon = Icons.Default.Save,
                            contentDescription = "Save",
                            onClick = saveSelection,
                            enabled = selectedGroupIds.isNotEmpty(),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    ),
                    onSelectionChange = { groupId, isSelected ->
                        selectedGroupIds = if (isSelected) {
                            selectedGroupIds + groupId
                        } else {
                            selectedGroupIds - groupId
                        }
                    },
                    emptyStateMessage = if (allContactGroups.isEmpty()) {
                        stringResource(R.string.no_contact_groups_available)
                    } else {
                        stringResource(R.string.no_contact_groups_match_search)
                    }
                )
            }
        }
    }
}