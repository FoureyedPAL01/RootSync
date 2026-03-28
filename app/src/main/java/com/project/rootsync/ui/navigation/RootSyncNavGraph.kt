package com.project.rootsync.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.project.rootsync.ui.screens.AlertsScreen
import com.project.rootsync.ui.screens.DashboardScreen
import com.project.rootsync.ui.screens.LoginScreen
import com.project.rootsync.ui.screens.WeatherScreen
import com.project.rootsync.ui.screens.crop.CropProfilesScreen
import com.project.rootsync.ui.screens.device.DeviceChoiceScreen
import com.project.rootsync.ui.screens.device.DeviceManagementScreen
import com.project.rootsync.ui.screens.device.LinkDeviceScreen
import com.project.rootsync.ui.screens.fertigation.FertigationScreen
import com.project.rootsync.ui.screens.irrigation.IrrigationScreen
import com.project.rootsync.ui.screens.more.MoreScreen
import com.project.rootsync.ui.screens.settings.PreferencesScreen
import com.project.rootsync.ui.screens.settings.ProfileScreen
import com.project.rootsync.ui.screens.settings.SettingsScreen
import com.project.rootsync.ui.screens.waterusage.WaterUsageScreen

@Composable
fun RootSyncNavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(Screen.Login.route) { LoginScreen(navController = navController) }
        composable(Screen.Dashboard.route) { DashboardScreen(navController = navController) }
        composable(Screen.Alerts.route) { AlertsScreen(navController = navController) }
        composable(Screen.Weather.route) { WeatherScreen(navController = navController) }
        composable(Screen.DeviceManagement.route) { DeviceManagementScreen(navController = navController) }
        composable(Screen.LinkDevice.route) { LinkDeviceScreen(navController = navController) }
        composable(Screen.DeviceChoice.route) { DeviceChoiceScreen(navController = navController) }
        composable(Screen.CropProfiles.route) { CropProfilesScreen(navController = navController) }
        composable(Screen.Irrigation.route) { IrrigationScreen(navController = navController) }
        composable(Screen.WaterUsage.route) { WaterUsageScreen(navController = navController) }
        composable(Screen.Fertigation.route) { FertigationScreen(navController = navController) }
        composable(Screen.Profile.route) { ProfileScreen(navController = navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController = navController) }
        composable(Screen.Preferences.route) { PreferencesScreen(navController = navController) }
        composable(Screen.More.route) { MoreScreen(navController = navController) }
    }
}
