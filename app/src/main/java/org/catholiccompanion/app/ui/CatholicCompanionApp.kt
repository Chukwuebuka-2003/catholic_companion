package org.catholiccompanion.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import android.util.Log
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.catholiccompanion.app.data.RosaryProgressRepository
import org.catholiccompanion.app.ai.AiDayContext
import org.catholiccompanion.app.ai.AiRepository
import org.catholiccompanion.app.data.bible.BibleRepository
import org.catholiccompanion.app.data.liturgy.LiturgyDatabase
import org.catholiccompanion.app.data.liturgy.LiturgyRepository
import org.catholiccompanion.app.data.liturgy.BundledCalendarInstaller
import org.catholiccompanion.app.ui.screens.CelebrationsScreen
import org.catholiccompanion.app.ui.screens.LearnScreen
import org.catholiccompanion.app.ui.screens.AngelusScreen
import org.catholiccompanion.app.ui.screens.RosaryScreen
import org.catholiccompanion.app.ui.screens.SettingsScreen
import org.catholiccompanion.app.ui.screens.TodayScreen

private enum class MainDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    TODAY("today", "Today", Icons.Default.Home),
    CELEBRATIONS("celebrations", "Saints", Icons.Default.DateRange),
    ROSARY("rosary", "Rosary", Icons.Default.Favorite),
    ANGELUS("angelus", "Angelus", Icons.Default.Notifications),
    LEARN("learn", "Learn", Icons.Default.Info),
}

private const val SETTINGS_ROUTE = "settings"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatholicCompanionApp(openAngelus: Boolean = false) {
    val context = LocalContext.current.applicationContext
    val rosaryViewModel: RosaryViewModel = viewModel(
        factory = RosaryViewModel.Factory(RosaryProgressRepository(context)),
    )
    val rosaryState by rosaryViewModel.uiState.collectAsStateWithLifecycle()
    val liturgyDatabase = remember(context) { LiturgyDatabase.create(context) }
    val liturgyRepository = remember(liturgyDatabase) { LiturgyRepository(liturgyDatabase) }
    val bibleRepository = remember(context) { BibleRepository(context) }
    LaunchedEffect(liturgyRepository) {
        runCatching {
            BundledCalendarInstaller(context, liturgyRepository).installIfNeeded()
        }.onFailure { error ->
            Log.e("CatholicCompanion", "Unable to install bundled calendar", error)
        }
    }
    val todayViewModel: TodayViewModel = viewModel(
        factory = TodayViewModel.Factory(liturgyRepository, bibleRepository),
    )
    val todayState by todayViewModel.uiState.collectAsStateWithLifecycle()
    val celebrationsViewModel: CelebrationsViewModel = viewModel(
        factory = CelebrationsViewModel.Factory(liturgyRepository),
    )
    val celebrationsState by celebrationsViewModel.uiState.collectAsStateWithLifecycle()
    val learnViewModel: LearnViewModel = viewModel(
        factory = LearnViewModel.Factory(AiRepository(context)),
    )
    val learnState by learnViewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: MainDestination.TODAY.route
    val isSettings = currentRoute == SETTINGS_ROUTE
    val title = if (isSettings) {
        "Settings"
    } else {
        MainDestination.entries.firstOrNull { it.route == currentRoute }?.label ?: "Catholic Companion"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    if (isSettings) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (!isSettings) {
                        IconButton(onClick = { navController.navigate(SETTINGS_ROUTE) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (!isSettings) {
                NavigationBar {
                    MainDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (openAngelus) {
                MainDestination.ANGELUS.route
            } else {
                MainDestination.TODAY.route
            },
        ) {
            composable(MainDestination.TODAY.route) {
                TodayScreen(
                    contentPadding = innerPadding,
                    state = todayState,
                    onPreviousDay = todayViewModel::previousDay,
                    onNextDay = todayViewModel::nextDay,
                    onReturnToToday = todayViewModel::returnToToday,
                    onPrayRosary = { navController.navigate(MainDestination.ROSARY.route) },
                )
            }
            composable(MainDestination.CELEBRATIONS.route) {
                CelebrationsScreen(
                    contentPadding = innerPadding,
                    state = celebrationsState,
                    onPreviousMonth = celebrationsViewModel::previousMonth,
                    onNextMonth = celebrationsViewModel::nextMonth,
                    onReturnToCurrentMonth = celebrationsViewModel::returnToCurrentMonth,
                    onToggleRank = celebrationsViewModel::toggleRank,
                )
            }
            composable(MainDestination.ROSARY.route) {
                RosaryScreen(
                    contentPadding = innerPadding,
                    state = rosaryState,
                    onSelectSet = rosaryViewModel::selectMysterySet,
                    onStart = rosaryViewModel::start,
                    onPrevious = rosaryViewModel::previous,
                    onNext = rosaryViewModel::next,
                    onStartAgain = rosaryViewModel::startAgain,
                    onEndSession = rosaryViewModel::endSession,
                    onSoundEnabledChange = rosaryViewModel::setSoundEnabled,
                    onHapticsEnabledChange = rosaryViewModel::setHapticsEnabled,
                )
            }
            composable(MainDestination.ANGELUS.route) {
                AngelusScreen(contentPadding = innerPadding)
            }
            composable(MainDestination.LEARN.route) {
                val dayContext = todayState.record?.let { record ->
                    AiDayContext(
                        date = record.day.date,
                        calendar = todayState.scope?.displayName ?: record.day.calendarId,
                        celebration = record.selectedCelebration.title,
                        rank = record.selectedCelebration.rank,
                        season = record.day.season,
                        readingReferences = record.readings.map { reading ->
                            "${reading.label}: ${reading.citation}"
                        },
                    )
                }
                val currentMysterySet = rosaryState.progress?.mysterySet ?: rosaryState.selectedSet
                LearnScreen(
                    contentPadding = innerPadding,
                    state = learnState,
                    dayContext = dayContext,
                    mysterySet = currentMysterySet,
                    onDraftChange = learnViewModel::updateDraft,
                    onAsk = learnViewModel::ask,
                    onClear = learnViewModel::clearConversation,
                )
            }
            composable(SETTINGS_ROUTE) {
                SettingsScreen(contentPadding = innerPadding)
            }
        }
    }
}
