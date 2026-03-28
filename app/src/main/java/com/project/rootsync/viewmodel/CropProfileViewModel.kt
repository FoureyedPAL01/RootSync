package com.project.rootsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.rootsync.data.model.CropProfile
import com.project.rootsync.data.repository.CropProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CropProfileUiState(
    val isLoading: Boolean = true,
    val profiles: List<CropProfile> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class CropProfileViewModel @Inject constructor(
    private val cropProfileRepository: CropProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CropProfileUiState())
    val uiState: StateFlow<CropProfileUiState> = _uiState.asStateFlow()

    fun loadProfiles(deviceId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val profiles = cropProfileRepository.getProfilesForDevice(deviceId)
                _uiState.value = _uiState.value.copy(
                    profiles = profiles,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun addProfile(profile: CropProfile) {
        viewModelScope.launch {
            try {
                cropProfileRepository.insertProfile(profile)
                loadProfiles(profile.deviceId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun deleteProfile(profileId: String, deviceId: String) {
        viewModelScope.launch {
            try {
                cropProfileRepository.deleteProfile(profileId)
                loadProfiles(deviceId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
