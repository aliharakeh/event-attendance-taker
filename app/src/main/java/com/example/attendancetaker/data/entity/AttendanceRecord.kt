package com.example.attendancetaker.data.entity

import androidx.room.Entity

enum class AttendanceStatus {
    ABSENT,
    READY,
    PRESENT
}

@Entity(
    tableName = "attendance_records",
    primaryKeys = ["eventId", "contactId"]
)
data class AttendanceRecord(
    val contactId: String,
    val eventId: String,
    val status: AttendanceStatus = AttendanceStatus.ABSENT,
    val notes: String = ""
)