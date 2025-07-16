package com.example.attendancetaker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringEventDao {

    @Query("SELECT * FROM recurring_events ORDER BY name ASC")
    fun getAllRecurringEvents(): Flow<List<RecurringEvent>>

    @Query("SELECT * FROM recurring_events WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveRecurringEvents(): Flow<List<RecurringEvent>>

    @Query("SELECT * FROM recurring_events WHERE id = :recurringEventId")
    suspend fun getRecurringEventById(recurringEventId: String): RecurringEvent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringEvent(recurringEvent: RecurringEvent)

    @Update
    suspend fun updateRecurringEvent(recurringEvent: RecurringEvent)

    @Delete
    suspend fun deleteRecurringEvent(recurringEvent: RecurringEvent)

    @Query("DELETE FROM recurring_events WHERE id = :recurringEventId")
    suspend fun deleteRecurringEventById(recurringEventId: String)
}