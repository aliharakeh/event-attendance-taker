package com.example.attendancetaker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.attendancetaker.R

/**
 * Data class representing an item in the AppList
 */
data class AppListItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val isSelected: Boolean = false,
    val content: @Composable (() -> Unit)? = null
)

/**
 * A reusable list component with search, selection, and action capabilities.
 *
 * Features:
 * - Search functionality with customizable placeholder
 * - Selectable mode with checkbox selection
 * - Editable mode with edit/delete actions
 * - Click-to-toggle selection when in selectable mode
 * - Clear all selection functionality
 * - Empty state handling
 * - Global action beside the title
 *
 * @param title The title displayed at the top of the list (optional)
 * @param items The list of items to display
 * @param onItemToListItem Function to convert items to AppListItem
 * @param modifier Modifier for styling
 * @param searchPlaceholder Placeholder text for the search field
 * @param showSearch Whether to show the search field
 * @param isSelectable Whether the list supports selection mode
 * @param selectedItems Set of selected item IDs
 * @param onSelectionChange Callback when selection changes (itemId, isSelected)
 * @param isEditable Whether the list supports edit/delete actions
 * @param onEdit Callback for edit action
 * @param onDelete Callback for delete action
 * @param onItemClick Callback for item click (only when not in selectable or editable mode)
 * @param emptyStateMessage Message to display when no items are found
 * @param globalAction Optional list of ActionItem objects to display beside the title
 * @param cardActions Optional list of ActionItem objects to display in each card
 */
@Composable
fun <T> AppList(
    title: String? = null,
    items: List<T>,
    onItemToListItem: @Composable (T) -> AppListItem,
    modifier: Modifier = Modifier,
    searchPlaceholder: String = "Search...",
    showSearch: Boolean = true,
    isSelectable: Boolean = false,
    isItemClickable: Boolean = false,
    selectedItems: Set<String> = emptySet(),
    onSelectionChange: ((String, Boolean) -> Unit)? = null,
    isEditable: Boolean = false,
    isDeletable: Boolean = false,
    onEdit: ((T) -> Unit)? = null,
    onDelete: ((T) -> Unit)? = null,
    onItemClick: ((T) -> Unit)? = null,
    emptyStateMessage: String = "No items found",
    globalAction: List<ActionItem>? = null,
    cardActions: List<ActionItem>? = null,
) {
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf<T?>(null) }

    // Filter items based on search query
    val filteredItems = items.filter { item ->
        if (searchQuery.isBlank()) {
            true
        } else {
            val listItem = onItemToListItem(item)
            listItem.title.contains(searchQuery, ignoreCase = true) ||
                    (listItem.subtitle?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Title and Global Action Row (only show if title is provided or global actions exist)
        if (title != null || globalAction != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                title?.let { titleText ->
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                } ?: run {
                    // If no title but global actions exist, add a spacer to push actions to the right
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                }
                globalAction?.let { actionItems ->
                    AppActionRow(actions = actionItems)
                }
            }
        }

        // Search Bar
        if (showSearch) {
            AppSearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = searchPlaceholder,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        // Selection Mode Controls
        if (isSelectable) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedItems.size} selected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(
                    onClick = {
                        selectedItems.forEach { itemId ->
                            onSelectionChange?.invoke(itemId, false)
                        }
                    }
                ) {
                    Text("Clear All")
                }
            }
        }

        // Items List
        if (filteredItems.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = emptyStateMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredItems) { item ->
                    val listItem = onItemToListItem(item)
                    val isItemSelected = selectedItems.contains(listItem.id)

                    AppCard(
                        title = listItem.title,
                        subtitle = listItem.subtitle,
                        onClick = {
                            when {
                                isSelectable -> {
                                    // Toggle selection when clicking on the item
                                    onSelectionChange?.invoke(listItem.id, !isItemSelected)
                                }
                                isItemClickable -> {
                                    // Regular item click when not in edit mode
                                    onItemClick?.invoke(item)
                                }
                            }
                        },
                        content = {
                            listItem.content?.invoke()
                        },
                        actions = cardActions ?: emptyList(),
                        isClickable = isSelectable || isItemClickable,
                        showEditAction = isEditable,
                        showDeleteAction = isDeletable,
                        selected = isItemSelected, // <-- pass selected state,
                        onEdit = {
                            onEdit?.invoke(item)
                        },
                        onDelete = {
                            showDeleteConfirmation = item
                        }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    showDeleteConfirmation?.let { itemToDelete ->
        val listItem = onItemToListItem(itemToDelete)
        AppConfirmDialog(
            isVisible = true,
            title = stringResource(R.string.delete_group), // You may want to make this more generic
            message = "Are you sure you want to delete \"${listItem.title}\"?",
            onConfirm = {
                onDelete?.invoke(itemToDelete)
            },
            onDismiss = {
                showDeleteConfirmation = null
            },
            isDestructive = true
        )
    }
}