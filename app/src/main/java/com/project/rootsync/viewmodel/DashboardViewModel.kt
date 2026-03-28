package com.project.rootsync.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.rootsync.data.model.SensorReading
import com.project.rootsync.data.repository.SensorRepository
import com.project.rootsync.service.MqttService
import com.project.rootsync.util.PumpStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val sensorReading: SensorReading? = null,
    val recentReadings: List<SensorReading> = emptyList(),
    val pumpStatus: PumpStatus = PumpStatus.IDLE,
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val mqttService: MqttService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    val liveReading: StateFlow<SensorReading?> get() = _liveReading
    private val _liveReading = MutableStateFlow<SensorReading?>(null)

    private var currentDeviceId: String? = null

    fun loadDashboard(deviceId: String) {
        if (currentDeviceId == deviceId) return
        currentDeviceId = deviceId

        _uiState.value = _uiState.value.copy(isLoading = true)

        // Load latest reading
        viewModelScope.launch {
            try {
                val latest = sensorRepository.getLatestReading(deviceId)
                _uiState.value = _uiState.value.copy(
                    sensorReading = latest,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }

        // Load recent readings for charts
        viewModelScope.launch {
            try {
                val recent = sensorRepository.getRecentReadings(deviceId, limit = 24)
                _uiState.value = _uiState.value.copy(recentReadings = recent)
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Failed to load recent readings: ${e.message}")
            }
        }

        // Subscribe to realtime updates
        subscribeToRealtime(deviceId)
    }

    private fun subscribeToRealtime(deviceId: String) {
        viewModelScope.launch {
            sensorRepository.sensorReadingFlow(deviceId)
                .catch { e ->
                    Log.e("DashboardViewModel", "Realtime error: ${e.message}")
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                }
                .collect { reading ->
                    _liveReading.value = reading
                    _uiState.value = _uiState.value.copy(
                        sensorReading = reading,
                        recentReadings = (_uiState.value.recentReadings + reading).takeLast(24)
                    )
                }
        }
    }

    fun togglePump(deviceId: String, turnOn: Boolean) {
        viewModelScope.launch {
            try {
                mqttService.publishPumpCommand(deviceId, turnOn)
                _uiState.value = _uiState.value.copy(
                    pumpStatus = if (turnOn) PumpStatus.RUNNING else PumpStatus.IDLE
                )
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Failed to toggle pump: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    pumpStatus = PumpStatus.ERROR,
                    errorMessage = "Failed to send pump command"
                )
            }
        }
    }

    fun connectMqtt(host: String, port: Int, username: String, password: String) {
        viewModelScope.launch {
            mqttService.connect(host, port, username, password)
        }
    }

    fun refresh(deviceId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadDashboard(deviceId)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        mqttService.disconnect()
    }
}
