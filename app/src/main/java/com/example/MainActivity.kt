package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.ProjectRepository
import com.example.data.local.UserPreferencesRepository
import com.example.data.quran.QuranRepository
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.EditorViewModel

enum class NavigationScreen {
    HOME,
    PROJECTS,
    SETTINGS,
    EDITOR,
    EXPORT
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val context = applicationContext
        val database = AppDatabase.getDatabase(context)
        val projectRepository = ProjectRepository(database.projectDao())
        val quranRepository = QuranRepository()
        val userPreferencesRepository = UserPreferencesRepository(context)
        val editorViewModel = EditorViewModel(projectRepository, quranRepository, userPreferencesRepository)

        setContent {
            val userSettings by userPreferencesRepository.settings.collectAsState()

            QuranVideoEditorTheme(
                themeMode = userSettings.themeMode,
                accentChoice = userSettings.accentColor
            ) {
                var currentScreen by remember { mutableStateOf(NavigationScreen.HOME) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = BackgroundDark,
                    bottomBar = {
                        // Only show bottom navigation on primary tabs (Home, Projects, Settings)
                        if (currentScreen in listOf(NavigationScreen.HOME, NavigationScreen.PROJECTS, NavigationScreen.SETTINGS)) {
                            NavigationBar(
                                containerColor = SurfaceDark,
                                contentColor = QuranGoldPrimary
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen == NavigationScreen.HOME,
                                    onClick = { currentScreen = NavigationScreen.HOME },
                                    icon = {
                                        Icon(
                                            Icons.Default.Home,
                                            contentDescription = "Home",
                                            modifier = Modifier.size(userSettings.iconSize.dp.dp)
                                        )
                                    },
                                    label = { Text("Home", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = QuranGoldPrimary,
                                        indicatorColor = QuranGoldPrimary,
                                        unselectedIconColor = TextGray,
                                        unselectedTextColor = TextGray
                                    ),
                                    modifier = Modifier.testTag("nav_home")
                                )

                                NavigationBarItem(
                                    selected = currentScreen == NavigationScreen.PROJECTS,
                                    onClick = { currentScreen = NavigationScreen.PROJECTS },
                                    icon = {
                                        Icon(
                                            Icons.Default.Folder,
                                            contentDescription = "Projects",
                                            modifier = Modifier.size(userSettings.iconSize.dp.dp)
                                        )
                                    },
                                    label = { Text("Projects", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = QuranGoldPrimary,
                                        indicatorColor = QuranGoldPrimary,
                                        unselectedIconColor = TextGray,
                                        unselectedTextColor = TextGray
                                    ),
                                    modifier = Modifier.testTag("nav_projects")
                                )

                                NavigationBarItem(
                                    selected = currentScreen == NavigationScreen.SETTINGS,
                                    onClick = { currentScreen = NavigationScreen.SETTINGS },
                                    icon = {
                                        Icon(
                                            Icons.Default.Settings,
                                            contentDescription = "Settings",
                                            modifier = Modifier.size(userSettings.iconSize.dp.dp)
                                        )
                                    },
                                    label = { Text("Settings", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = QuranGoldPrimary,
                                        indicatorColor = QuranGoldPrimary,
                                        unselectedIconColor = TextGray,
                                        unselectedTextColor = TextGray
                                    ),
                                    modifier = Modifier.testTag("nav_settings")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            NavigationScreen.HOME -> {
                                HomeScreen(
                                    projectRepository = projectRepository,
                                    onVideoSelected = { uri ->
                                        editorViewModel.createProjectFromVideo(context, uri)
                                        currentScreen = NavigationScreen.EDITOR
                                    },
                                    onOpenProject = { projectId ->
                                        editorViewModel.loadProject(projectId)
                                        currentScreen = NavigationScreen.EDITOR
                                    },
                                    onNavigateToProjects = { currentScreen = NavigationScreen.PROJECTS }
                                )
                            }

                            NavigationScreen.PROJECTS -> {
                                ProjectsScreen(
                                    projectRepository = projectRepository,
                                    onOpenProject = { projectId ->
                                        editorViewModel.loadProject(projectId)
                                        currentScreen = NavigationScreen.EDITOR
                                    }
                                )
                            }

                            NavigationScreen.SETTINGS -> {
                                SettingsScreen(
                                    userPreferencesRepository = userPreferencesRepository
                                )
                            }

                            NavigationScreen.EDITOR -> {
                                MainEditorScreen(
                                    viewModel = editorViewModel,
                                    onNavigateBack = { currentScreen = NavigationScreen.HOME },
                                    onNavigateToExport = { currentScreen = NavigationScreen.EXPORT }
                                )
                            }

                            NavigationScreen.EXPORT -> {
                                ExportScreen(
                                    viewModel = editorViewModel,
                                    onNavigateBack = { currentScreen = NavigationScreen.EDITOR }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
