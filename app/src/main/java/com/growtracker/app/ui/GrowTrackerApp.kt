package com.growtracker.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
// use fully-qualified material icon references to avoid receiver/import mismatches
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.growtracker.app.ui.grow.GrowDataStore
import com.growtracker.app.ui.grow.GrowScreenV2
import com.growtracker.app.ui.grow.PlantDetailScreen
import com.growtracker.app.ui.growguide.GrowGuideScreen
import com.growtracker.app.ui.overview.OverviewScreen
import com.growtracker.app.ui.statistics.PlantStatisticsScreen
import com.growtracker.app.ui.settings.SettingsScreen
import com.growtracker.app.ui.theme.ThemeManager
import com.growtracker.app.ui.language.LanguageManager
import com.growtracker.app.ui.language.Strings
import com.growtracker.app.ui.language.getString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowTrackerApp(
    modifier: Modifier = Modifier,
    themeManager: ThemeManager,
    languageManager: LanguageManager
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    LaunchedEffect(Unit) { GrowDataStore.initialize(context) }
    // single Snackbar host used across top-level destinations so snackbars (eg. "Pflanze hinzugefügt") are visible everywhere
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    // Skip authentication - go directly to main app
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            // Android-Style Bottom Navigation
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                tonalElevation = 3.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    listOf(
                        TopLevelDestination.OVERVIEW,
                        TopLevelDestination.GROW,
                        TopLevelDestination.SETTINGS
                    ).forEach { destination ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                        
                        // Android-style animation
                        val iconColor by animateColorAsState(
                            targetValue = if (isSelected) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = tween(250),
                            label = "icon_color"
                        )
                        
                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = tween(250),
                            label = "text_color"
                        )
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        navController.navigate(destination.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                .padding(vertical = 8.dp),
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.getTitle(languageManager),
                                modifier = Modifier.size(24.dp),
                                tint = iconColor
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = destination.getTitle(languageManager),
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.OVERVIEW.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(TopLevelDestination.OVERVIEW.route) {
                OverviewScreen(
                    languageManager = languageManager,
                    onOpenGrowGuide = { navController.navigate("growguide") },
                    onOpenStatistics = { navController.navigate("plant-stats") }
                )
            }
            composable(TopLevelDestination.GROW.route) {
                GrowScreenV2(languageManager = languageManager, snackbarHostState = snackbarHostState, onOpenGrowbox = { id ->
                    navController.navigate("grow/plant/$id")
                }, onOpenGrowGuide = { navController.navigate("growguide") })
            }
            composable("growguide") {
                GrowGuideScreen(languageManager = languageManager, onNavigateBack = { navController.popBackStack() })
            }
            composable("plant-stats") {
                PlantStatisticsScreen(languageManager = languageManager, onNavigateBack = { navController.popBackStack() })
            }
            composable("grow/plant/{plantId}") { backStackEntry ->
                val plantId = backStackEntry.arguments?.getString("plantId") ?: return@composable
                val plant = GrowDataStore.plants.find { it.id == plantId }
                if (plant != null) {
                    PlantDetailScreen(plant = plant, onBack = { navController.popBackStack() })
                } else {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Pflanze nicht gefunden")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { navController.popBackStack() }) { Text("Zurück") }
                        }
                    }
                }
            }
            composable(TopLevelDestination.SETTINGS.route) {
                SettingsScreen(
                    themeManager = themeManager,
                    languageManager = languageManager
                )
            }
        }
    }
}

enum class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    private val titleStringMap: Map<com.growtracker.app.ui.language.Language, String>
) {
    OVERVIEW(
        route = "overview",
    icon = Icons.Filled.Home,
        titleStringMap = Strings.navigation_overview
    ),
    GROW(
        route = "grow",
    icon = Icons.Filled.Spa,
        titleStringMap = Strings.navigation_grow
    ),
    SETTINGS(
        route = "settings",
    icon = Icons.Filled.Settings,
        titleStringMap = Strings.navigation_settings
    );
    
    fun getTitle(languageManager: LanguageManager): String {
        return titleStringMap[languageManager.currentLanguage] ?: titleStringMap[com.growtracker.app.ui.language.Language.ENGLISH] ?: ""
    }
}
