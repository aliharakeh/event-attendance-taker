package com.example.attendancetaker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    onClick: (() -> Unit) = {},
    content: @Composable () -> Unit = {},
    actions: List<ActionItem> = emptyList(),
    showEditAction: Boolean = true,
    showDeleteAction: Boolean = true,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    selected: Boolean = false,
    isClickable: Boolean = false
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val backgroundColor = when {
        isClickable && selected && isDark -> com.example.attendancetaker.ui.theme.SelectionDark
        isClickable && selected && !isDark -> com.example.attendancetaker.ui.theme.SelectionLight
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .let { if (selected) it else it }
            .then(
                if (isClickable && selected) androidx.compose.ui.Modifier.border(
                    width = 2.dp,
                    color = com.example.attendancetaker.ui.theme.SelectionBorder,
                    shape = MaterialTheme.shapes.medium
                ) else androidx.compose.ui.Modifier
            ),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with title, subtitle and actions
            if (title != null || subtitle != null || actions.isNotEmpty() || onEdit != null || onDelete != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        title?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        subtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    AppActionRow(
                        actions = actions,
                        showEditAction = showEditAction,
                        showDeleteAction = showDeleteAction,
                        onEdit = onEdit,
                        onDelete = onDelete
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content
            content()
        }
    }
}