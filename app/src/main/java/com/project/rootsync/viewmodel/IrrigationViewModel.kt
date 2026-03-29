package com.project.rootsync.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.rootsync.data.model.SensorReading
import com.project.rootsync.data.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * UI state for irrigation screen.
 */
data class IrrigationUiState(
    val isLoading: Boolean = true,
    val deviceId: String? = null,
    val chartData: List<Pair<Float, Float>> = emptyList(), // x, y pairs
    val error: String? = null
)

/**
 * ViewModel for irrigation screen.
 */
@HiltViewModel
class IrrigationViewModel @Inject constructor(
    private val sensorRepository: SensorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IrrigationUiState())
    val uiState: StateFlow<IrrigationUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            // TODO: Get deviceId from DataStore
            val deviceId = "current-device-id"

            if (deviceId == null) {
                _uiState.value = IrrigationUiState(
                    isLoading = false,
                    deviceId = null,
                    chartData = emptyList()
                )
                return@launch
            }

            // Fetch history for last 7 days
            val now = java.time.LocalDateTime.now()
            val cutoff = now.minusDays(7).toString()

            val result = runCatching {
                sensorRepository.getReadingsForRange(deviceId, cutoff, now.toString())
            }

            result.onSuccess { history ->
                val processed = processHistory(history)
                _uiState.value = IrrigationUiState(
                    isLoading = false,
                    deviceId = deviceId,
                    chartData = processed.spots,
                    error = processed.error
                )
            }.onFailure { e ->
                Log.e("IrrigationVM", "Error loading history", e)
                _uiState.value = IrrigationUiState(
                    isLoading = false,
                    deviceId = deviceId,
                    error = "Failed to load data."
                )
            }
        }
    }

    private data class ProcessedResult(val spots: List<Pair<Float, Float>>, val error: String?)

    private fun processHistory(history: List<SensorReading>): ProcessedResult {
        val now = LocalDateTime.now()
        val spots = mutableListOf<Pair<Float, Float>>()
        var invalidRows = 0

        for (row in history) {
            // Parse date
            val recordedAt = try {
                LocalDateTime.parse(row.createdAt)
            } catch (e: Exception) {
                null
            }

            val moisture = row.soilMoisture.toDouble()

            if (recordedAt == null) {
                invalidRows++
                continue
            }

            // Filter logic
            if (recordedAt.isBefore(now.minusDays(7)) || recordedAt.isAfter(now)) {
                continue
            }

            // Calculate X and Y
            val minutesAgo = ChronoUnit.MINUTES.between(recordedAt, now).toDouble()
            val daysAgo = minutesAgo / (60 * 24)

            val x = (7.0 - daysAgo).coerceIn(0.0, 7.0).toFloat()
            val y = moisture.coerceIn(0.0, 100.0).toFloat()

            spots.add(Pair(x, y))
        }

        // Sort by X
        val sortedSpots = spots.sortedBy { it.first }

        // Determine error
        val error = if (sortedSpots.isEmpty() && history.isNotEmpty() && invalidRows == history.size) {
            "Could not read moisture history from the available records."
        } else {
            null
        }

        return ProcessedResult(sortedSpots, error)
    }
}
