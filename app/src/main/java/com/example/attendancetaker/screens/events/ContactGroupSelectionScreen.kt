package com.example.attendancetaker.screens.events

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.attendancetaker.R
import com.example.attendancetaker.data.repository.AttendanceRepository
import com.example.attendancetaker.data.entity.Contact
import com.example.attendancetaker.data.entity.Event
import com.example.attendancetaker.ui.components.AppList
import com.example.attendancetaker.ui.components.AppListItem
import com.example.attendancetaker.ui.components.AppToolbar
import com.example.attendancetaker.ui.components.ToolbarActionPresets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactGroupSelectionScreen(
    repository: AttendanceRepository,
    eventState: EventState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var contactsForGroups by remember { mutableStateOf(mapOf<String, List<Contact>>()) }

    val allContactGroups by repository.getAllContactGroups().collectAsState(initial = emptyList())

    // Load contacts for each group
    LaunchedEffect(allContactGroups) {
        val contactsMap = mutableMapOf<String, List<Contact>>()
        allContactGroups.forEach { group ->
            contactsMap[group.id] = repository.getContactsFromGroups(listOf(group.id))
        }
        contactsForGroups = contactsMap
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Toolbar
        AppToolbar(
            title = stringResource(R.string.select_contact_groups),
            onNavigationClick = onNavigateBack,
            actions = listOf(
                ToolbarActionPresets.saveAction(
                    onClick = {
                        // The selection is already saved in the ViewModel
                        onNavigateBack()
                    }
                )
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
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
                            isSelected = eventState.selectedGroupIds.contains(group.id)
                        )
                    },
                    searchPlaceholder = stringResource(R.string.search_contact_groups),
                    isSelectable = true,
                    selectedItems = eventState.selectedGroupIds,
                    onSelectionChange = { groupId, isSelected ->
                        val group = allContactGroups.find { it.id == groupId }
                        group?.let {
                            eventState.toggleGroup(it)
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