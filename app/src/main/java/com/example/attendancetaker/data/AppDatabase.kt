package com.example.attendancetaker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.attendancetaker.data.dao.AttendanceRecordDao
import com.example.attendancetaker.data.dao.ContactDao
import com.example.attendancetaker.data.dao.ContactGroupDao
import com.example.attendancetaker.data.dao.EventDao
import com.example.attendancetaker.data.entity.AttendanceRecord
import com.example.attendancetaker.data.entity.Contact
import com.example.attendancetaker.data.entity.ContactGroup
import com.example.attendancetaker.data.entity.Event
import com.example.attendancetaker.utils.Converters

@Database(
    entities = [
        Contact::class,
        ContactGroup::class,
        Event::class,
        AttendanceRecord::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun contactGroupDao(): ContactGroupDao
    abstract fun eventDao(): EventDao
    abstract fun attendanceRecordDao(): AttendanceRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to events table for recurring functionality
                database.execSQL("ALTER TABLE events ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE events ADD COLUMN dayOfWeek TEXT")
                database.execSQL("ALTER TABLE events ADD COLUMN startDate TEXT")
                database.execSQL("ALTER TABLE events ADD COLUMN endDate TEXT")
                database.execSQL("ALTER TABLE events ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")

                // Make date column nullable by creating new table and copying data
                database.execSQL("""
                    CREATE TABLE events_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        date TEXT,
                        time TEXT NOT NULL,
                        isRecurring INTEGER NOT NULL DEFAULT 0,
                        dayOfWeek TEXT,
                        startDate TEXT,
                        endDate TEXT,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        contactGroupIds TEXT NOT NULL,
                        recurringEventId TEXT
                    )
                """)

                database.execSQL("""
                    INSERT INTO events_new (id, name, description, date, time, contactGroupIds, recurringEventId)
                    SELECT id, name, description, date, time, contactGroupIds, recurringEventId FROM events
                """)

                database.execSQL("DROP TABLE events")
                database.execSQL("ALTER TABLE events_new RENAME TO events")

                // Migrate recurring_events to events table as recurring events
                database.execSQL("""
                    INSERT INTO events (id, name, description, time, isRecurring, dayOfWeek, startDate, endDate, isActive, contactGroupIds)
                    SELECT id, name, description, time, 1, dayOfWeek, startDate, endDate, isActive, contactGroupIds FROM recurring_events
                """)

                // Drop the recurring_events table
                database.execSQL("DROP TABLE IF EXISTS recurring_events")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "attendance_database"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}