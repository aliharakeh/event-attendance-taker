package com.example.attendancetaker.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.attendancetaker.R
import com.example.attendancetaker.data.AttendanceRepository
import com.example.attendancetaker.data.Contact
import com.example.attendancetaker.ui.theme.ButtonBlue
import com.example.attendancetaker.ui.theme.ButtonRed
import com.example.attendancetaker.ui.theme.EditIconBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    repository: AttendanceRepository,
    showAddDialog: Boolean,
    onAddDialogDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingContact by remember { mutableStateOf<Contact?>(null) }

    // Contacts List
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(repository.contacts) { contact ->
            ContactItem(
                contact = contact,
                onEdit = { editingContact = contact },
                onDelete = { repository.removeContact(contact.id) }
            )
        }
    }

    // Add/Edit Dialog
    if (showAddDialog) {
        ContactDialog(
            contact = null,
            onDismiss = onAddDialogDismiss,
            onSave = { contact ->
                repository.addContact(contact)
                onAddDialogDismiss()
            }
        )
    }

    editingContact?.let { contact ->
        ContactDialog(
            contact = contact,
            onDismiss = { editingContact = null },
            onSave = { updatedContact ->
                repository.updateContact(updatedContact)
                editingContact = null
            }
        )
    }
}

@Composable
fun ContactItem(
    contact: Contact,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = contact.phoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit),
                            tint = EditIconBlue
                        )
                    }
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = Color(0xFFE53E3E)
                        )
                    }
                }
            }

            // WhatsApp Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { openWhatsAppMessage(context, contact.phoneNumber) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366) // WhatsApp green
                    )
                ) {
                    Icon(
                        Icons.Default.Message,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.whatsapp_message),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = { openWhatsAppCall(context, contact.phoneNumber) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366) // WhatsApp green
                    )
                ) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.whatsapp_call),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.delete_contact)) },
            text = { Text(stringResource(R.string.delete_contact_confirmation, contact.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = ButtonRed // Red for delete button
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = ButtonRed // Red for cancel button
                    )
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDialog(
    contact: Contact?,
    onDismiss: () -> Unit,
    onSave: (Contact) -> Unit
) {
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var phoneNumber by remember { mutableStateOf(contact?.phoneNumber ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (contact == null) stringResource(R.string.add_contact_title) else stringResource(R.string.edit_contact_title))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text(stringResource(R.string.phone_number)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && phoneNumber.isNotBlank()) {
                        val newContact = if (contact == null) {
                            Contact(name = name.trim(), phoneNumber = phoneNumber.trim())
                        } else {
                            contact.copy(name = name.trim(), phoneNumber = phoneNumber.trim())
                        }
                        onSave(newContact)
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ButtonBlue // Blue for save button
                )
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ButtonRed // Red for cancel button
                )
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

// Helper functions for WhatsApp functionality
private fun openWhatsAppMessage(context: Context, phoneNumber: String) {
    try {
        // Format phone number (remove any non-digit characters)
        val formattedNumber = phoneNumber.replace(Regex("[^\\d+]"), "")

        // Try to open WhatsApp with specific contact
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/$formattedNumber")
            `package` = "com.whatsapp"
        }

        // Check if WhatsApp is installed
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // WhatsApp not installed, open in browser
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$formattedNumber"))
            if (browserIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(browserIntent)
            } else {
                Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
            }
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error opening WhatsApp", Toast.LENGTH_SHORT).show()
    }
}

private fun openWhatsAppCall(context: Context, phoneNumber: String) {
    try {
        // Format phone number (remove any non-digit characters)
        val formattedNumber = phoneNumber.replace(Regex("[^\\d+]"), "")

        // Try to open WhatsApp voice call
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/$formattedNumber?action=call")
            `package` = "com.whatsapp"
        }

        // Check if WhatsApp is installed
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // WhatsApp not installed, open in browser
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$formattedNumber"))
            if (browserIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(browserIntent)
            } else {
                Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
            }
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error opening WhatsApp", Toast.LENGTH_SHORT).show()
    }
}