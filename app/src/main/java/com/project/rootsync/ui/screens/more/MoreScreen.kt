package com.project.rootsync.ui.screens.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeviceHub
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.project.rootsync.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("More") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            item {
                NavigationListItem(
                    icon = Icons.Rounded.DeviceHub,
                    title = "Devices",
                    subtitle = "Manage your ESP32 devices",
                    onClick = { navController.navigate(Screen.DeviceManagement.route) }
                )
            }
            item {
                NavigationListItem(
                    icon = Icons.Rounded.Grass,
                    title = "Crop Profiles",
                    subtitle = "Set moisture & rain thresholds",
                    onClick = { navController.navigate(Screen.CropProfiles.route) }
                )
            }
            item {
                NavigationListItem(
                    icon = Icons.Rounded.WaterDrop,
                    title = "Irrigation",
                    subtitle = "View pump logs",
                    onClick = { navController.navigate(Screen.Irrigation.route) }
                )
            }
            item {
                NavigationListItem(
                    icon = Icons.Rounded.Notifications,
                    title = "Alerts",
                    subtitle = "View system alerts",
                    onClick = { navController.navigate(Screen.Alerts.route) }
                )
            }
            item {
                NavigationListItem(
                    icon = Icons.Rounded.Tune,
                    title = "Preferences",
                    subtitle = "Units & notifications",
                    onClick = { navController.navigate(Screen.Preferences.route) }
                )
            }
            item {
                NavigationListItem(
                    icon = Icons.Rounded.Settings,
                    title = "Settings",
                    subtitle = "Device settings",
                    onClick = { navController.navigate(Screen.Settings.route) }
                )
            }
            item {
                NavigationListItem(
                    icon = Icons.Rounded.Person,
                    title = "Profile",
                    subtitle = "Account & sign out",
                    onClick = { navController.navigate(Screen.Profile.route) }
                )
            }
        }
    }
}

@Composable
private fun NavigationListItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(Icons.Rounded.ChevronRight, contentDescription = null)
        }
    )
}
