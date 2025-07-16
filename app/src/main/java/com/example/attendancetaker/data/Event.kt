package com.example.attendancetaker.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

data class Event(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val contactGroupIds: List<String> = emptyList(),
    val recurringEventId: String? = null // ID of the recurring event template that created this event
) {
    val dateTime: LocalDateTime = LocalDateTime.of(date, time)
    val isGeneratedFromRecurring: Boolean = recurringEventId != null
}