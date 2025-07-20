package com.example.attendancetaker.data.repository

import android.content.Context
import com.example.attendancetaker.data.AppDatabase
import com.example.attendancetaker.data.entity.AttendanceRecord
import com.example.attendancetaker.data.entity.Contact
import com.example.attendancetaker.data.entity.ContactGroup
import com.example.attendancetaker.data.entity.Event
import com.example.attendancetaker.utils.ContactUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class AttendanceRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val contactDao = database.contactDao()
    private val contactGroupDao = database.contactGroupDao()
    private val eventDao = database.eventDao()
    private val attendanceRecordDao = database.attendanceRecordDao()

    // Contact management
    fun getAllContacts(): Flow<List<Contact>> = contactDao.getAllContacts()

    suspend fun addContact(contact: Contact) {
        contactDao.insertContact(contact)
    }

    suspend fun removeContact(contactId: String) {
        // Remove contact from all groups first
        val groups = contactGroupDao.getAllContactGroups().first()
        groups.forEach { group ->
            if (group.contactIds.contains(contactId)) {
                val updatedGroup = group.copy(
                    contactIds = group.contactIds.filter { it != contactId }
                )
                contactGroupDao.updateContactGroup(updatedGroup)
            }
        }

        // Remove all attendance records for this contact
        attendanceRecordDao.deleteAttendanceByContactId(contactId)

        // Remove the contact
        contactDao.deleteContactById(contactId)
    }

    suspend fun updateContact(contact: Contact) {
        contactDao.updateContact(contact)
    }

    suspend fun getContactById(contactId: String): Contact? {
        return contactDao.getContactById(contactId)
    }

    /**
     * Sync all existing contacts in contact groups with phone contacts
     * Only updates names for contacts that already exist in the database
     */
    suspend fun syncContactNamesWithPhone() {
        if (!ContactUtils.hasContactsPermission(context)) {
            return
        }

        // Get all contacts that are actually used in contact groups
        val allGroups = contactGroupDao.getAllContactGroups().first()
        val usedContactIds = allGroups.flatMap { it.contactIds }.toSet()

        if (usedContactIds.isEmpty()) {
            return
        }

        // Get the actual contact objects for these IDs
        val usedContacts = contactDao.getContactsByIds(usedContactIds.toList())

        // Sync their names with phone contacts
        val syncedContacts = ContactUtils.syncContactNamesWithPhone(context, usedContacts)

        // Update contacts that have changed names
        syncedContacts.forEach { syncedContact ->
            val originalContact = usedContacts.find { it.id == syncedContact.id }
            if (originalContact != null && originalContact.name != syncedContact.name) {
                contactDao.updateContact(syncedContact)
            }
        }
    }

    // Contact group management
    fun getAllContactGroups(): Flow<List<ContactGroup>> = contactGroupDao.getAllContactGroups()

    suspend fun addContactGroup(contactGroup: ContactGroup) {
        contactGroupDao.insertContactGroup(contactGroup)
    }

    suspend fun removeContactGroup(groupId: String) {
        // Remove group from all events first
        val events = eventDao.getAllEvents().first()
        events.forEach { event ->
            if (event.contactGroupIds.contains(groupId)) {
                val updatedEvent = event.copy(
                    contactGroupIds = event.contactGroupIds.filter { it != groupId }
                )
                eventDao.updateEvent(updatedEvent)
            }
        }

        contactGroupDao.deleteContactGroupById(groupId)
    }

    suspend fun updateContactGroup(contactGroup: ContactGroup) {
        contactGroupDao.updateContactGroup(contactGroup)
    }

    suspend fun getContactGroup(groupId: String): ContactGroup? {
        return contactGroupDao.getContactGroupById(groupId)
    }

    // Event management (now handles both regular and recurring events)
    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()

    fun getRegularEvents(): Flow<List<Event>> = eventDao.getRegularEvents()

    fun getRecurringEvents(): Flow<List<Event>> = eventDao.getRecurringEvents()

    fun getActiveRecurringEvents(): Flow<List<Event>> = eventDao.getActiveRecurringEvents()

    fun getPastEvents(): Flow<List<Event>> = eventDao.getPastEvents(LocalDate.now())

    fun getPastEventsInDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Event>> =
        eventDao.getPastEventsInDateRange(LocalDate.now(), startDate, endDate)

    fun getCurrentAndFutureEvents(): Flow<List<Event>> = eventDao.getCurrentAndFutureEvents(LocalDate.now())

    suspend fun addEvent(event: Event) {
        eventDao.insertEvent(event)
    }

    suspend fun removeEvent(eventId: String) {
        // Remove all attendance records for this event
        attendanceRecordDao.deleteAttendanceByEventId(eventId)

        eventDao.deleteEventById(eventId)
    }

    suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event)
    }

    suspend fun getEventById(eventId: String): Event? {
        return eventDao.getEventById(eventId)
    }

    suspend fun getEventsBetweenDates(startDate: LocalDate, endDate: LocalDate): List<Event> {
        return eventDao.getEventsBetweenDates(startDate, endDate)
    }

    // Helper functions for contact filtering
    suspend fun getContactsForEvent(eventId: String): List<Contact> {
        val event = eventDao.getEventById(eventId) ?: return emptyList()
        return getContactsFromGroups(event.contactGroupIds)
    }

    suspend fun getContactsFromGroups(groupIds: List<String>): List<Contact> {
        val allContactIds = mutableSetOf<String>()

        groupIds.forEach { groupId ->
            val group = contactGroupDao.getContactGroupById(groupId)
            group?.contactIds?.forEach { contactId ->
                allContactIds.add(contactId)
            }
        }

        return if (allContactIds.isNotEmpty()) {
            contactDao.getContactsByIds(allContactIds.toList())
        } else {
            emptyList()
        }
    }

    suspend fun getGroupsContainingContact(contactId: String): List<ContactGroup> {
        return contactGroupDao.getAllContactGroups().first().filter { group ->
            group.contactIds.contains(contactId)
        }
    }

    // Attendance management
    suspend fun getAttendanceRecord(eventId: String, contactId: String): AttendanceRecord? {
        return attendanceRecordDao.getAttendanceRecord(eventId, contactId)
    }

    suspend fun updateAttendanceRecord(record: AttendanceRecord) {
        attendanceRecordDao.insertAttendanceRecord(record)
    }

    fun getAttendanceForEvent(eventId: String): Flow<List<AttendanceRecord>> {
        return attendanceRecordDao.getAttendanceForEvent(eventId)
    }

    suspend fun getAttendanceForContact(contactId: String): List<AttendanceRecord> {
        return attendanceRecordDao.getAttendanceForContact(contactId)
    }

    // Helper functions for recurring events
    suspend fun hasEventForRecurringEvent(recurringEventId: String, date: LocalDate): Boolean {
        return eventDao.getEventByRecurringEventAndDate(recurringEventId, date) != null
    }

    suspend fun createEventFromRecurring(recurringEvent: Event, date: LocalDate): Event {
        val event = Event(
            name = recurringEvent.name,
            description = recurringEvent.description,
            date = date,
            time = recurringEvent.time,
            contactGroupIds = recurringEvent.contactGroupIds,
            recurringEventId = recurringEvent.id
        )
        eventDao.insertEvent(event)
        return event
    }
}