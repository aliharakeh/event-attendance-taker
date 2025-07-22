package com.example.attendancetaker.screens.contacts

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
import com.example.attendancetaker.ui.components.AppList
import com.example.attendancetaker.ui.components.AppListItem
import com.example.attendancetaker.ui.components.AppToolbar
import com.example.attendancetaker.ui.components.ToolbarActionPresets
import com.example.attendancetaker.utils.ContactUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactSelectionScreen(
    groupId: String?,
    repository: AttendanceRepository,
    contactSelectionViewModel: ContactSelectionViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var group by remember { mutableStateOf<ContactGroup?>(null) }
    var phoneContacts by remember { mutableStateOf(emptyList<Contact>()) }
    val repositoryContacts by repository.getAllContacts().collectAsState(initial = emptyList())

    // Load group data if editing existing group
    LaunchedEffect(groupId) {
        if (groupId != null && groupId != "new") {
            group = repository.getContactGroup(groupId)
        }
    }

    // Load available contacts from phone
    LaunchedEffect(Unit) {
        phoneContacts = ContactUtils.getPhoneContacts(context)
    }

    // Combine phone contacts with repository contacts
    val allAvailableContacts = remember(phoneContacts, repositoryContacts) {
        val phoneContactMap = phoneContacts.associateBy { it.id }
        val repoContactMap = repositoryContacts.associateBy { it.id }
        // Merge both lists, prioritizing repository contacts for duplicates
        val combinedMap = phoneContactMap + repoContactMap
        combinedMap.values.toList()
    }

    // Get selected data from ViewModel
    val selectedContactIds = contactSelectionViewModel.selectedContactIds

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Toolbar
        AppToolbar(
            title = stringResource(R.string.select_contacts),
            onNavigationClick = onNavigateBack,
            actions = listOf(
                ToolbarActionPresets.saveAction(
                    onClick = {
                        // The selection is already saved in the ViewModel
                        // and will be persisted when the group is saved in ContactGroupEditScreen
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
            // Contact list using AppList component
            AppList(
                items = allAvailableContacts,
                onItemToListItem = { contact ->
                    AppListItem(
                        id = contact.id,
                        title = contact.name,
                        subtitle = contact.phoneNumber,
                        isSelected = selectedContactIds.contains(contact.id)
                    )
                },
                searchPlaceholder = stringResource(R.string.search_contacts),
                showSearch = true,
                isSelectable = true,
                isItemClickable = true,
                selectedItems = selectedContactIds,
                onSelectionChange = { contactId, isSelected ->
                    val contact = allAvailableContacts.find { it.id == contactId }
                    contact?.let {
                        contactSelectionViewModel.toggleContact(it)
                    }
                },
                emptyStateMessage = stringResource(R.string.no_contacts_available),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}