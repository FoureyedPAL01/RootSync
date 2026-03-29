package com.project.rootsync.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.rootsync.data.model.PumpLog
import com.project.rootsync.data.repository.PumpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.abs

/**
 * Daily log data class for aggregated water usage data.
 */
data class DailyLog(
    val date: LocalDate,
    val waterLitres: Double,
    val runtimeMinutes: Int,
    val efficiency: Double
)

/**
 * UI state for water usage screen.
 */
data class WaterUsageUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val allLogs: List<DailyLog> = emptyList(),
    val weeklyData: List<DailyLog> = emptyList()
)

/**
 * ViewModel for water usage screen.
 */
@HiltViewModel
class WaterUsageViewModel @Inject constructor(
    private val pumpRepository: PumpRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WaterUsageUiState())
    val uiState: StateFlow<WaterUsageUiState> = _uiState.asStateFlow()

    private val _volumeUnit = MutableStateFlow("liters")
    val volumeUnit: StateFlow<String> = _volumeUnit.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // TODO: Get deviceId from DataStore
                val deviceId = "current-device-id"

                val cutoff14 = LocalDate.now().minusDays(14).toString()
                val pumpLogs = pumpRepository.getPumpLogsSince(deviceId, cutoff14)

                // Aggregate logs by day
                val dailyLogs = aggregateLogsByDay(pumpLogs)

                // Build 7-day week data (zero-filled for empty days)
                val today = LocalDate.now()
                val weekData = (0..6).map { daysAgo ->
                    val date = today.minusDays((6 - daysAgo).toLong())
                    dailyLogs.find { it.date == date } ?: DailyLog(
                        date = date,
                        waterLitres = 0.0,
                        runtimeMinutes = 0,
                        efficiency = 0.0
                    )
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allLogs = dailyLogs.sortedByDescending { log -> log.date },
                        weeklyData = weekData
                    )
                }
            } catch (e: Exception) {
                Log.e("WaterUsageVM", "Error loading data", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load data: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Sets the volume unit preference.
     */
    fun setVolumeUnit(unit: String) {
        _volumeUnit.value = unit
    }

    private fun aggregateLogsByDay(logs: List<PumpLog>): List<DailyLog> {
        data class Accumulator(
            val date: LocalDate,
            var water: Double = 0.0,
            var runtimeSecs: Int = 0,
            var rainDetected: Boolean = false,
            val moistureBefore: MutableList<Int> = mutableListOf(),
            val moistureAfter: MutableList<Int> = mutableListOf()
        )

        val accMap = mutableMapOf<LocalDate, Accumulator>()

        logs.forEach { log ->
            try {
                val date = LocalDate.parse(log.pumpOnAt.take(10))
                val acc = accMap.getOrPut(date) { Accumulator(date) }

                acc.water += log.waterUsedLitres ?: 0.0
                acc.runtimeSecs += log.durationSeconds ?: 0
                if (log.rainDetected == true) acc.rainDetected = true
                log.moistureBefore?.let { acc.moistureBefore.add(it) }
                log.moistureAfter?.let { acc.moistureAfter.add(it) }
            } catch (e: Exception) {
                Log.e("WaterUsageVM", "Error parsing log", e)
            }
        }

        return accMap.values.map { acc ->
            val runtimeMinutes = acc.runtimeSecs / 60
            val efficiency = calculateEfficiency(
                waterLitres = acc.water,
                runtimeMinutes = runtimeMinutes,
                moistureBefore = acc.moistureBefore.averageOrNull(),
                moistureAfter = acc.moistureAfter.averageOrNull(),
                rainDetected = acc.rainDetected
            )

            DailyLog(
                date = acc.date,
                waterLitres = acc.water,
                runtimeMinutes = runtimeMinutes,
                efficiency = efficiency
            )
        }
    }

    private fun calculateEfficiency(
        waterLitres: Double,
        runtimeMinutes: Int,
        moistureBefore: Double?,
        moistureAfter: Double?,
        rainDetected: Boolean
    ): Double {
        // Moisture score (40%)
        val mScore = if (moistureBefore != null && moistureAfter != null) {
            val gain = (moistureAfter - moistureBefore).coerceIn(0.0, 40.0)
            (gain / 40.0) * 100.0
        } else {
            50.0
        }

        // Water rate score (40%)
        val wScore = if (runtimeMinutes > 0) {
            val lpm = waterLitres / runtimeMinutes
            ((1.0 - abs(lpm - 1.5) / 3.0).coerceIn(0.0, 1.0)) * 100.0
        } else {
            70.0
        }

        // Rain bonus (20%)
        val rainBonus = if (rainDetected) 100.0 else 0.0

        return (0.4 * mScore + 0.4 * wScore + 0.2 * rainBonus).coerceIn(0.0, 100.0)
    }

    private fun List<Int>.averageOrNull(): Double? {
        return if (isNotEmpty()) average() else null
    }
}
