package com.example.attendancetaker.screens.contacts

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.attendancetaker.R
import com.example.attendancetaker.data.repository.AttendanceRepository
import com.example.attendancetaker.data.entity.Contact
import com.example.attendancetaker.data.entity.ContactGroup
import com.example.attendancetaker.ui.components.AppIconButton
import com.example.attendancetaker.ui.components.AppIconButtonStyle
import com.example.attendancetaker.ui.components.AppList
import com.example.attendancetaker.ui.components.AppListItem
import com.example.attendancetaker.ui.components.AppToolbar

@Composable
fun ContactGroupDetailsScreen(
    groupId: String,
    repository: AttendanceRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var group by remember { mutableStateOf<ContactGroup?>(null) }
    var contacts by remember { mutableStateOf(emptyList<Contact>()) }

    // Load group and contacts data
    LaunchedEffect(groupId) {
        group = repository.getContactGroup(groupId)
        if (group == null) {
            onNavigateBack()
            return@LaunchedEffect
        }
        contacts = repository.getContactsFromGroups(listOf(group!!.id))
    }

    if (group == null) {
        return
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // App Toolbar with group info
        AppToolbar(
            title = group!!.name,
            subtitle = if (group!!.description.isNotEmpty()) {
                "${group!!.description} • ${contacts.size} members"
            } else {
                "${contacts.size} members"
            },
            onNavigationClick = onNavigateBack
        )

        // Contacts List
        AppList(
            items = contacts,
            onItemToListItem = { contact ->
                AppListItem(
                    id = contact.id,
                    title = contact.name,
                    subtitle = contact.phoneNumber,
                    content = {
                        ContactWhatsAppActions(
                            contact = contact,
                            context = context
                        )
                    }
                )
            },
            showSearch = true,
            emptyStateMessage = stringResource(R.string.no_contacts_in_group),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun ContactWhatsAppActions(
    contact: Contact,
    context: Context
) {
    Spacer(modifier = Modifier.height(16.dp))

    // WhatsApp action buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Message button
        AppIconButton(
            style = AppIconButtonStyle.ROUNDED_ICON_ONLY,
            onClick = { openWhatsAppMessage(context, contact.phoneNumber) },
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Whatsapp,
            backgroundColor = Color(0xFF25D366), // WhatsApp green
            contentColor = Color.White,
            contentDescription = "Send WhatsApp Message",
            iconSize = 22.dp,
            verticalPadding = 0.dp,
        )

        // Call button
        AppIconButton(
            style = AppIconButtonStyle.ROUNDED_ICON_ONLY,
            onClick = { openWhatsAppCall(context, contact.phoneNumber) },
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Call,
            backgroundColor = Color(0xFF0B5D9C),
            contentColor = Color.White,
            contentDescription = "WhatsApp Call",
            iconSize = 22.dp,
            verticalPadding = 0.dp,
        )
    }
}

private fun openWhatsAppMessage(context: Context, phoneNumber: String) {
    try {
        // Clean the phone number (remove any non-numeric characters except +)
        val cleanedNumber = phoneNumber.replace(Regex("[^+\\d]"), "")

        // Try to open WhatsApp directly
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/$cleanedNumber")
            setPackage("com.whatsapp")
        }

        // Check if WhatsApp is installed
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback to web WhatsApp
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$cleanedNumber")
            }
            context.startActivity(webIntent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        // Fallback to regular SMS
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")
        }
        try {
            context.startActivity(smsIntent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}

private fun openWhatsAppCall(context: Context, phoneNumber: String) {
    try {
        // Clean the phone number
        val cleanedNumber = phoneNumber.replace(Regex("[^+\\d]"), "")

        // Try to open WhatsApp call directly
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/$cleanedNumber?call")
            setPackage("com.whatsapp")
        }

        // Check if WhatsApp is installed
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback to regular phone call
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            context.startActivity(callIntent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        // Fallback to regular phone call
        val callIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        try {
            context.startActivity(callIntent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}