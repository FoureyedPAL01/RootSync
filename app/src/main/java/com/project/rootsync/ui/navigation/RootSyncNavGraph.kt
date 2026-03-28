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
        composable(Screen.DeviceManagement.route) {
            // TODO: DeviceManagementScreen(navController)
        }
        composable(Screen.LinkDevice.route) {
            // TODO: LinkDeviceScreen(navController)
        }
        composable(Screen.DeviceChoice.route) {
            // TODO: DeviceChoiceScreen(navController)
        }
        composable(Screen.CropProfiles.route) {
            // TODO: CropProfilesScreen(navController)
        }
        composable(Screen.Irrigation.route) {
            // TODO: IrrigationScreen(navController)
        }
        composable(Screen.WaterUsage.route) {
            // TODO: WaterUsageScreen(navController)
        }
        composable(Screen.Fertigation.route) {
            // TODO: FertigationScreen(navController)
        }
        composable(Screen.Profile.route) {
            // TODO: ProfileScreen(navController)
        }
        composable(Screen.Settings.route) {
            // TODO: SettingsScreen(navController)
        }
        composable(Screen.Preferences.route) {
            // TODO: PreferencesScreen(navController)
        }
        composable(Screen.More.route) {
            // TODO: MoreScreen(navController)
        }
    }
}
