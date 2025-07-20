# UI Components

This folder contains reusable UI components that provide a consistent and modern design throughout the app. All components follow Material Design 3 principles and use the app's defined color scheme.

## Components

### 1. AppCard

A flexible card component with title, subtitle, content, and actions.

**Features:**

-   Optional title and subtitle
-   Composable content slot
-   Composable actions slot (typically for buttons/icons)
-   Optional click handler
-   Consistent elevation and styling

**Usage:**

```kotlin
AppCard(
    title = "Contact Group",
    subtitle = "5 members",
    onClick = { /* handle click */ },
    content = {
        Text("Additional content goes here")
    },
    actions = {
        IconButton(onClick = { /* edit */ }) {
            Icon(Icons.Default.Edit, contentDescription = "Edit")
        }
    }
)
```

### 2. AppSearchField

A search input field with modern rounded design and clear functionality.

**Features:**

-   Search icon on the left
-   Clear button when text is present
-   Rounded corners (28dp)
-   Consistent styling with app theme

**Usage:**

```kotlin
var searchQuery by remember { mutableStateOf("") }
AppSearchField(
    value = searchQuery,
    onValueChange = { searchQuery = it },
    placeholder = "Search contacts...",
    modifier = Modifier.fillMaxWidth()
)
```

### 3. AppTextField

A versatile input field with modern styling and comprehensive customization options.

**Features:**

-   Outlined style with rounded corners (12dp)
-   Support for labels, placeholders, and supporting text
-   Error states
-   Leading and trailing icons
-   Keyboard options and actions
-   Single-line and multi-line support

**Usage:**

```kotlin
var text by remember { mutableStateOf("") }
AppTextField(
    value = text,
    onValueChange = { text = it },
    label = "Event Name",
    placeholder = "Enter event name",
    isError = text.isEmpty(),
    supportingText = if (text.isEmpty()) "This field is required" else null
)
```

### 4. AppDateRangePicker

A date range picker with two date fields in a row layout.

**Features:**

-   Start and end date fields
-   Customizable placeholders
-   Date formatting (MMM dd, yyyy)
-   Click handlers for opening date pickers
-   Disabled state support

**Usage:**

```kotlin
var startDate by remember { mutableStateOf<LocalDate?>(null) }
var endDate by remember { mutableStateOf<LocalDate?>(null) }

AppDateRangePicker(
    startDate = startDate,
    endDate = endDate,
    onStartDateChange = { startDate = it },
    onEndDateChange = { endDate = it },
    startDatePlaceholder = "Start Date",
    endDatePlaceholder = "End Date",
    onStartDateClick = { /* show date picker */ },
    onEndDateClick = { /* show date picker */ }
)
```

### 5. AppConfirmDialog

A confirmation dialog for important actions like deletions.

**Features:**

-   Title and message text
-   Configurable button text
-   Destructive/non-destructive styling
-   Consistent with app's color scheme

**Usage:**

```kotlin
var showDialog by remember { mutableStateOf(false) }

AppConfirmDialog(
    isVisible = showDialog,
    title = "Delete Contact",
    message = "Are you sure you want to delete this contact?",
    onConfirm = { /* handle confirm */ },
    onDismiss = { showDialog = false },
    confirmButtonText = "Delete",
    isDestructive = true
)
```

### 6. AppList

A comprehensive list component with search, selection, and editing capabilities.

**Features:**

-   Built-in search functionality
-   Toggle item selection with checkboxes
-   Edit and delete buttons
-   Empty state handling
-   Flexible item rendering through data mapping

**Usage:**

```kotlin
data class Contact(val id: String, val name: String, val email: String)

val contacts = listOf(/* your contacts */)
var selectedItems by remember { mutableStateOf(emptySet<String>()) }

AppList(
    title = "Contacts",
    items = contacts,
    onItemToListItem = { contact ->
        AppListItem(
            id = contact.id,
            title = contact.name,
            subtitle = contact.email
        )
    },
    searchPlaceholder = "Search contacts...",
    isSelectable = true,
    selectedItems = selectedItems,
    onSelectionChange = { id, selected ->
        selectedItems = if (selected) {
            selectedItems + id
        } else {
            selectedItems - id
        }
    },
    isEditable = true,
    onEdit = { contact -> /* handle edit */ },
    onDelete = { contact -> /* handle delete */ },
    onItemClick = { contact -> /* handle click */ }
)
```

### 7. AppActionRow

A configurable row of action buttons (edit, delete, etc.) with flexible customization options.

**Features:**

-   Configurable action buttons with custom icons and colors
-   Pre-built edit and delete actions
-   ActionPresets for standard actions
-   Disabled state support
-   Flexible arrangement and spacing

**Usage:**

```kotlin
// Simple edit/delete actions
AppActionRow(
    onEdit = { /* handle edit */ },
    onDelete = { /* handle delete */ }
)

// Custom actions only
AppActionRow(
    actions = listOf(
        ActionItem(
            icon = Icons.Default.Star,
            contentDescription = "Star",
            tint = Warning,
            onClick = { /* handle star */ }
        ),
        ActionItem(
            icon = Icons.Default.Share,
            contentDescription = "Share",
            tint = ButtonBlue,
            onClick = { /* handle share */ }
        )
    ),
    showEditAction = false,
    showDeleteAction = false
)

// Mixed custom and default actions
AppActionRow(
    actions = listOf(
        ActionItem(
            icon = Icons.Default.Favorite,
            contentDescription = "Favorite",
            tint = Success,
            onClick = { /* handle favorite */ }
        )
    ),
    onEdit = { /* handle edit */ },
    onDelete = { /* handle delete */ }
)

// Using ActionPresets
AppActionRow(
    actions = listOf(
        ActionPresets.editAction(onClick = { /* handle edit */ }),
        ActionPresets.deleteAction(
            onClick = { /* handle delete */ },
            enabled = false // disabled state
        )
    ),
    showEditAction = false,
    showDeleteAction = false
)
```

**ActionItem Properties:**

-   `icon`: Vector icon to display
-   `contentDescription`: Accessibility description
-   `tint`: Color for the icon
-   `onClick`: Click handler
-   `enabled`: Whether the action is enabled (default: true)

## Design Principles

All components follow these design principles:

1. **Consistent Styling**: Use the app's Material Design 3 theme
2. **Rounded Corners**: Modern appearance with appropriate corner radius
3. **Accessible**: Proper content descriptions and keyboard support
4. **Flexible**: Composable slots for customization
5. **Reusable**: Generic enough to be used throughout the app

## Color Scheme

Components automatically use colors from the app's theme:

-   Primary: Blue variants
-   Surface: Cards and backgrounds
-   On-surface variants: Text and icons
-   Error: Red for destructive actions
-   Outline: Borders and dividers

## Migration Guide

To migrate existing screens to use these components:

1. Replace custom Card implementations with `AppCard`
2. Replace TextField search implementations with `AppSearchField`
3. Replace OutlinedTextField instances with `AppTextField`
4. Replace AlertDialog confirmations with `AppConfirmDialog`
5. Replace custom list implementations with `AppList`
6. Replace manual action button rows with `AppActionRow`

This will ensure consistent styling and behavior across the app.
