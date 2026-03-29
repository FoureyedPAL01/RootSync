package com.project.rootsync.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.project.rootsync.ui.components.SettingsSection
import com.project.rootsync.ui.components.ThemeRadioGroup
import com.project.rootsync.ui.components.ToggleSettingTile
import com.project.rootsync.viewmodel.SettingsViewModel

/**
 * Main settings screen composable.
 */
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val saveStatus by viewModel.saveStatus.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 36.dp)
    ) {
        // ── Appearance ────────────────────────────────────────────────
        item {
            SettingsSection(
                title = "Appearance",
                leadingIcon = Icons.Outlined.Palette
            ) {
                ThemeRadioGroup(
                    selected = state.themeMode,
                    onSelected = viewModel::updateThemeMode
                )
            }
        }

        // ── Notifications ─────────────────────────────────────────────
        item {
            SettingsSection(
                title = "Notifications",
                leadingIcon = Icons.Outlined.Notifications
            ) {
                Column {
                    ToggleSettingTile(
                        title = "Pump Alerts",
                        subtitle = "Notify on pump start/stop",
                        icon = Icons.Outlined.PowerSettingsNew,
                        checked = state.pumpAlerts,
                        saveStatus = saveStatus,
                        onCheckedChange = { viewModel.updateNotificationSetting("pump_alerts", it) }
                    )

                    ToggleSettingTile(
                        title = "Soil Moisture Alerts",
                        subtitle = "Critical moisture level warnings",
                        icon = Icons.Outlined.Eco,
                        checked = state.soilMoistureAlerts,
                        saveStatus = saveStatus,
                        onCheckedChange = { viewModel.updateNotificationSetting("soil_moisture_alerts", it) }
                    )

                    ToggleSettingTile(
                        title = "Weather Alerts",
                        subtitle = "Rain detection & irrigation skips",
                        icon = Icons.Outlined.CloudQueue,
                        checked = state.weatherAlerts,
                        saveStatus = saveStatus,
                        onCheckedChange = { viewModel.updateNotificationSetting("weather_alerts", it) }
                    )

                    ToggleSettingTile(
                        title = "Fertigation Reminders",
                        subtitle = "Nutrient injection scheduling",
                        icon = Icons.Outlined.Science,
                        checked = state.fertigationReminders,
                        saveStatus = saveStatus,
                        onCheckedChange = { viewModel.updateNotificationSetting("fertigation_reminders", it) }
                    )

                    ToggleSettingTile(
                        title = "Device Offline Alerts",
                        subtitle = "Network connectivity warnings",
                        icon = Icons.Outlined.WifiOff,
                        checked = state.deviceOfflineAlerts,
                        saveStatus = saveStatus,
                        onCheckedChange = { viewModel.updateNotificationSetting("device_offline_alerts", it) }
                    )

                    ToggleSettingTile(
                        title = "Weekly Summary Report",
                        subtitle = "Email digest of water usage",
                        icon = Icons.Outlined.BarChart,
                        checked = state.weeklySummary,
                        saveStatus = saveStatus,
                        onCheckedChange = { viewModel.updateNotificationSetting("weekly_summary", it) }
                    )
                }
            }
        }
    }
}
