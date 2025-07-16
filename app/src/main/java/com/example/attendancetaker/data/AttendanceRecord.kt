package com.example.attendancetaker.data

import androidx.room.Entity

@Entity(
    tableName = "attendance_records",
    primaryKeys = ["eventId", "contactId"]
)
data class AttendanceRecord(
    val contactId: String,
    val eventId: String,
    val isPresent: Boolean = false,
    val notes: String = ""
)