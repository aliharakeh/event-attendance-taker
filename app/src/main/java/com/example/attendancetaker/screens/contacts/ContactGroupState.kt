package com.example.attendancetaker.screens.contacts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.attendancetaker.data.entity.Contact

class ContactGroupState : ViewModel() {
    var selectedContacts by mutableStateOf<List<Contact>>(emptyList())
        private set

    var selectedContactIds by mutableStateOf<Set<String>>(emptySet())
        private set

    var hasState by mutableStateOf(false)

    var groupName by mutableStateOf("")

    var groupDescription by mutableStateOf("")

    fun addContact(contact: Contact) {
        if (!selectedContactIds.contains(contact.id)) {
            selectedContacts = selectedContacts + contact
            selectedContactIds = selectedContactIds + contact.id
        }
    }

    fun removeContact(contactId: String) {
        selectedContacts = selectedContacts.filter { it.id != contactId }
        selectedContactIds = selectedContactIds - contactId
    }

    fun updateSelectedContacts(contacts: List<Contact>) {
        selectedContacts = contacts
        selectedContactIds = contacts.map { it.id }.toSet()
    }

    fun toggleContact(contact: Contact) {
        if (selectedContactIds.contains(contact.id)) {
            removeContact(contact.id)
        } else {
            addContact(contact)
        }
    }

    fun clearState() {
        selectedContacts = emptyList()
        selectedContactIds = emptySet()
        hasState = false
        groupName = ""
        groupDescription = ""
    }
}