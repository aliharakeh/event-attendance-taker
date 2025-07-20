package com.example.attendancetaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.attendancetaker.ui.screens.ContactGroupDetailsScreen
import com.example.attendancetaker.ui.screens.ContactGroupEditScreen
import com.example.attendancetaker.ui.screens.ContactGroupSelectionScreen
import com.example.attendancetaker.ui.screens.ContactSelectionScreen
import com.example.attendancetaker.ui.screens.ContactsScreen
import com.example.attendancetaker.ui.screens.EventEditScreen
import com.example.attendancetaker.ui.screens.EventHistoryScreen
import com.example.attendancetaker.ui.screens.EventsScreen
import com.example.attendancetaker.ui.screens.RecurringTemplatesScreen
import com.example.attendancetaker.ui.theme.AttendanceTakerTheme
import com.example.attendancetaker.utils.LanguageManager
import com.example.attendancetaker.utils.RecurringEventManager
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val repository = remember { AttendanceRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    // Initialize database with sample data and create recurring events when app starts
    LaunchedEffect(repository) {
        // Sync contact names with phone contacts first
        repository.syncContactNamesWithPhone()
        // Then create recurring events
        RecurringEventManager.createTodaysRecurringEvents(repository)
    }

    // Listen to language changes to trigger recomposition
    val currentLanguage by languageManager.currentLanguage

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // State for dialogs
    var showAddContactDialog by remember { mutableStateOf(false) }

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
                                        onClick = {
                                            // Navigate directly to ContactGroupEdit for new group
                                            navController.navigate(Screen.ContactGroupEdit.createRouteForNew())
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = stringResource(R.string.add),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Screen.Events.route -> {
                                    IconButton(
                                        onClick = {
                                            navController.navigate(Screen.EventEdit.createRouteForNew())
                                        },
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
                        // Language toggle button
                        LanguageToggleButton(languageManager)
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
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300, easing = EaseInOut)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300, easing = EaseInOut)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300, easing = EaseInOut)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300, easing = EaseInOut)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            composable(
                route = Screen.Contacts.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                ContactsScreen(
                    repository = repository,
                    onNavigateToGroupDetails = { group ->
                        navController.navigate(Screen.ContactGroupDetails.createRoute(group.id))
                    },
                    onNavigateToGroupEdit = { group ->
                        val route = if (group == null) {
                            Screen.ContactGroupEdit.createRouteForNew()
                        } else {
                            Screen.ContactGroupEdit.createRoute(group.id)
                        }
                        navController.navigate(route)
                    }
                )
            }

            composable(
                route = Screen.Events.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                EventsScreen(
                    repository = repository,
                    onNavigateToAttendance = { eventId ->
                        navController.navigate(Screen.AttendanceList.createRoute(eventId))
                    },
                    onNavigateToEventEdit = { event ->
                        val route = if (event == null) {
                            Screen.EventEdit.createRouteForNew()
                        } else {
                            Screen.EventEdit.createRoute(event.id)
                        }
                        navController.navigate(route)
                    },
                    onNavigateToHistory = {
                        navController.navigate(Screen.EventHistory.route)
                    },
                    onNavigateToRecurringTemplates = {
                        navController.navigate(Screen.RecurringTemplates.route)
                    }
                )
            }

            composable(
                route = Screen.EventHistory.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                EventHistoryScreen(
                    repository = repository,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToAttendance = { eventId ->
                        navController.navigate(Screen.AttendanceList.createRoute(eventId))
                    }
                )
            }

            composable(
                route = Screen.RecurringTemplates.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                RecurringTemplatesScreen(
                    repository = repository,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEventEdit = { event ->
                        val route = if (event == null) {
                            Screen.EventEdit.createRouteForNew()
                        } else {
                            Screen.EventEdit.createRoute(event.id)
                        }
                        navController.navigate(route)
                    }
                )
            }

            composable(
                route = Screen.AttendanceList.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
                AttendanceScreen(
                    eventId = eventId,
                    repository = repository,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.ContactGroupEdit.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
                ContactGroupEditScreen(
                    groupId = if (groupId == "new") null else groupId,
                    repository = repository,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToContactSelection = { groupId ->
                        val route = if (groupId == null) {
                            Screen.ContactSelection.createRouteForNew()
                        } else {
                            Screen.ContactSelection.createRoute(groupId)
                        }
                        navController.navigate(route)
                    }
                )
            }

            composable(
                route = Screen.ContactGroupDetails.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
                ContactGroupDetailsScreen(
                    groupId = groupId,
                    repository = repository,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.ContactSelection.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
                ContactSelectionScreen(
                    groupId = if (groupId == "new") null else groupId,
                    repository = repository,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.ContactGroupSelection.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
                ContactGroupSelectionScreen(
                    eventId = if (eventId == "new") null else eventId,
                    repository = repository,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.EventEdit.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300, easing = EaseInOut)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
                EventEditScreen(
                    eventId = if (eventId == "new") null else eventId,
                    repository = repository,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToContactGroupSelection = { eventId ->
                        val route = if (eventId == null) {
                            Screen.ContactGroupSelection.createRouteForNew()
                        } else {
                            Screen.ContactGroupSelection.createRoute(eventId)
                        }
                        navController.navigate(route)
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
                    text = stringResource(
                        R.string.current_language,
                        languageManager.getCurrentLanguageName()
                    ),
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
                        Text(stringResource(R.string.english))
                    }

                    // Arabic Button
                    OutlinedButton(
                        onClick = {
                            languageManager.setLanguage("ar")
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.arabic))
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