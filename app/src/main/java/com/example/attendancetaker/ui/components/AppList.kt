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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.attendancetaker.ui.theme.ButtonRed
import com.example.attendancetaker.ui.theme.CheckboxSelected
import com.example.attendancetaker.ui.theme.CheckboxUnselected
import com.example.attendancetaker.ui.theme.EditIconBlue

data class AppListItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val isSelected: Boolean = false,
    val content: @Composable (() -> Unit)? = null
)

@Composable
fun <T> AppList(
    title: String,
    items: List<T>,
    onItemToListItem: (T) -> AppListItem,
    modifier: Modifier = Modifier,
    searchPlaceholder: String = "Search...",
    showSearch: Boolean = true,
    isSelectable: Boolean = false,
    selectedItems: Set<String> = emptySet(),
    onSelectionChange: ((String, Boolean) -> Unit)? = null,
    isEditable: Boolean = false,
    onEdit: ((T) -> Unit)? = null,
    onDelete: ((T) -> Unit)? = null,
    onItemClick: ((T) -> Unit)? = null,
    emptyStateMessage: String = "No items found"
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
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Search Bar
        if (showSearch) {
            AppSearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = searchPlaceholder,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Selection Mode Controls
        if (isSelectable && selectedItems.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredItems) { item ->
                    val listItem = onItemToListItem(item)

                    AppCard(
                        title = listItem.title,
                        subtitle = listItem.subtitle,
                        onClick = {
                            if (!isSelectable && !isEditable) {
                                { onItemClick?.invoke(item) }
                            } else null
                        },
                        content = {
                            listItem.content?.invoke()
                        },
                        actions = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Selection checkbox
                                if (isSelectable) {
                                    Checkbox(
                                        checked = selectedItems.contains(listItem.id),
                                        onCheckedChange = { checked ->
                                            onSelectionChange?.invoke(listItem.id, checked)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = CheckboxSelected,
                                            uncheckedColor = CheckboxUnselected,
                                            checkmarkColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }

                                // Edit button
                                if (isEditable && onEdit != null) {
                                    IconButton(onClick = { onEdit(item) }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = EditIconBlue
                                        )
                                    }
                                }

                                // Delete button
                                if (isEditable && onDelete != null) {
                                    IconButton(onClick = { showDeleteConfirmation = item }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = ButtonRed
                                        )
                                    }
                                }
                            }
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