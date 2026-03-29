package com.project.rootsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.rootsync.data.repository.AuthRepository
import com.project.rootsync.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for link device screen.
 */
data class LinkDeviceUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLinked: Boolean = false
)

/**
 * ViewModel for link device screen.
 */
@HiltViewModel
class LinkDeviceViewModel @Inject constructor(
    private val deviceRepo: DeviceRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LinkDeviceUiState())
    val uiState: StateFlow<LinkDeviceUiState> = _uiState.asStateFlow()

    fun linkDevice(uuid: String, deviceName: String) {
        val trimmedUuid = uuid.trim()
        val trimmedName = deviceName.trim()

        // UUID validation
        if (!trimmedUuid.matches(uuidRegex)) {
            _uiState.update { it.copy(error = "Please enter a valid device UUID.") }
            return
        }

        // Name validation
        if (trimmedName.isEmpty()) {
            _uiState.update { it.copy(error = "Please enter a device name.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val userId = authRepo.getCurrentUserId()
                if (userId == null) {
                    _uiState.update { it.copy(error = "User not authenticated", isLoading = false) }
                    return@launch
                }

                val device = deviceRepo.claimDevice(trimmedUuid, userId, trimmedName)

                if (device == null) {
                    _uiState.update {
                        it.copy(
                            error = "Device not found. Check the UUID and try again.",
                            isLoading = false
                        )
                    }
                } else {
                    // Success
                    _uiState.update { it.copy(isLoading = false, isLinked = true) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Failed to link device: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun resetError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        // UUID validation: 8-4-4-4-12 hex characters
        private val uuidRegex = Regex(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
        )
    }
}
