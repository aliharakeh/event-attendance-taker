package com.example.attendancetaker.data.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

@Entity(tableName = "events")
data class Event(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",

    // For regular events: fixed date and time
    val date: LocalDate? = null,
    val time: LocalTime = LocalTime.now(),

    // For recurring events: weekday and time pattern
    val isRecurring: Boolean = false,
    val dayOfWeek: DayOfWeek? = null,
    val startDate: LocalDate? = null, // When recurring starts
    val endDate: LocalDate? = null, // When recurring ends (null = never ends)
    val isActive: Boolean = true, // For recurring events

    val contactGroupIds: List<String> = emptyList(),

    // Legacy field for events generated from old recurring events
    val recurringEventId: String? = null
) {
    @get:Ignore
    val dateTime: LocalDateTime?
        get() = date?.let { LocalDateTime.of(it, time) }

    @get:Ignore
    val isGeneratedFromRecurring: Boolean
        get() = recurringEventId != null

    @get:Ignore
    val isValidRegularEvent: Boolean
        get() = !isRecurring && date != null

    @get:Ignore
    val isValidRecurringEvent: Boolean
        get() = isRecurring && dayOfWeek != null && startDate != null
}