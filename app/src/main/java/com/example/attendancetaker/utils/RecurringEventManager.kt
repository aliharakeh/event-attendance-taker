package com.example.attendancetaker.utils

import com.example.attendancetaker.data.AttendanceRepository
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

    private fun shouldCreateEventForDay(
        recurringEvent: com.example.attendancetaker.data.RecurringEvent,
        date: LocalDate,
        dayOfWeek: DayOfWeek
    ): Boolean {
        // Check if the day of week matches
        if (recurringEvent.dayOfWeek != dayOfWeek) {
            return false
        }

        // Check if the date is after the start date
        if (date.isBefore(recurringEvent.startDate)) {
            return false
        }

        // Check if the date is before the end date (if set)
        recurringEvent.endDate?.let { endDate ->
            if (date.isAfter(endDate)) {
                return false
            }
        }

        // Check if the recurring event is active
        return recurringEvent.isActive
    }
}