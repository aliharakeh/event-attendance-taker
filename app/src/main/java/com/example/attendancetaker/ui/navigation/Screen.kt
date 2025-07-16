package com.example.attendancetaker.ui.navigation

sealed class Screen(val route: String) {
    object Contacts : Screen("contacts")
    object Events : Screen("events")
    object AttendanceList : Screen("attendance/{eventId}") {
        fun createRoute(eventId: String) = "attendance/$eventId"
    }
    object ContactGroupEdit : Screen("contact_group_edit/{groupId}") {
        fun createRoute(groupId: String) = "contact_group_edit/$groupId"
        fun createRouteForNew() = "contact_group_edit/new"
    }
    object ContactGroupDetails : Screen("contact_group_details/{groupId}") {
        fun createRoute(groupId: String) = "contact_group_details/$groupId"
    }
    object EventEdit : Screen("event_edit/{eventId}") {
        fun createRoute(eventId: String) = "event_edit/$eventId"
        fun createRouteForNew() = "event_edit/new"
    }
}