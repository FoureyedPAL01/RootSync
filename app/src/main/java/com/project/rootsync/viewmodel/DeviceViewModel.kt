package com.project.rootsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.rootsync.data.model.Device
import com.project.rootsync.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for device list and selection.
 */
data class DeviceUiState(
    val isLoading: Boolean = true,
    val devices: List<Device> = emptyList(),
    val selectedDevice: Device? = null,
    val errorMessage: String? = null
)

/**
 * UI state for device management operations.
 */
data class DeviceManagementUiState(
    val isSavingName: Boolean = false,
    val isUnlinking: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel for device management.
 */
@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceUiState())
    val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

    private val _managementState = MutableStateFlow(DeviceManagementUiState())
    val managementState: StateFlow<DeviceManagementUiState> = _managementState.asStateFlow()

    fun loadDevices(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val devices = deviceRepository.getDevicesForUser(userId)
                _uiState.value = _uiState.value.copy(
                    devices = devices,
                    selectedDevice = devices.firstOrNull(),
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

    fun selectDevice(device: Device) {
        _uiState.value = _uiState.value.copy(selectedDevice = device)
    }

    fun linkDevice(deviceId: String, userId: String, name: String, location: String?) {
        viewModelScope.launch {
            try {
                deviceRepository.linkDevice(deviceId, userId, name, location)
                loadDevices(userId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun deleteDevice(deviceId: String, userId: String) {
        viewModelScope.launch {
            try {
                deviceRepository.deleteDevice(deviceId)
                loadDevices(userId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    /**
     * Updates the device name in Supabase.
     */
    fun updateDeviceName(deviceId: String, newName: String) {
        viewModelScope.launch {
            _managementState.value = _managementState.value.copy(isSavingName = true)
            try {
                supabase.postgrest["devices"]
                    .update(mapOf("name" to newName)) {
                        filter {
                            eq("id", deviceId)
                        }
                    }

                // Update local state
                val currentDevice = _uiState.value.selectedDevice
                if (currentDevice != null && currentDevice.id == deviceId) {
                    _uiState.value = _uiState.value.copy(
                        selectedDevice = currentDevice.copy(name = newName)
                    )
                }

                // Update in devices list
                val updatedDevices = _uiState.value.devices.map { device ->
                    if (device.id == deviceId) device.copy(name = newName) else device
                }
                _uiState.value = _uiState.value.copy(devices = updatedDevices)

                _managementState.value = _managementState.value.copy(isSavingName = false)
            } catch (e: Exception) {
                _managementState.value = _managementState.value.copy(
                    isSavingName = false,
                    errorMessage = e.message
                )
                throw e
            }
        }
    }

    /**
     * Unlinks the device from the user.
     */
    fun unlinkDevice(deviceId: String) {
        viewModelScope.launch {
            _managementState.value = _managementState.value.copy(isUnlinking = true)
            try {
                supabase.postgrest["devices"]
                    .update(mapOf("user_id" to null, "claimed_at" to null)) {
                        filter {
                            eq("id", deviceId)
                        }
                    }

                // Remove from local list
                val updatedDevices = _uiState.value.devices.filter { it.id != deviceId }
                _uiState.value = _uiState.value.copy(
                    devices = updatedDevices,
                    selectedDevice = updatedDevices.firstOrNull()
                )

                _managementState.value = _managementState.value.copy(isUnlinking = false)
            } catch (e: Exception) {
                _managementState.value = _managementState.value.copy(
                    isUnlinking = false,
                    errorMessage = e.message
                )
                throw e
            }
        }
    }

    fun refreshDeviceStatus() {
        val userId = _uiState.value.selectedDevice?.userId
        if (userId != null) {
            loadDevices(userId)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
        _managementState.value = _managementState.value.copy(errorMessage = null)
    }
}
