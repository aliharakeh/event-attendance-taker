package com.example.attendancetaker.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.attendancetaker.R
import com.example.attendancetaker.ui.theme.ButtonNeutral

@Composable
fun AppNotesDialog(
    isVisible: Boolean,
    title: String,
    initialNotes: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    saveButtonText: String = stringResource(R.string.save),
    cancelButtonText: String = stringResource(R.string.cancel),
    placeholder: String = "Enter notes..."
) {
    if (isVisible) {
        var notes by remember { mutableStateOf(initialNotes) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                AppTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes",
                    placeholder = placeholder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    singleLine = false,
                    maxLines = 5
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSave(notes)
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(saveButtonText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = ButtonNeutral
                    )
                ) {
                    Text(cancelButtonText)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    }
}