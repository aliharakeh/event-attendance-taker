package com.example.attendancetaker.ui.navigation

sealed class Screen(val route: String) {
    object Contacts : Screen("contacts")
    object Events : Screen("events")
    object EventHistory : Screen("event_history")
    object RecurringTemplates : Screen("recurring_templates")
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

    object ContactSelection : Screen("contact_selection/{groupId}") {
        fun createRoute(groupId: String) = "contact_selection/$groupId"
        fun createRouteForNew() = "contact_selection/new"
    }

    object ContactGroupSelection : Screen("contact_group_selection/{eventId}") {
        fun createRoute(eventId: String) = "contact_group_selection/$eventId"
        fun createRouteForNew() = "contact_group_selection/new"
    }

    object EventEdit : Screen("event_edit/{eventId}") {
        fun createRoute(eventId: String) = "event_edit/$eventId"
        fun createRouteForNew() = "event_edit/new"
    }
}