package com.project.rootsync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.project.rootsync.data.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
 * Loads from and saves to DataStore.
 */
@HiltViewModel
class PreferencesViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val prefs = UserPreferencesDataStore(application)

    private val _isEditingLocation = MutableStateFlow(false)
    private val _isSaving = MutableStateFlow(false)
    private val _latError = MutableStateFlow<String?>(null)
    private val _lonError = MutableStateFlow<String?>(null)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    // Load from DataStore
    val uiState: StateFlow<PreferencesUiState> = combine(
        prefs.tempUnitFlow,
        prefs.volumeUnitFlow,
        prefs.windUnitFlow,
        prefs.precipitationUnitFlow,
        prefs.aqiTypeFlow,
        prefs.locationLatFlow,
        prefs.locationLonFlow,
        prefs.timezoneFlow,
        _isEditingLocation,
        _isSaving,
        _latError,
        _lonError,
        _snackbarMessage
    ) { values ->
        PreferencesUiState(
            tempUnit = values[0] as String,
            volumeUnit = values[1] as String,
            windUnit = values[2] as String,
            precipitationUnit = values[3] as String,
            aqiType = values[4] as String,
            locationLat = values[5] as String,
            locationLon = values[6] as String,
            timezone = values[7] as String,
            isEditingLocation = values[8] as Boolean,
            isSaving = values[9] as Boolean,
            latError = values[10] as String?,
            lonError = values[11] as String?,
            snackbarMessage = values[12] as String?
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreferencesUiState())

    // Save to DataStore
    fun updateTempUnit(unit: String) {
        viewModelScope.launch { prefs.saveTempUnit(unit) }
    }

    fun updateVolumeUnit(unit: String) {
        viewModelScope.launch { prefs.saveVolumeUnit(unit) }
    }

    fun updateWindUnit(unit: String) {
        viewModelScope.launch { prefs.saveWindUnit(unit) }
    }

    fun updatePrecipitationUnit(unit: String) {
        viewModelScope.launch { prefs.savePrecipitationUnit(unit) }
    }

    fun updateAqiType(type: String) {
        viewModelScope.launch { prefs.saveAqiType(type) }
    }

    fun setEditingLocation(editing: Boolean) {
        _isEditingLocation.update { editing }
        _latError.update { null }
        _lonError.update { null }
    }

    fun onLatChange(lat: String) {
        _uiStateTempLat = lat
        _latError.update { null }
    }

    fun onLonChange(lon: String) {
        _uiStateTempLon = lon
        _lonError.update { null }
    }

    // Temporary storage for lat/lon during editing
    private var _uiStateTempLat = ""
    private var _uiStateTempLon = ""

    fun saveLocation() {
        val lat = uiState.value.locationLat.ifEmpty { _uiStateTempLat }
        val lon = uiState.value.locationLon.ifEmpty { _uiStateTempLon }
        val latErr = validateCoord(lat, isLat = true)
        val lonErr = validateCoord(lon, isLat = false)

        if (latErr != null || lonErr != null) {
            _latError.update { latErr }
            _lonError.update { lonErr }
            return
        }

        _isSaving.update { true }
        viewModelScope.launch {
            prefs.saveLocation(lat, lon)
            _isSaving.update { false }
            _isEditingLocation.update { false }
            _snackbarMessage.update { "Location saved." }
        }
    }

    fun cancelLocationEdit() {
        _isEditingLocation.update { false }
        _latError.update { null }
        _lonError.update { null }
    }

    fun onSnackbarShown() {
        _snackbarMessage.update { null }
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
