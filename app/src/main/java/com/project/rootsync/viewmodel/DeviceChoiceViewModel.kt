package com.project.rootsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.rootsync.data.repository.DeviceRepository
import com.project.rootsync.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for device choice screen.
 */
@HiltViewModel
class DeviceChoiceViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Expose device ID as a StateFlow
    private val _deviceId = MutableStateFlow<String?>(null)
    val deviceId: StateFlow<String?> = _deviceId.asStateFlow()

    init {
        // Check for existing device on init
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    val devices = deviceRepository.getDevicesForUser(userId)
                    _deviceId.value = devices.firstOrNull()?.id
                }
            } catch (e: Exception) {
                _deviceId.value = null
            }
        }
    }

    /**
     * Called when user chooses to continue with existing device.
     */
    fun onContinue() {
        // Navigation is handled in the Composable
        // Just mark that we've processed the choice
    }

    /**
     * Called when user chooses to link a different device.
     */
    fun onLinkDifferent() {
        // Navigation is handled in the Composable
        // Just mark that we've processed the choice
    }
}
