package com.example.attendancetaker.screens.events

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.attendancetaker.data.entity.ContactGroup

class ContactGroupSelectionViewModel : ViewModel() {
    var selectedGroups by mutableStateOf<List<ContactGroup>>(emptyList())
        private set

    var selectedGroupIds by mutableStateOf<Set<String>>(emptySet())
        private set

    var eventGroupsAdded by mutableStateOf(false)

    fun addGroup(group: ContactGroup) {
        if (!selectedGroupIds.contains(group.id)) {
            selectedGroups = selectedGroups + group
            selectedGroupIds = selectedGroupIds + group.id
        }
    }

    fun removeGroup(groupId: String) {
        selectedGroups = selectedGroups.filter { it.id != groupId }
        selectedGroupIds = selectedGroupIds - groupId
    }

    fun toggleGroup(group: ContactGroup) {
        if (selectedGroupIds.contains(group.id)) {
            removeGroup(group.id)
        } else {
            addGroup(group)
        }
    }

    fun setSelectedGroups(groups: List<ContactGroup>) {
        selectedGroups = groups
        selectedGroupIds = groups.map { it.id }.toSet()
    }

    fun clearSelection() {
        selectedGroups = emptyList()
        selectedGroupIds = emptySet()
        eventGroupsAdded = false
    }
}