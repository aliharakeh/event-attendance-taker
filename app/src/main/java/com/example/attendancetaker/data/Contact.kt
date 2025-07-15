package com.example.attendancetaker.data

import java.util.UUID

data class Contact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String
)