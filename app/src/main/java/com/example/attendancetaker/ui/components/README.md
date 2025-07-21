# UI Components

This folder contains reusable UI components that provide a consistent and modern design throughout the app. All components follow Material Design 3 principles and use the app's defined color scheme.

## Components

### 0. AppIconButton

A versatile icon button component that supports multiple visual styles including rounded backgrounds and text combinations.

**Features:**

-   6 different visual styles (rounded/no background, icon/text/both combinations)
-   Customizable colors, sizes, and spacing
-   Accessibility support with proper roles and descriptions
-   Disabled state handling with alpha transparency
-   Minimum touch target compliance for icon-only buttons
-   Theme-aware default colors

**Styles:**

-   `ROUNDED_ICON_ONLY` - Rounded background with icon only
-   `ROUNDED_ICON_TEXT` - Rounded background with icon and text
-   `ROUNDED_TEXT_ONLY` - Rounded background with text only
-   `NO_BACKGROUND_TEXT_ONLY` - No background with text only
-   `NO_BACKGROUND_ICON_ONLY` - No background with icon only
-   `NO_BACKGROUND_ICON_TEXT` - No background with icon and text

**Usage:**

```kotlin
// Rounded button with icon only
AppIconButton(
    style = AppIconButtonStyle.ROUNDED_ICON_ONLY,
    icon = Icons.Default.Add,
    onClick = { /* handle click */ },
    contentDescription = "Add item"
)

// Rounded button with icon and text
AppIconButton(
    style = AppIconButtonStyle.ROUNDED_ICON_TEXT,
    icon = Icons.Default.Save,
    text = "Save",
    onClick = { /* handle click */ },
    backgroundColor = MaterialTheme.colorScheme.primary
)

// No background text button
AppIconButton(
    style = AppIconButtonStyle.NO_BACKGROUND_TEXT_ONLY,
    text = "Cancel",
    onClick = { /* handle click */ },
    contentColor = MaterialTheme.colorScheme.error
)
```

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
-   **Click-to-toggle selection** when in selectable mode
-   Edit and delete buttons
-   Empty state handling
-   Flexible item rendering through data mapping
-   Clear all selection functionality
-   Selection count display

**Usage:**

```kotlin
data class Contact(val id: String, val name: String, val email: String)

val contacts = listOf(/* your contacts */)
var selectedItems by remember { mutableStateOf(emptySet<String>()) }

// Basic selectable list with click-to-toggle
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
    }
)

// Editable list with actions
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
    isEditable = true,
    onEdit = { contact -> /* handle edit */ },
    onDelete = { contact -> /* handle delete */ }
)

// Regular list with item clicks
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
    onItemClick = { contact -> /* handle click */ }
)
```

**Selection Behavior:**
- When `isSelectable = true`, users can click anywhere on the item to toggle selection
- Checkboxes are still available for explicit selection
- Selection count is displayed when items are selected
- "Clear All" button appears to deselect all items
- When `isEditable = true`, item clicks are disabled to prevent conflicts with action buttons

### 7. AppToolbar

A flexible and reusable toolbar component for pages with title, navigation, and action buttons.

**Features:**

-   Configurable title and optional subtitle
-   Navigation icon (back button by default)
-   Multiple action buttons support
-   Custom colors and styling
-   Built-in action presets for common actions
-   Consistent with Material Design 3

**Usage:**

```kotlin
// Simple toolbar with title and back navigation
AppToolbar(
    title = "Contact Details",
    onNavigationClick = { /* handle back */ }
)

// Toolbar with subtitle and actions
AppToolbar(
    title = "Events",
    subtitle = "5 upcoming events",
    actions = listOf(
        ToolbarActionPresets.searchAction(onClick = { /* handle search */ }),
        ToolbarActionPresets.addAction(onClick = { /* handle add */ })
    )
)

// Custom toolbar with custom navigation icon and actions
AppToolbar(
    title = "Settings",
    navigationIcon = Icons.Default.Menu,
    onNavigationClick = { /* open menu */ },
    actions = listOf(
        ToolbarAction(
            icon = Icons.Default.Save,
            contentDescription = "Save",
            onClick = { /* handle save */ }
        )
    )
)

// Toolbar without navigation icon
AppToolbar(
    title = "Main Screen",
    showNavigationIcon = false,
    actions = listOf(
        ToolbarActionPresets.menuAction(onClick = { /* handle menu */ })
    )
)
```

**ToolbarAction Properties:**

-   `icon`: Vector icon to display
-   `contentDescription`: Accessibility description
-   `onClick`: Click handler
-   `enabled`: Whether the action is enabled (default: true)
-   `tint`: Custom color for the icon (optional)

**ToolbarActionPresets:**

-   `searchAction()`: Standard search icon
-   `menuAction()`: Three-dot menu icon
-   `addAction()`: Plus/add icon
-   `filterAction()`: Filter list icon

### 8. AppActionRow

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
7. Replace TopAppBar implementations with `AppToolbar`

This will ensure consistent styling and behavior across the app.
