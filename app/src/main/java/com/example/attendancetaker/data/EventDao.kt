package com.example.attendancetaker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface EventDao {

    @Query("SELECT * FROM events ORDER BY date DESC, time DESC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE isRecurring = 0 ORDER BY date DESC, time DESC")
    fun getRegularEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE isRecurring = 1 ORDER BY name ASC")
    fun getRecurringEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE isRecurring = 1 AND isActive = 1 ORDER BY name ASC")
    fun getActiveRecurringEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: String): Event?

    @Query("SELECT * FROM events WHERE recurringEventId = :recurringEventId")
    suspend fun getEventsByRecurringEventId(recurringEventId: String): List<Event>

    @Query("SELECT * FROM events WHERE recurringEventId = :recurringEventId AND date = :date")
    suspend fun getEventByRecurringEventAndDate(recurringEventId: String, date: LocalDate): Event?

    @Query("SELECT * FROM events WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, time ASC")
    suspend fun getEventsBetweenDates(startDate: LocalDate, endDate: LocalDate): List<Event>

    @Query("SELECT * FROM events WHERE date < :currentDate ORDER BY date DESC, time DESC")
    fun getPastEvents(currentDate: LocalDate): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE date < :currentDate AND date BETWEEN :startDate AND :endDate ORDER BY date DESC, time DESC")
    fun getPastEventsInDateRange(currentDate: LocalDate, startDate: LocalDate, endDate: LocalDate): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE (date >= :currentDate OR (date IS NULL AND isRecurring = 0)) AND isRecurring = 0 ORDER BY date ASC, time ASC")
    fun getCurrentAndFutureEvents(currentDate: LocalDate): Flow<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: String)

    @Query("DELETE FROM events WHERE recurringEventId = :recurringEventId")
    suspend fun deleteEventsByRecurringEventId(recurringEventId: String)
}