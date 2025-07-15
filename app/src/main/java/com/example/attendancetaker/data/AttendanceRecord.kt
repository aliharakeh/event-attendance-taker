package com.example.attendancetaker.data

data class AttendanceRecord(
    val contactId: String,
    val eventId: String,
    val isPresent: Boolean = false,
    val notes: String = ""
)