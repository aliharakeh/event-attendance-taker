package com.example.attendancetaker.screens.events

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.attendancetaker.data.entity.ContactGroup
import com.example.attendancetaker.data.entity.Event

class EventState : ViewModel() {
    var selectedGroups by mutableStateOf<List<ContactGroup>>(emptyList())
        private set

    var selectedGroupIds by mutableStateOf<Set<String>>(emptySet())
        private set

    var eventName by mutableStateOf("")
    var eventDescription by mutableStateOf("")
    var eventDate by mutableStateOf(java.time.LocalDate.now())
    var eventTime by mutableStateOf(java.time.LocalTime.now())
    var isRecurring by mutableStateOf(false)
    var recurringEndDate by mutableStateOf<java.time.LocalDate?>(null)
    var hasEndDate by mutableStateOf(false)
    var showDatePicker by mutableStateOf(false)
    var showTimePicker by mutableStateOf(false)
    var showRecurringEndDatePicker by mutableStateOf(false)

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

    fun updateSelectedGroups(groups: List<ContactGroup>) {
        groups.forEach { addGroup(it) }
    }

    fun initializeFromEvent(event: Event) {
        eventName = event.name
        eventDescription = event.description
        if (event.isRecurring) {
            isRecurring = true
            eventTime = event.time
            eventDate = event.startDate ?: java.time.LocalDate.now()
            recurringEndDate = event.endDate
            hasEndDate = event.endDate != null
        } else {
            eventDate = event.date ?: java.time.LocalDate.now()
            eventTime = event.time
            isRecurring = false
            recurringEndDate = null
            hasEndDate = false
        }
    }

    fun clearState() {
        selectedGroups = emptyList()
        selectedGroupIds = emptySet()
        eventName = ""
        eventDescription = ""
        eventDate = java.time.LocalDate.now()
        eventTime = java.time.LocalTime.now()
        isRecurring = false
        recurringEndDate = null
        hasEndDate = false
        showDatePicker = false
        showTimePicker = false
        showRecurringEndDatePicker = false
    }
}