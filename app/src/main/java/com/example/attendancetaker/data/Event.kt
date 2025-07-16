package com.example.attendancetaker.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

@Entity(tableName = "events")
data class Event(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val contactGroupIds: List<String> = emptyList(),
    val recurringEventId: String? = null // ID of the recurring event template that created this event
) {
    @get:Ignore
    val dateTime: LocalDateTime
        get() = LocalDateTime.of(date, time)

    @get:Ignore
    val isGeneratedFromRecurring: Boolean
        get() = recurringEventId != null
}