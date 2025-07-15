package com.example.attendancetaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.attendancetaker.data.AttendanceRepository
import com.example.attendancetaker.ui.navigation.Screen
import com.example.attendancetaker.ui.screens.AttendanceScreen
import com.example.attendancetaker.ui.screens.ContactsScreen
import com.example.attendancetaker.ui.screens.EventsScreen
import com.example.attendancetaker.ui.theme.AttendanceTakerTheme
import com.example.attendancetaker.utils.LanguageManager
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape

class MainActivity : ComponentActivity() {
    private lateinit var languageManager: LanguageManager

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize language manager and apply saved language
        languageManager = LanguageManager(this)
        languageManager.initializeLanguage()

        // Set up language change listener to recreate activity
        languageManager.setOnLanguageChangeListener {
            recreate()
        }

        enableEdgeToEdge()
        setContent {
            AttendanceTakerTheme {
                AttendanceTakerApp(languageManager)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceTakerApp(languageManager: LanguageManager) {
    val navController = rememberNavController()
    val repository = remember { AttendanceRepository() }

    // Listen to language changes to trigger recomposition
    val currentLanguage by languageManager.currentLanguage

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // State for dialogs
    var showAddContactDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    // Define bottom navigation items
    val bottomNavItems = listOf(
        BottomNavItem(
            screen = Screen.Events,
            icon = Icons.Default.Event,
            label = stringResource(R.string.nav_events)
        ),
        BottomNavItem(
            screen = Screen.Contacts,
            icon = Icons.Default.People,
            label = stringResource(R.string.nav_contacts)
        )
    )

    // Check if we should show bottom navigation
    val showBottomNav = currentDestination?.route in listOf(
        Screen.Events.route,
        Screen.Contacts.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (showBottomNav) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = when (currentDestination?.route) {
                                    Screen.Contacts.route -> stringResource(R.string.nav_contacts)
                                    Screen.Events.route -> stringResource(R.string.nav_events)
                                    else -> stringResource(R.string.app_name)
                                },
                                fontWeight = FontWeight.Bold
                            )

                            // Add button based on current screen
                            when (currentDestination?.route) {
                                Screen.Contacts.route -> {
                                    IconButton(
                                        onClick = { showAddContactDialog = true },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = stringResource(R.string.add_contact),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Screen.Events.route -> {
                                    IconButton(
                                        onClick = { showAddEventDialog = true },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = stringResource(R.string.add_event),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    actions = {
                        LanguageToggleButton(languageManager = languageManager)
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomNav) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Events.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Contacts.route) {
                ContactsScreen(
                    repository = repository,
                    showAddDialog = showAddContactDialog,
                    onAddDialogDismiss = { showAddContactDialog = false }
                )
            }

            composable(Screen.Events.route) {
                EventsScreen(
                    repository = repository,
                    onNavigateToAttendance = { eventId ->
                        navController.navigate(Screen.AttendanceList.createRoute(eventId))
                    },
                    showAddDialog = showAddEventDialog,
                    onAddDialogDismiss = { showAddEventDialog = false }
                )
            }

            composable(Screen.AttendanceList.route) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
                AttendanceScreen(
                    eventId = eventId,
                    repository = repository,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun LanguageToggleButton(languageManager: LanguageManager) {
    var showLanguageDialog by remember { mutableStateOf(false) }

    IconButton(onClick = { showLanguageDialog = true }) {
        Icon(
            Icons.Default.Language,
            contentDescription = stringResource(R.string.language_toggle)
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            languageManager = languageManager,
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
fun LanguageSelectionDialog(
    languageManager: LanguageManager,
    onDismiss: () -> Unit
) {
    val currentLanguage by languageManager.currentLanguage

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.language_toggle))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.current_language, languageManager.getCurrentLanguageName()),
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // English Button
                    OutlinedButton(
                        onClick = {
                            languageManager.setLanguage("en")
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("English")
                    }

                    // Arabic Button
                    OutlinedButton(
                        onClick = {
                            languageManager.setLanguage("ar")
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("العربية")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

data class BottomNavItem(
    val screen: Screen,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)