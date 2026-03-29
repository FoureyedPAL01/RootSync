package com.project.rootsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.rootsync.util.SaveStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
 */
@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _saveStatus = MutableStateFlow(SaveStatus.IDLE)
    val saveStatus: StateFlow<SaveStatus> = _saveStatus.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        // TODO: Load from DataStore
        _uiState.value = SettingsUiState()
    }

    fun updateThemeMode(pref: ThemePref) {
        _uiState.update { it.copy(themeMode = pref) }
        // TODO: Save to DataStore
    }

    fun updateNotificationSetting(key: String, enabled: Boolean) {
        viewModelScope.launch {
            _saveStatus.value = SaveStatus.SAVING

            when (key) {
                "pump_alerts" -> _uiState.update { it.copy(pumpAlerts = enabled) }
                "soil_moisture_alerts" -> _uiState.update { it.copy(soilMoistureAlerts = enabled) }
                "weather_alerts" -> _uiState.update { it.copy(weatherAlerts = enabled) }
                "fertigation_reminders" -> _uiState.update { it.copy(fertigationReminders = enabled) }
                "device_offline_alerts" -> _uiState.update { it.copy(deviceOfflineAlerts = enabled) }
                "weekly_summary" -> _uiState.update { it.copy(weeklySummary = enabled) }
            }

            // TODO: Save to DataStore
            delay(600) // Brief visual feedback
            _saveStatus.value = SaveStatus.IDLE
        }
    }
}
