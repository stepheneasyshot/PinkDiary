package com.stephen.pinkdiary.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.stephen.pinkdiary.PinkdiaryApp
import com.stephen.pinkdiary.R
import com.stephen.pinkdiary.ui.home.HomeScreen
import com.stephen.pinkdiary.ui.home.HomeViewModel
import com.stephen.pinkdiary.ui.knowledge.KnowledgeScreen
import com.stephen.pinkdiary.ui.settings.SettingsScreen
import com.stephen.pinkdiary.ui.settings.SettingsViewModel

object Routes {
    const val PERIOD = "period"
    const val KNOWLEDGE = "knowledge"
    const val SETTINGS = "settings"
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun PinkdiaryNavHost(app: PinkdiaryApp) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem(Routes.PERIOD, stringResource(R.string.nav_period), Icons.Filled.CalendarMonth),
        BottomNavItem(Routes.KNOWLEDGE, stringResource(R.string.nav_knowledge), Icons.AutoMirrored.Filled.MenuBook),
        BottomNavItem(Routes.SETTINGS, stringResource(R.string.nav_settings), Icons.Filled.Settings)
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.PERIOD,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.PERIOD) {
                val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(app))
                HomeScreen(viewModel = viewModel)
            }
            composable(Routes.KNOWLEDGE) {
                KnowledgeScreen()
            }
            composable(Routes.SETTINGS) {
                val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(app))
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
