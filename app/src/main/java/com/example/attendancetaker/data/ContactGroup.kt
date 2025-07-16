package com.example.attendancetaker.data

import java.util.UUID

data class ContactGroup(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val contactIds: List<String> = emptyList()
)