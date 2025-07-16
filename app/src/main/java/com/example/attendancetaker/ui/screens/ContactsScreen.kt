package com.example.attendancetaker.ui.screens
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.attendancetaker.R
import com.example.attendancetaker.data.AttendanceRepository
import com.example.attendancetaker.data.ContactGroup
import com.example.attendancetaker.ui.theme.ButtonBlue
import com.example.attendancetaker.ui.theme.ButtonRed
import com.example.attendancetaker.ui.theme.EditIconBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    repository: AttendanceRepository,
    showAddDialog: Boolean,
    onAddDialogDismiss: () -> Unit,
    onNavigateToGroupEdit: (ContactGroup?) -> Unit,
    onNavigateToGroupDetails: (ContactGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddGroupDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Text(
            text = "Contact Groups",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        // Contact Groups List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(repository.contactGroups) { group ->
                ContactGroupItem(
                    group = group,
                    repository = repository,
                    onEdit = { onNavigateToGroupEdit(group) },
                    onDelete = { repository.removeContactGroup(group.id) },
                    onItemClick = { onNavigateToGroupDetails(group) }
                )
            }
        }
    }

    // Add Group Dialog
    if (showAddDialog || showAddGroupDialog) {
        ContactGroupDialog(
            group = null,
            onDismiss = {
                onAddDialogDismiss()
                showAddGroupDialog = false
            },
            onSave = { name, description ->
                val newGroup = ContactGroup(
                    name = name,
                    description = description,
                    contactIds = emptyList()
                )
                repository.addContactGroup(newGroup)
                onAddDialogDismiss()
                showAddGroupDialog = false
                // Navigate to edit screen to add contacts
                onNavigateToGroupEdit(newGroup)
            }
        )
    }
}

@Composable
fun ContactGroupItem(
    group: ContactGroup,
    repository: AttendanceRepository,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onItemClick: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val contacts = repository.getContactsFromGroups(listOf(group.id))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        text = group.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (group.description.isNotEmpty()) {
                        Text(
                            text = group.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${contacts.size} members",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (contacts.isNotEmpty()) {
                        Text(
                            text = contacts.take(3).joinToString(", ") { it.name } +
                                if (contacts.size > 3) " and ${contacts.size - 3} more" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Group",
                            tint = EditIconBlue
                        )
                    }
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Group",
                            tint = Color(0xFFE53E3E)
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Group") },
            text = { Text("Are you sure you want to delete the group '${group.name}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = ButtonRed
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = ButtonRed
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactGroupDialog(
    group: ContactGroup?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(group?.name ?: "") }
    var description by remember { mutableStateOf(group?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (group == null) "Add Contact Group" else "Edit Contact Group")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), description.trim())
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ButtonBlue
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ButtonRed
                )
            ) {
                Text("Cancel")
            }
        }
    )
}