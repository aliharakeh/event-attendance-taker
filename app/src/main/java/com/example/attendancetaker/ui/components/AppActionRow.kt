package com.example.attendancetaker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.attendancetaker.ui.theme.ButtonBlue
import com.example.attendancetaker.ui.theme.ButtonRed
import com.example.attendancetaker.ui.theme.EditIconBlue

/**
 * Data class representing a configurable action with its properties
 */
data class ActionItem(
    val icon: ImageVector,
    val contentDescription: String,
    val tint: Color,
    val onClick: () -> Unit,
    val enabled: Boolean = true
)

/**
 * A configurable row of action buttons (edit, delete, etc.)
 *
 * @param modifier Modifier for styling the row
 * @param actions List of ActionItem objects to display
 * @param arrangement How to arrange the actions horizontally
 * @param showEditAction Whether to show the default edit action (if onEdit is provided)
 * @param showDeleteAction Whether to show the default delete action (if onDelete is provided)
 * @param onEdit Callback for edit action (creates default edit action if provided)
 * @param onDelete Callback for delete action (creates default delete action if provided)
 */
@Composable
fun AppActionRow(
    modifier: Modifier = Modifier,
    actions: List<ActionItem> = emptyList(),
    arrangement: Arrangement.Horizontal = Arrangement.spacedBy(4.dp),
    showEditAction: Boolean = true,
    showDeleteAction: Boolean = true,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Row(
        modifier = modifier,
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Render custom actions first
        actions.forEach { action ->
            IconButton(
                onClick = action.onClick,
                enabled = action.enabled
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.contentDescription,
                    tint = if (action.enabled) action.tint else action.tint.copy(alpha = 0.38f)
                )
            }
        }

        // Default edit action
        if (showEditAction && onEdit != null) {
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = EditIconBlue
                )
            }
        }

        // Default delete action
        if (showDeleteAction && onDelete != null) {
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = ButtonRed
                )
            }
        }
    }
}

/**
 * Convenience function to create common action configurations
 */
object ActionPresets {

    /**
     * Creates a standard edit action item
     */
    fun addAction(
        onClick: () -> Unit,
        enabled: Boolean = true
    ) = ActionItem(
        icon = Icons.Default.Add,
        contentDescription = "Add",
        tint = ButtonBlue,
        onClick = onClick,
        enabled = enabled
    )

    fun editAction(
        onClick: () -> Unit,
        enabled: Boolean = true
    ) = ActionItem(
        icon = Icons.Default.Edit,
        contentDescription = "Edit",
        tint = ButtonBlue,
        onClick = onClick,
        enabled = enabled
    )
}