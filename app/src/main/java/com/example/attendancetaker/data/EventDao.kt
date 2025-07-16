package com.example.attendancetaker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface EventDao {

    @Query("SELECT * FROM events ORDER BY date DESC, time DESC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: String): Event?

    @Query("SELECT * FROM events WHERE recurringEventId = :recurringEventId")
    suspend fun getEventsByRecurringEventId(recurringEventId: String): List<Event>

    @Query("SELECT * FROM events WHERE recurringEventId = :recurringEventId AND date = :date")
    suspend fun getEventByRecurringEventAndDate(recurringEventId: String, date: LocalDate): Event?

    @Query("SELECT * FROM events WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, time ASC")
    suspend fun getEventsBetweenDates(startDate: LocalDate, endDate: LocalDate): List<Event>

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