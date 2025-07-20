package com.example.attendancetaker.utils

import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.attendancetaker.data.entity.Contact
import java.security.MessageDigest

object ContactUtils {

    /**
     * Generate a consistent ID for a contact based on their phone number
     */
    private fun generateContactId(phoneNumber: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val hash = digest.digest(phoneNumber.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Check if the app has permission to read contacts
     */
    fun hasContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get all contacts from the phone's contact list
     * Note: This requires READ_CONTACTS permission
     */
    fun getPhoneContacts(context: Context): List<Contact> {
        if (!hasContactsPermission(context)) {
            return emptyList()
        }

        val contacts = mutableListOf<Contact>()
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val name = it.getString(0) ?: continue
                val phone = it.getString(1) ?: continue

                // Clean up phone number
                val cleanPhone = phone.replace(Regex("[^\\d+]"), "")
                if (cleanPhone.isNotEmpty()) {
                    contacts.add(
                        Contact(
                            id = generateContactId(cleanPhone),
                            name = name,
                            phoneNumber = cleanPhone
                        )
                    )
                }
            }
        }

        // Remove duplicates based on phone number
        return contacts.distinctBy { it.phoneNumber }
    }

    /**
     * Filter and merge phone contacts with existing contacts
     * This helps avoid duplicates when importing from phone
     */
    fun mergeContactLists(
        existingContacts: List<Contact>,
        phoneContacts: List<Contact>
    ): List<Contact> {
        val existingPhoneNumbers =
            existingContacts.map { it.phoneNumber.replace(Regex("[^\\d+]"), "") }.toSet()

        val newContacts = phoneContacts.filter { phoneContact ->
            val cleanPhone = phoneContact.phoneNumber.replace(Regex("[^\\d+]"), "")
            !existingPhoneNumbers.contains(cleanPhone)
        }

        return existingContacts + newContacts
    }

    /**
     * Sync existing contacts with phone contacts to update names
     * This only updates existing contacts, doesn't add new ones
     */
    fun syncContactNamesWithPhone(
        context: Context,
        existingContacts: List<Contact>
    ): List<Contact> {
        if (!hasContactsPermission(context) || existingContacts.isEmpty()) {
            return existingContacts
        }

        val phoneContacts = getPhoneContacts(context)
        val phoneContactMap = phoneContacts.associateBy { contact ->
            contact.phoneNumber.replace(Regex("[^\\d+]"), "")
        }

        return existingContacts.map { existingContact ->
            val cleanExistingPhone = existingContact.phoneNumber.replace(Regex("[^\\d+]"), "")
            val phoneContact = phoneContactMap[cleanExistingPhone]

            if (phoneContact != null && phoneContact.name != existingContact.name) {
                // Update the contact name from phone
                existingContact.copy(name = phoneContact.name)
            } else {
                // Keep existing contact unchanged
                existingContact
            }
        }
    }
}