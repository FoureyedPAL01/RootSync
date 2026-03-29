package com.project.rootsync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.project.rootsync.data.UserPreferencesDataStore
import com.project.rootsync.util.SaveStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Theme preference enum.
 */
enum class ThemePref { LIGHT, DARK, SYSTEM }

/**
 * UI state for settings screen.
 */
data class SettingsUiState(
    val themeMode: ThemePref = ThemePref.SYSTEM,
    val pumpAlerts: Boolean = true,
    val soilMoistureAlerts: Boolean = true,
    val weatherAlerts: Boolean = true,
    val fertigationReminders: Boolean = true,
    val deviceOfflineAlerts: Boolean = true,
    val weeklySummary: Boolean = false,
    val saveStatus: SaveStatus = SaveStatus.IDLE
)

/**
 * ViewModel for settings screen.
 * Loads from and saves to DataStore.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val prefs = UserPreferencesDataStore(application)

    private val _saveStatus = MutableStateFlow(SaveStatus.IDLE)
    val saveStatus: StateFlow<SaveStatus> = _saveStatus.asStateFlow()

    // Load from DataStore
    val uiState: StateFlow<SettingsUiState> = combine(
        prefs.themeFlow,
        prefs.pumpAlertsFlow,
        prefs.soilMoistureAlertsFlow,
        prefs.weatherAlertsFlow,
        prefs.fertigationRemindersFlow,
        prefs.deviceOfflineAlertsFlow,
        prefs.weeklySummaryFlow
    ) { theme, pump, soil, weather, fert, offline, weekly ->
        SettingsUiState(
            themeMode = when (theme) {
                "light" -> ThemePref.LIGHT
                "dark" -> ThemePref.DARK
                else -> ThemePref.SYSTEM
            },
            pumpAlerts = pump,
            soilMoistureAlerts = soil,
            weatherAlerts = weather,
            fertigationReminders = fert,
            deviceOfflineAlerts = offline,
            weeklySummary = weekly
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    // Save theme to DataStore
    fun updateThemeMode(pref: ThemePref) {
        viewModelScope.launch {
            val themeStr = when (pref) {
                ThemePref.LIGHT -> "light"
                ThemePref.DARK -> "dark"
                ThemePref.SYSTEM -> "system"
            }
            prefs.saveTheme(themeStr)
        }
    }

    // Save notification toggle to DataStore
    fun updateNotificationSetting(key: String, enabled: Boolean) {
        viewModelScope.launch {
            _saveStatus.value = SaveStatus.SAVING

            when (key) {
                "pump_alerts" -> prefs.savePumpAlerts(enabled)
                "soil_moisture_alerts" -> prefs.saveSoilMoistureAlerts(enabled)
                "weather_alerts" -> prefs.saveWeatherAlerts(enabled)
                "fertigation_reminders" -> prefs.saveFertigationReminders(enabled)
                "device_offline_alerts" -> prefs.saveDeviceOfflineAlerts(enabled)
                "weekly_summary" -> prefs.saveWeeklySummary(enabled)
            }

            delay(600) // Brief visual feedback
            _saveStatus.value = SaveStatus.IDLE
        }
    }
}
