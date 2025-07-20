package com.example.attendancetaker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.example.attendancetaker.data.AttendanceRepository
import com.example.attendancetaker.data.Contact
import com.example.attendancetaker.data.ContactGroup
import com.example.attendancetaker.data.Event
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
    var event by remember { mutableStateOf<Event?>(null) }
    var selectedGroupIds by remember { mutableStateOf(emptySet<String>()) }
    var searchQuery by remember { mutableStateOf("") }
    var contactsForGroups by remember { mutableStateOf(mapOf<String, List<Contact>>()) }

    val allContactGroups by repository.getAllContactGroups().collectAsState(initial = emptyList())

    // Load event data if editing existing event
    LaunchedEffect(eventId) {
        if (eventId != null && eventId != "new") {
            event = repository.getEventById(eventId)
            selectedGroupIds = event?.contactGroupIds?.toSet() ?: emptySet()
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

    // Filter contact groups based on search query
    val filteredGroups = remember(allContactGroups, searchQuery) {
        if (searchQuery.isBlank()) {
            allContactGroups
        } else {
            allContactGroups.filter { group ->
                group.name.contains(searchQuery, ignoreCase = true) ||
                        group.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Save data when the screen is disposed (user navigates back)
    DisposableEffect(Unit) {
        onDispose {
            coroutineScope.launch {
                if (event != null) {
                    // Update existing event
                    val updatedEvent = event!!.copy(
                        contactGroupIds = selectedGroupIds.toList()
                    )
                    repository.updateEvent(updatedEvent)
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
            label = { Text(stringResource(R.string.search_contact_groups)) },
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
            text = stringResource(R.string.contact_groups_selected, selectedGroupIds.size),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredGroups.isEmpty()) {
            if (allContactGroups.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_contact_groups_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                Text(
                    text = stringResource(R.string.no_contact_groups_match_search),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        } else {
            // Contact groups list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredGroups) { group ->
                    ContactGroupSelectionItem(
                        group = group,
                        contacts = contactsForGroups[group.id] ?: emptyList(),
                        isSelected = selectedGroupIds.contains(group.id),
                        onSelectionChanged = { isSelected ->
                            selectedGroupIds = if (isSelected) {
                                selectedGroupIds + group.id
                            } else {
                                selectedGroupIds - group.id
                            }
                        }
                    )
                }
            }
        }
    }
}