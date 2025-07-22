package com.example.attendancetaker.screens.contacts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.attendancetaker.data.entity.Contact

class ContactSelectionViewModel : ViewModel() {
    var selectedContacts by mutableStateOf<List<Contact>>(emptyList())
        private set

    var selectedContactIds by mutableStateOf<Set<String>>(emptySet())
        private set

    fun updateSelectedContacts(contacts: List<Contact>) {
        selectedContacts = contacts
        selectedContactIds = contacts.map { it.id }.toSet()
    }

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

    fun toggleContact(contact: Contact) {
        if (selectedContactIds.contains(contact.id)) {
            removeContact(contact.id)
        } else {
            addContact(contact)
        }
    }

    fun clearSelection() {
        selectedContacts = emptyList()
        selectedContactIds = emptySet()
    }
}