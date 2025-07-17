package com.example.attendancetaker.utils

import com.example.attendancetaker.data.AttendanceRepository
import com.example.attendancetaker.data.Event
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate

object RecurringEventManager {

    /**
     * Checks all active recurring events and creates any missing events for today
     */
    suspend fun createTodaysRecurringEvents(repository: AttendanceRepository) {
        val today = LocalDate.now()
        val todayDayOfWeek = today.dayOfWeek

        val activeRecurringEvents = repository.getActiveRecurringEvents().first()

        activeRecurringEvents.forEach { recurringEvent ->
            // Check if this recurring event should create an event today
            if (shouldCreateEventForDay(recurringEvent, today, todayDayOfWeek)) {
                // Check if we already have an event for today from this recurring event
                if (!repository.hasEventForRecurringEvent(recurringEvent.id, today)) {
                    // Create the event
                    repository.createEventFromRecurring(recurringEvent, today)
                }
            }
        }
    }

    /**
     * Creates missing recurring events for a date range (useful for missed days when app wasn't opened)
     */
    suspend fun createRecurringEventsForDateRange(
        repository: AttendanceRepository,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        val activeRecurringEvents = repository.getActiveRecurringEvents().first()

        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            activeRecurringEvents.forEach { recurringEvent ->
                if (shouldCreateEventForDay(recurringEvent, currentDate, currentDate.dayOfWeek)) {
                    if (!repository.hasEventForRecurringEvent(recurringEvent.id, currentDate)) {
                        repository.createEventFromRecurring(recurringEvent, currentDate)
                    }
                }
            }
            currentDate = currentDate.plusDays(1)
        }
    }

    /**
     * Determines if a recurring event should create an event for a specific day
     */
    private fun shouldCreateEventForDay(
        recurringEvent: Event,
        date: LocalDate,
        dayOfWeek: DayOfWeek
    ): Boolean {
        // Check if the recurring event is for this day of the week
        if (recurringEvent.dayOfWeek != dayOfWeek) {
            return false
        }

        // Check if the date is before the start date
        val startDate = recurringEvent.startDate ?: return false
        if (date.isBefore(startDate)) {
            return false
        }

        // Check if the date is after the end date (if one is set)
        val endDate = recurringEvent.endDate
        if (endDate != null && date.isAfter(endDate)) {
            return false
        }

        // Check if the recurring event is active
        return recurringEvent.isActive
    }
}