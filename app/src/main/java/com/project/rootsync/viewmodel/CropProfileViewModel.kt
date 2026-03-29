package com.project.rootsync.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.rootsync.data.model.CropProfile
import com.project.rootsync.data.repository.AuthRepository
import com.project.rootsync.data.repository.CropProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for crop profiles screen.
 */
data class CropProfileUiState(
    val isLoading: Boolean = true,
    val profiles: List<CropProfile> = emptyList(),
    val activeProfileId: Int? = null,
    val fetchingIds: Set<Int> = emptySet(),
    val errorMessage: String? = null
)

/**
 * ViewModel for crop profile management.
 */
@HiltViewModel
class CropProfileViewModel @Inject constructor(
    private val cropRepo: CropProfileRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CropProfileUiState())
    val uiState: StateFlow<CropProfileUiState> = _uiState.asStateFlow()

    private var deviceId: String? = null
    private var userId: String? = null

    init {
        viewModelScope.launch {
            // TODO: Get userId and deviceId from auth state / DataStore
            userId = "current-user-id"
            deviceId = "current-device-id"
            loadProfiles()
        }
    }

    fun loadProfiles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val uid = userId ?: return@launch
                val profiles = cropRepo.getProfilesForUser(uid)

                val activeId = deviceId?.let { cropRepo.getActiveProfileId(it) }

                _uiState.value = _uiState.value.copy(
                    profiles = profiles,
                    activeProfileId = activeId,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("CropProfileVM", "Error loading profiles", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun setActiveProfile(profileId: Int) {
        viewModelScope.launch {
            deviceId?.let { did ->
                try {
                    cropRepo.setActiveProfile(did, profileId)
                    _uiState.value = _uiState.value.copy(activeProfileId = profileId)
                } catch (e: Exception) {
                    Log.e("CropProfileVM", "Error setting active profile", e)
                }
            }
        }
    }

    fun deleteProfile(profile: CropProfile) {
        viewModelScope.launch {
            try {
                cropRepo.deleteProfile(profile.id)

                if (_uiState.value.activeProfileId == profile.id) {
                    deviceId?.let { did ->
                        cropRepo.clearActiveProfile(did)
                    }
                    _uiState.value = _uiState.value.copy(activeProfileId = null)
                }

                loadProfiles()
            } catch (e: Exception) {
                Log.e("CropProfileVM", "Error deleting profile", e)
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun fetchPerenualData(profile: CropProfile) {
        viewModelScope.launch {
            if (profile.plantName.isEmpty()) return@launch

            _uiState.value = _uiState.value.copy(
                fetchingIds = _uiState.value.fetchingIds + profile.id
            )

            try {
                cropRepo.fetchPerenualData(profile.id, profile.plantName)
                loadProfiles()
            } catch (e: Exception) {
                Log.e("CropProfileVM", "Error fetching perenual data", e)
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(
                    fetchingIds = _uiState.value.fetchingIds - profile.id
                )
            }
        }
    }

    fun saveProfile(
        profileId: Int? = null,
        name: String,
        plantName: String,
        threshold: Int,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val uid = userId ?: return@launch

                if (profileId != null) {
                    // Update existing
                    val updates = mutableMapOf<String, Any?>(
                        "name" to name,
                        "plant_name" to plantName,
                        "min_moisture" to threshold
                    )

                    // Clear perenual data if plant name changed
                    val existing = _uiState.value.profiles.find { it.id == profileId }
                    if (existing != null && existing.plantName != plantName) {
                        updates["perenual_data"] = null
                        updates["perenual_species_id"] = null
                        updates["perenual_cached_at"] = null
                    }

                    cropRepo.updateProfile(profileId, updates)
                } else {
                    // Create new
                    val profileData = mapOf(
                        "user_id" to uid,
                        "name" to name,
                        "plant_name" to plantName,
                        "min_moisture" to threshold
                    )
                    cropRepo.insertProfile(profileData)
                }

                loadProfiles()
                onComplete()
            } catch (e: Exception) {
                Log.e("CropProfileVM", "Error saving profile", e)
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
