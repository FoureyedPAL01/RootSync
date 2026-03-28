package com.project.rootsync.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object DeviceManagement : Screen("devices")
    object LinkDevice : Screen("devices/link")
    object DeviceChoice : Screen("devices/choice")
    object CropProfiles : Screen("crop-profiles")
    object Irrigation : Screen("irrigation")
    object WaterUsage : Screen("water-usage")
    object Weather : Screen("weather")
    object Alerts : Screen("alerts")
    object Fertigation : Screen("fertigation")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Preferences : Screen("preferences")
    object More : Screen("more")
}
