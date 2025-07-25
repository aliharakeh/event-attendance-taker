package com.example.attendancetaker.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Data class representing a toolbar action button
 */
data class ToolbarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val tint: Color? = null
)

/**
 * A flexible and reusable toolbar component for pages
 *
 * @param title The main title text displayed in the toolbar
 * @param subtitle Optional subtitle text displayed below the title
 * @param navigationIcon Optional navigation icon (defaults to back arrow if onNavigationClick is provided)
 * @param onNavigationClick Callback for navigation icon click (typically back navigation)
 * @param actions List of action buttons to display on the right side
 * @param backgroundColor Background color of the toolbar (defaults to surface color)
 * @param contentColor Content color for text and icons (defaults to onSurface color)
 * @param modifier Modifier for styling the toolbar
 * @param showNavigationIcon Whether to show the navigation icon when onNavigationClick is provided
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppToolbar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: List<ToolbarAction> = emptyList(),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    showNavigationIcon: Boolean = true
) {
    TopAppBar(
        title = {
            if (subtitle != null) {
                // Title with subtitle layout
                androidx.compose.foundation.layout.Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                // Single title layout
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            if (showNavigationIcon && onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon ?: Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back",
                        tint = contentColor
                    )
                }
            }
        },
        actions = {
            Row(
                modifier = Modifier.padding(end = 8.dp)
            ) {
                actions.forEach { action ->
                    IconButton(
                        onClick = action.onClick,
                        enabled = action.enabled
                    ) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = action.contentDescription,
                            tint = when {
                                !action.enabled -> contentColor.copy(alpha = 0.38f)
                                action.tint != null -> action.tint
                                else -> contentColor
                            }
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Convenience function to create common toolbar action configurations
 */
object ToolbarActionPresets {
    /**
     * Creates a standard add action item
     */
    fun saveAction(
        onClick: () -> Unit,
        enabled: Boolean = true
    ) = ToolbarAction(
        icon = Icons.Default.Check,
        contentDescription = "Save",
        onClick = onClick,
        enabled = enabled
    )
}