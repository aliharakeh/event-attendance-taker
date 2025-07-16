package com.example.attendancetaker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactGroupDao {

    @Query("SELECT * FROM contact_groups ORDER BY name ASC")
    fun getAllContactGroups(): Flow<List<ContactGroup>>

    @Query("SELECT * FROM contact_groups WHERE id = :groupId")
    suspend fun getContactGroupById(groupId: String): ContactGroup?

    @Query("SELECT * FROM contact_groups WHERE id IN (:groupIds)")
    suspend fun getContactGroupsByIds(groupIds: List<String>): List<ContactGroup>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContactGroup(contactGroup: ContactGroup)

    @Update
    suspend fun updateContactGroup(contactGroup: ContactGroup)

    @Delete
    suspend fun deleteContactGroup(contactGroup: ContactGroup)

    @Query("DELETE FROM contact_groups WHERE id = :groupId")
    suspend fun deleteContactGroupById(groupId: String)
}