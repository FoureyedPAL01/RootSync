package com.project.rootsync.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.rootsync.data.model.CropProfileWithPerenual
import com.project.rootsync.data.model.FertigationLog
import com.project.rootsync.data.repository.FertigationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

/**
 * UI state for fertigation screen.
 */
data class FertigationUiState(
    val isLoading: Boolean = true,
    val isFetching: Boolean = false,
    val isLogging: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val profile: CropProfileWithPerenual? = null,
    val plantData: JsonElement? = null,
    val careData: JsonElement? = null,
    val logs: List<FertigationLog> = emptyList()
)

/**
 * ViewModel for fertigation screen.
 */
@HiltViewModel
class FertigationViewModel @Inject constructor(
    private val fertigationRepo: FertigationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FertigationUiState())
    val uiState: StateFlow<FertigationUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // TODO: Get deviceId from DataStore
                val deviceId = "current-device-id"

                val profile = fertigationRepo.getActiveCropProfile(deviceId)
                val logs = fertigationRepo.getFertigationLogs(deviceId)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profile = profile,
                        plantData = profile?.perenualData,
                        careData = profile?.perenualCareData,
                        logs = logs
                    )
                }
            } catch (e: Exception) {
                Log.e("FertigationVM", "Error loading data", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun fetchPlantData() {
        val profile = _uiState.value.profile
        val plantName = profile?.plantName

        if (profile == null || plantName.isNullOrEmpty()) {
            _uiState.update { it.copy(error = "Set a plant name in Crop Profiles first.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isFetching = true) }

            try {
                fertigationRepo.fetchPlantData(profile.id, plantName)
                load() // Reload data
                _uiState.update { it.copy(successMessage = "Plant care data updated.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to fetch: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isFetching = false) }
            }
        }
    }

    fun logFertilizerApplication(notes: String) {
        // TODO: Get deviceId from DataStore
        val deviceId = "current-device-id"
        val profileId = _uiState.value.profile?.id

        viewModelScope.launch {
            _uiState.update { it.copy(isLogging = true) }

            try {
                fertigationRepo.logFertilizerApplication(deviceId, profileId, notes)
                load() // Reload logs
                _uiState.update { it.copy(successMessage = "Fertilizer application logged.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to log: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLogging = false) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
