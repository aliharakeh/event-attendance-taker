package com.example.attendancetaker.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import java.time.LocalDateTime

class AttendanceRepository {
    private val _contacts = mutableStateListOf<Contact>()
    val contacts: List<Contact> = _contacts

    private val _events = mutableStateListOf<Event>()
    val events: List<Event> = _events

    // Map of "eventId_contactId" to AttendanceRecord
    private val _attendanceRecords = mutableStateMapOf<String, AttendanceRecord>()

    init {
        // Add some sample data for testing
        addSampleData()
    }

    private fun addSampleData() {
        // Sample contacts
        val sampleContacts = listOf(
            Contact(name = "John Doe", phoneNumber = "+1-555-0123"),
            Contact(name = "Jane Smith", phoneNumber = "+1-555-0124"),
            Contact(name = "Mike Johnson", phoneNumber = "+1-555-0125"),
            Contact(name = "Sarah Wilson", phoneNumber = "+1-555-0126"),
            Contact(name = "David Brown", phoneNumber = "+1-555-0127")
        )

        sampleContacts.forEach { contact ->
            _contacts.add(contact)
        }

        // Sample events
        val sampleEvents = listOf(
            Event(
                name = "Team Meeting",
                description = "Weekly team standup meeting",
                date = LocalDateTime.now().minusDays(1).toLocalDate(),
                time = LocalDateTime.now().minusDays(1).toLocalTime()
            ),
            Event(
                name = "Project Review",
                description = "Monthly project progress review",
                date = LocalDateTime.now().plusDays(7).toLocalDate(),
                time = LocalDateTime.now().plusDays(7).toLocalTime()
            ),
            Event(
                name = "Training Session",
                description = "Professional development training",
                date = LocalDateTime.now().plusDays(14).toLocalDate(),
                time = LocalDateTime.now().plusDays(14).toLocalTime()
            )
        )

        sampleEvents.forEach { event ->
            _events.add(event)
        }
    }

    fun addContact(contact: Contact) {
        _contacts.add(contact)
    }

    fun removeContact(contactId: String) {
        _contacts.removeAll { it.id == contactId }
        // Remove all attendance records for this contact
        _attendanceRecords.entries.removeAll { it.value.contactId == contactId }
    }

    fun updateContact(contact: Contact) {
        val index = _contacts.indexOfFirst { it.id == contact.id }
        if (index != -1) {
            _contacts[index] = contact
        }
    }

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