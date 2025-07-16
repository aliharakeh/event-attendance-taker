package com.example.attendancetaker.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import java.time.LocalDateTime

class AttendanceRepository {
    private val _contacts = mutableStateListOf<Contact>()
    val contacts: List<Contact> = _contacts

    private val _contactGroups = mutableStateListOf<ContactGroup>()
    val contactGroups: List<ContactGroup> = _contactGroups

    private val _events = mutableStateListOf<Event>()
    val events: List<Event> = _events

    // Map of "eventId_contactId" to AttendanceRecord
    private val _attendanceRecords = mutableStateMapOf<String, AttendanceRecord>()

    init {
        // Add some sample data for testing
        addSampleData()
    }

    private fun addSampleData() {
        // No sample contacts - users will add them through groups

        // Sample contact groups (empty initially)
        val teamGroup = ContactGroup(
            name = "Team Members",
            description = "Core team members",
            contactIds = emptyList()
        )
        val managementGroup = ContactGroup(
            name = "Management",
            description = "Management team",
            contactIds = emptyList()
        )

        _contactGroups.add(teamGroup)
        _contactGroups.add(managementGroup)

        // Sample events (without contact groups initially)
        val sampleEvents = listOf(
            Event(
                name = "Team Meeting",
                description = "Weekly team standup meeting",
                date = LocalDateTime.now().minusDays(1).toLocalDate(),
                time = LocalDateTime.now().minusDays(1).toLocalTime(),
                contactGroupIds = emptyList()
            ),
            Event(
                name = "Project Review",
                description = "Monthly project progress review",
                date = LocalDateTime.now().plusDays(7).toLocalDate(),
                time = LocalDateTime.now().plusDays(7).toLocalTime(),
                contactGroupIds = emptyList()
            ),
            Event(
                name = "Training Session",
                description = "Professional development training",
                date = LocalDateTime.now().plusDays(14).toLocalDate(),
                time = LocalDateTime.now().plusDays(14).toLocalTime(),
                contactGroupIds = emptyList()
            )
        )

        sampleEvents.forEach { event ->
            _events.add(event)
        }
    }

    // Contact management
    fun addContact(contact: Contact) {
        _contacts.add(contact)
    }

    fun removeContact(contactId: String) {
        _contacts.removeAll { it.id == contactId }
        // Remove contact from all groups
        _contactGroups.replaceAll { group ->
            group.copy(contactIds = group.contactIds.filter { it != contactId })
        }
        // Remove all attendance records for this contact
        _attendanceRecords.entries.removeAll { it.value.contactId == contactId }
    }

    fun updateContact(contact: Contact) {
        val index = _contacts.indexOfFirst { it.id == contact.id }
        if (index != -1) {
            _contacts[index] = contact
        }
    }

    // Contact group management
    fun addContactGroup(contactGroup: ContactGroup) {
        _contactGroups.add(contactGroup)
    }

    fun removeContactGroup(groupId: String) {
        _contactGroups.removeAll { it.id == groupId }
        // Remove group from all events
        _events.replaceAll { event ->
            event.copy(contactGroupIds = event.contactGroupIds.filter { it != groupId })
        }
    }

    fun updateContactGroup(contactGroup: ContactGroup) {
        val index = _contactGroups.indexOfFirst { it.id == contactGroup.id }
        if (index != -1) {
            _contactGroups[index] = contactGroup
        }
    }

    fun getContactGroup(groupId: String): ContactGroup? {
        return _contactGroups.find { it.id == groupId }
    }

    // Event management
    fun addEvent(event: Event) {
        _events.add(event)
    }

    fun removeEvent(eventId: String) {
        _events.removeAll { it.id == eventId }
        // Remove all attendance records for this event
        _attendanceRecords.entries.removeAll { it.value.eventId == eventId }
    }

    fun updateEvent(event: Event) {
        val index = _events.indexOfFirst { it.id == event.id }
        if (index != -1) {
            _events[index] = event
        }
    }

    // Helper functions for contact filtering
    fun getContactsForEvent(eventId: String): List<Contact> {
        val event = _events.find { it.id == eventId } ?: return emptyList()
        return getContactsFromGroups(event.contactGroupIds)
    }

    fun getContactsFromGroups(groupIds: List<String>): List<Contact> {
        val allContactIds = mutableSetOf<String>()

        groupIds.forEach { groupId ->
            val group = _contactGroups.find { it.id == groupId }
            group?.contactIds?.forEach { contactId ->
                allContactIds.add(contactId)
            }
        }

        return _contacts.filter { contact -> allContactIds.contains(contact.id) }
    }

    fun getGroupsContainingContact(contactId: String): List<ContactGroup> {
        return _contactGroups.filter { group ->
            group.contactIds.contains(contactId)
        }
    }

    // Attendance management
    fun getAttendanceRecord(eventId: String, contactId: String): AttendanceRecord? {
        return _attendanceRecords["${eventId}_${contactId}"]
    }

    fun updateAttendanceRecord(record: AttendanceRecord) {
        _attendanceRecords["${record.eventId}_${record.contactId}"] = record
    }

    fun getAttendanceForEvent(eventId: String): List<AttendanceRecord> {
        return _attendanceRecords.values.filter { it.eventId == eventId }
    }

    fun getAttendanceForContact(contactId: String): List<AttendanceRecord> {
        return _attendanceRecords.values.filter { it.contactId == contactId }
    }
}