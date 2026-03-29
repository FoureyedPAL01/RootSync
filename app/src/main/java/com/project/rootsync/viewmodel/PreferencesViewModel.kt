package com.project.rootsync.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for preferences screen.
 */
data class PreferencesUiState(
    // Units
    val tempUnit: String = "celsius",
    val volumeUnit: String = "litres",
    val windUnit: String = "km/h",
    val precipitationUnit: String = "mm",
    val aqiType: String = "us",

    // Location
    val locationLat: String = "",
    val locationLon: String = "",
    val timezone: String = "UTC",

    // UI State
    val isEditingLocation: Boolean = false,
    val isSaving: Boolean = false,
    val latError: String? = null,
    val lonError: String? = null,
    val snackbarMessage: String? = null
)

/**
 * ViewModel for preferences screen.
 */
@HiltViewModel
class PreferencesViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PreferencesUiState())
    val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            // TODO: Load from DataStore or Supabase
            // For now, use defaults
            _uiState.update { state ->
                state.copy(
                    tempUnit = "celsius",
                    volumeUnit = "litres",
                    windUnit = "km/h",
                    precipitationUnit = "mm",
                    aqiType = "us",
                    locationLat = "",
                    locationLon = "",
                    timezone = "UTC"
                )
            }
        }
    }

    fun updateTempUnit(unit: String) {
        _uiState.update { it.copy(tempUnit = unit) }
        // TODO: Save to DataStore
    }

    fun updateVolumeUnit(unit: String) {
        _uiState.update { it.copy(volumeUnit = unit) }
        // TODO: Save to DataStore
    }

    fun updateWindUnit(unit: String) {
        _uiState.update { it.copy(windUnit = unit) }
        // TODO: Save to DataStore
    }

    fun updatePrecipitationUnit(unit: String) {
        _uiState.update { it.copy(precipitationUnit = unit) }
        // TODO: Save to DataStore
    }

    fun updateAqiType(type: String) {
        _uiState.update { it.copy(aqiType = type) }
        // TODO: Save to DataStore
    }

    fun setEditingLocation(editing: Boolean) {
        _uiState.update {
            it.copy(
                isEditingLocation = editing,
                latError = null,
                lonError = null
            )
        }
    }

    fun onLatChange(lat: String) {
        _uiState.update { it.copy(locationLat = lat, latError = null) }
    }

    fun onLonChange(lon: String) {
        _uiState.update { it.copy(locationLon = lon, lonError = null) }
    }

    fun saveLocation() {
        val state = _uiState.value
        val latErr = validateCoord(state.locationLat, isLat = true)
        val lonErr = validateCoord(state.locationLon, isLat = false)

        if (latErr != null || lonErr != null) {
            _uiState.update { it.copy(latError = latErr, lonError = lonErr) }
            return
        }

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            // TODO: Save to DataStore
            _uiState.update {
                it.copy(
                    isSaving = false,
                    isEditingLocation = false,
                    snackbarMessage = "Location saved."
                )
            }
        }
    }

    fun cancelLocationEdit() {
        loadPreferences()
        _uiState.update { it.copy(isEditingLocation = false, latError = null, lonError = null) }
    }

    fun onSnackbarShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun validateCoord(value: String, isLat: Boolean): String? {
        val parsed = value.toDoubleOrNull()
        if (parsed == null) return "Enter a valid number"
        if (isLat && (parsed < -90 || parsed > 90)) {
            return "Latitude must be -90 to 90"
        }
        if (!isLat && (parsed < -180 || parsed > 180)) {
            return "Longitude must be -180 to 180"
        }
        return null
    }
}
