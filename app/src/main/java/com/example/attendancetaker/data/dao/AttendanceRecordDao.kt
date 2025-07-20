package com.example.attendancetaker.data.dao

import androidx.room.*
import com.example.attendancetaker.data.entity.AttendanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceRecordDao {

    @Query("SELECT * FROM attendance_records WHERE eventId = :eventId")
    fun getAttendanceForEvent(eventId: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE contactId = :contactId")
    suspend fun getAttendanceForContact(contactId: String): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records WHERE eventId = :eventId AND contactId = :contactId")
    suspend fun getAttendanceRecord(eventId: String, contactId: String): AttendanceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecord(attendanceRecord: AttendanceRecord)

    @Update
    suspend fun updateAttendanceRecord(attendanceRecord: AttendanceRecord)

    @Delete
    suspend fun deleteAttendanceRecord(attendanceRecord: AttendanceRecord)

    @Query("DELETE FROM attendance_records WHERE eventId = :eventId")
    suspend fun deleteAttendanceByEventId(eventId: String)

    @Query("DELETE FROM attendance_records WHERE contactId = :contactId")
    suspend fun deleteAttendanceByContactId(contactId: String)
}