package com.example.attendancetaker.ui.navigation

sealed class Screen(val route: String) {
    object Contacts : Screen("contacts")
    object Events : Screen("events")
    object AttendanceList : Screen("attendance/{eventId}") {
        fun createRoute(eventId: String) = "attendance/$eventId"
    }
}