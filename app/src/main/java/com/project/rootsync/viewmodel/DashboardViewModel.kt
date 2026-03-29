package com.project.rootsync.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.rootsync.data.model.SensorReading
import com.project.rootsync.data.repository.CropProfileRepository
import com.project.rootsync.data.repository.DeviceRepository
import com.project.rootsync.data.repository.SensorRepository
import com.project.rootsync.service.MqttService
import com.project.rootsync.util.Enums
import com.project.rootsync.util.PumpStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * Data class for chart points.
 */
data class ChartPoint(
    val time: Instant,
    val value: Double
)

/**
 * UI state for the dashboard screen.
 */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val deviceId: String? = null,
    val latestSensorData: Map<String, Any?> = emptyMap(),
    val sensorHistory: List<Map<String, Any>> = emptyList(),
    val activeCropProfile: Map<String, Any?>? = null,
    val tempUnit: Enums.TemperatureUnit = Enums.TemperatureUnit.CELSIUS,
    val pumpStatus: PumpStatus = PumpStatus.IDLE,
    val errorMessage: String? = null
)

/**
 * ViewModel for the dashboard screen.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val cropProfileRepository: CropProfileRepository,
    private val deviceRepository: DeviceRepository,
    private val mqttService: MqttService,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _liveReading = MutableStateFlow<SensorReading?>(null)
    val liveReading: StateFlow<SensorReading?> = _liveReading.asStateFlow()

    init {
        viewModelScope.launch {
            loadInitialData()
        }
    }

    private suspend fun loadInitialData() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        try {
            // TODO: Get deviceId from DataStore
            val deviceId = "current-device-id"

            // Load latest sensor data
            val latest = sensorRepository.getLatestReading(deviceId)
            val latestMap = if (latest != null) mapOf(
                "soil_moisture" to latest.soilMoisture.toDouble(),
                "temperature_c" to latest.temperature.toDouble(),
                "humidity" to latest.humidity.toDouble(),
                "flow_litres" to latest.flowRate?.toDouble(),
                "rain_detected" to latest.isRaining,
                "recorded_at" to latest.createdAt
            ) else emptyMap()

            // Load history (last 1 hour)
            val now = Instant.now()
            val oneHourAgo = now.minus(Duration.ofHours(1))
            val history = try {
                sensorRepository.getReadingsForRange(
                    deviceId = deviceId,
                    from = oneHourAgo.toString(),
                    to = now.toString()
                ).map { reading ->
                    mapOf(
                        "soil_moisture" to reading.soilMoisture.toDouble(),
                        "temperature_c" to reading.temperature.toDouble(),
                        "humidity" to reading.humidity.toDouble(),
                        "recorded_at" to reading.createdAt
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                deviceId = deviceId,
                latestSensorData = latestMap,
                sensorHistory = history
            )

            // Subscribe to realtime updates
            subscribeToRealtime(deviceId)

        } catch (e: Exception) {
            Log.e("DashboardVM", "Error loading data", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = e.message
            )
        }
    }

    private fun subscribeToRealtime(deviceId: String) {
        viewModelScope.launch {
            sensorRepository.sensorReadingFlow(deviceId)
                .catch { e ->
                    Log.e("DashboardVM", "Realtime error: ${e.message}")
                }
                .collect { reading ->
                    _liveReading.value = reading
                    val latestMap = mapOf(
                        "soil_moisture" to reading.soilMoisture.toDouble(),
                        "temperature_c" to reading.temperature.toDouble(),
                        "humidity" to reading.humidity.toDouble(),
                        "flow_litres" to reading.flowRate?.toDouble(),
                        "rain_detected" to reading.isRaining,
                        "recorded_at" to reading.createdAt
                    )
                    _uiState.value = _uiState.value.copy(
                        latestSensorData = latestMap,
                        sensorHistory = (_uiState.value.sensorHistory + mapOf(
                            "soil_moisture" to reading.soilMoisture.toDouble(),
                            "temperature_c" to reading.temperature.toDouble(),
                            "humidity" to reading.humidity.toDouble(),
                            "recorded_at" to reading.createdAt
                        )).takeLast(24)
                    )
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            loadInitialData()
        }
    }

    fun togglePump(turnOn: Boolean) {
        viewModelScope.launch {
            try {
                val deviceId = _uiState.value.deviceId ?: return@launch
                mqttService.publishPumpCommand(deviceId, turnOn)

                // Also log command to Supabase
                supabase.postgrest["device_commands"]
                    .insert(
                        mapOf(
                            "device_id" to deviceId,
                            "command" to if (turnOn) "pump_on" else "pump_off"
                        )
                    )

                _uiState.value = _uiState.value.copy(
                    pumpStatus = if (turnOn) PumpStatus.RUNNING else PumpStatus.IDLE
                )
            } catch (e: Exception) {
                Log.e("DashboardVM", "Failed to toggle pump: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    pumpStatus = PumpStatus.ERROR,
                    errorMessage = "Failed to send pump command"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        mqttService.disconnect()
    }
}
