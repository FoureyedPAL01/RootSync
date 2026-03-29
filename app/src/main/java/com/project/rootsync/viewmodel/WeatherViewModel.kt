package com.project.rootsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.rootsync.data.model.WeatherData
import com.project.rootsync.data.model.WeatherUnits
import com.project.rootsync.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * UI state for the Weather screen.
 */
sealed class WeatherUiState {
    data object Loading : WeatherUiState()
    data class Success(val data: WeatherData) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

/**
 * ViewModel for the Weather screen.
 * Manages weather data fetching, caching, and user unit preferences.
 */
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _units = MutableStateFlow(WeatherUnits())
    val units: StateFlow<WeatherUnits> = _units.asStateFlow()

    private var cachedData: WeatherData? = null
    private var cacheTimestamp: Instant? = null

    // User's location (these would come from DataStore/Preferences in production)
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    companion object {
        private val CACHE_DURATION = Duration.ofMinutes(10)
    }

    init {
        load()
    }

    /**
     * Loads weather data, using cache if available and not expired.
     */
    fun load(force: Boolean = false) {
        // Check cache validity
        if (!force && cachedData != null && cacheTimestamp != null) {
            val elapsed = Duration.between(cacheTimestamp, Instant.now())
            if (elapsed < CACHE_DURATION) {
                _uiState.value = WeatherUiState.Success(cachedData!!)
                return
            }
        }

        _uiState.update { WeatherUiState.Loading }

        viewModelScope.launch {
            val result = weatherRepository.fetchWeatherData(latitude, longitude)
            result.fold(
                onSuccess = { data ->
                    cachedData = data
                    cacheTimestamp = Instant.now()
                    _uiState.update { WeatherUiState.Success(data) }
                },
                onFailure = { error ->
                    _uiState.update { WeatherUiState.Error(error.message ?: "Unknown error") }
                }
            )
        }
    }

    /**
     * Forces a refresh of weather data.
     */
    fun refresh() = load(force = true)

    /**
     * Updates user's temperature unit preference.
     */
    fun setTempUnit(unit: String) {
        _units.update { it.copy(tempUnit = unit) }
    }

    /**
     * Updates user's wind unit preference.
     */
    fun setWindUnit(unit: String) {
        _units.update { it.copy(windUnit = unit) }
    }

    /**
     * Updates user's precipitation unit preference.
     */
    fun setPrecipUnit(unit: String) {
        _units.update { it.copy(precipUnit = unit) }
    }

    /**
     * Sets the user's location for weather queries.
     */
    fun setLocation(lat: Double, lon: Double) {
        latitude = lat
        longitude = lon
        // Invalidate cache when location changes
        cachedData = null
        cacheTimestamp = null
    }
}
