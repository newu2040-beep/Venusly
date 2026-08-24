package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.DefaultPresets
import com.example.model.EditorTab
import com.example.model.FilterPreset
import com.example.ui.components.RecentEditsBottomSheet
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PresetsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VenuslyBlue
import com.example.ui.theme.VenuslyBlueContainer
import com.example.viewmodel.EditorViewModel
import com.example.viewmodel.HomeViewModel

enum class AppDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    PRESETS("Presets", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    EDIT("Edit", Icons.Filled.Tune, Icons.Outlined.Tune),
    TOOLS("Tools", Icons.Filled.GridView, Icons.Outlined.GridView),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val homeViewModel: HomeViewModel = viewModel()
            val editorViewModel: EditorViewModel = viewModel()
            val isDarkMode by homeViewModel.isDarkMode.collectAsState()
            val selectedTheme by homeViewModel.selectedTheme.collectAsState()
            val isCompactMode by homeViewModel.isCompactMode.collectAsState()

            MyApplicationTheme(
                darkTheme = isDarkMode,
                pastelTheme = selectedTheme,
                compactMode = isCompactMode
            ) {
                VenuslyApp(
                    homeViewModel = homeViewModel,
                    editorViewModel = editorViewModel
                )
            }
        }
    }
}

@Composable
fun VenuslyApp(
    homeViewModel: HomeViewModel,
    editorViewModel: EditorViewModel
) {
    var showOnboarding by remember { mutableStateOf(false) } // Set to false to start directly at main experience
    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
    var inspectPreset by remember { mutableStateOf<FilterPreset?>(DefaultPresets.presets.first()) }
    var inCameraMode by remember { mutableStateOf(false) }
    var showRecentModal by remember { mutableStateOf(false) }

    val recentProjects by homeViewModel.recentProjects.collectAsState()
    val isCompactMode by homeViewModel.isCompactMode.collectAsState()
    val isDark = isSystemInDarkTheme()

    if (showOnboarding) {
        OnboardingScreen(
            onGetStarted = { showOnboarding = false }
        )
        return
    }

    if (inCameraMode) {
        CameraScreen(
            editorViewModel = editorViewModel,
            onPhotoCaptured = {
                inCameraMode = false
                currentDestination = AppDestination.EDIT
            },
            onBack = { inCameraMode = false }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Apple-inspired Floating/Pill Navigation Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = if (isDark) Color(0xFF141B2D).copy(alpha = 0.95f) else Color(0xFFFFFFFF).copy(alpha = 0.95f),
                shadowElevation = if (isCompactMode) 8.dp else 16.dp,
                tonalElevation = 4.dp
            ) {
                NavigationBar(
                    modifier = Modifier
                        .testTag("bottom_nav_bar")
                        .fillMaxWidth()
                        .height(if (isCompactMode) 58.dp else 68.dp),
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    AppDestination.values().forEach { destination ->
                        val isSelected = currentDestination == destination
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.title,
                                    modifier = Modifier.size(if (isCompactMode) 20.dp else 24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = destination.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = if (isCompactMode) 10.sp else 11.sp
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                unselectedTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { dest ->
                when (dest) {
                    AppDestination.HOME -> {
                        HomeScreen(
                            homeViewModel = homeViewModel,
                            editorViewModel = editorViewModel,
                            onNavigateToEdit = { currentDestination = AppDestination.EDIT },
                            onNavigateToCamera = { inCameraMode = true },
                            onNavigateToPresets = { preset ->
                                inspectPreset = preset
                                currentDestination = AppDestination.PRESETS
                            },
                            onSeeAllRecent = { showRecentModal = true }
                        )
                    }

                    AppDestination.PRESETS -> {
                        PresetsScreen(
                            initialPreset = inspectPreset,
                            homeViewModel = homeViewModel,
                            editorViewModel = editorViewModel,
                            onApply = { currentDestination = AppDestination.EDIT },
                            onBack = { currentDestination = AppDestination.HOME }
                        )
                    }

                    AppDestination.EDIT -> {
                        EditorScreen(
                            editorViewModel = editorViewModel,
                            onBack = { currentDestination = AppDestination.HOME }
                        )
                    }

                    AppDestination.TOOLS -> {
                        ToolsScreen(
                            editorViewModel = editorViewModel,
                            onNavigateToEditWithTab = { tab ->
                                editorViewModel.setActiveTab(tab)
                                currentDestination = AppDestination.EDIT
                            }
                        )
                    }

                    AppDestination.PROFILE -> {
                        ProfileScreen(
                            homeViewModel = homeViewModel,
                            editorViewModel = editorViewModel,
                            onNavigateToEdit = { currentDestination = AppDestination.EDIT }
                        )
                    }
                }
            }
        }
    }

    if (showRecentModal) {
        RecentEditsBottomSheet(
            projects = recentProjects,
            onSelectProject = { project ->
                editorViewModel.loadProject(project)
                currentDestination = AppDestination.EDIT
            },
            onDeleteProject = { project ->
                homeViewModel.deleteProject(project)
            },
            onDismiss = { showRecentModal = false }
        )
    }
}
