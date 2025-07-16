package com.example.attendancetaker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Entity(tableName = "recurring_events")
data class RecurringEvent(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val time: LocalTime,
    val dayOfWeek: DayOfWeek,
    val contactGroupIds: List<String> = emptyList(),
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null, // null means never ends
    val isActive: Boolean = true
)