package com.example.attendancetaker.screens.contacts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.attendancetaker.R
import com.example.attendancetaker.data.entity.Contact
import com.example.attendancetaker.data.entity.ContactGroup
import com.example.attendancetaker.data.repository.AttendanceRepository
import com.example.attendancetaker.ui.components.AppList
import com.example.attendancetaker.ui.components.AppListItem
import kotlinx.coroutines.launch

@Composable
fun ContactsScreen(
    repository: AttendanceRepository,
    onNavigateToGroupEdit: (ContactGroup?) -> Unit,
    onNavigateToGroupDetails: (ContactGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    val contactGroups by repository.getAllContactGroups().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        AppList(
            items = contactGroups,
            onItemToListItem = { group ->
                var contacts by remember(group.id) { mutableStateOf(emptyList<Contact>()) }

                // Load contacts for this group
                LaunchedEffect(group.id) {
                    contacts = repository.getContactsFromGroups(listOf(group.id))
                }

                AppListItem(
                    id = group.id,
                    title = group.name,
                    subtitle = if (group.description.isNotEmpty()) group.description else null,
                    content = {
                        Column {
                            Text(
                                text = stringResource(R.string.members_count, contacts.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (contacts.isNotEmpty()) {
                                Text(
                                    text = contacts.take(3).joinToString(", ") { it.name } +
                                            if (contacts.size > 3) " " + stringResource(
                                                R.string.and_more,
                                                contacts.size - 3
                                            ) else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )
            },
            searchPlaceholder = stringResource(R.string.search_contact_groups),
            showSearch = true,
            isEditable = true,
            isDeletable = true,
            isSelectable = true,
            onEdit = { group -> onNavigateToGroupEdit(group) },
            onDelete = { group ->
                coroutineScope.launch {
                    repository.removeContactGroup(group.id)
                }
            },
            onItemClick = { group -> onNavigateToGroupDetails(group) },
            emptyStateMessage = "No contact groups found",
            modifier = Modifier.fillMaxSize()
        )
    }
}