package com.example.attendancetaker.screens.contacts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.example.attendancetaker.data.repository.AttendanceRepository
import com.example.attendancetaker.data.entity.Contact
import com.example.attendancetaker.data.entity.ContactGroup
import com.example.attendancetaker.utils.ContactUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactSelectionScreen(
    groupId: String?,
    repository: AttendanceRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var group by remember { mutableStateOf<ContactGroup?>(null) }
    var selectedContactIds by remember { mutableStateOf(emptySet<String>()) }
    var searchQuery by remember { mutableStateOf("") }
    var allAvailableContacts by remember { mutableStateOf(emptyList<Contact>()) }

    // Load group data if editing existing group
    LaunchedEffect(groupId) {
        if (groupId != null && groupId != "new") {
            group = repository.getContactGroup(groupId)
            selectedContactIds = group?.contactIds?.toSet() ?: emptySet()
        }
    }

    // Load available contacts from phone
    LaunchedEffect(Unit) {
        allAvailableContacts = ContactUtils.getPhoneContacts(context)
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

    // Save data when the screen is disposed (user navigates back)
    DisposableEffect(Unit) {
        onDispose {
            coroutineScope.launch {
                if (group != null) {
                    // Update existing group
                    val updatedGroup = group!!.copy(
                        contactIds = selectedContactIds.toList()
                    )
                    repository.updateContactGroup(updatedGroup)

                    // Add new contacts to repository if they don't exist
                    selectedContactIds.forEach { contactId ->
                        val existingContact = repository.getContactById(contactId)
                        if (existingContact == null) {
                            val availableContact = allAvailableContacts.find { it.id == contactId }
                            availableContact?.let { repository.addContact(it) }
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(stringResource(R.string.search_contacts)) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = stringResource(R.string.clear_search)
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Selected count
        Text(
            text = stringResource(R.string.contacts_selected, selectedContactIds.size),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredContacts.isEmpty()) {
            if (allAvailableContacts.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_contacts_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                Text(
                    text = stringResource(R.string.no_contacts_match_search),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        } else {
            // Contact list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
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

@Composable
fun ContactSelectionItem(
    contact: Contact,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectionChanged(!isSelected) },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
        border = if (isSelected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
        }
    }
}