package com.project.rootsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.rootsync.data.model.Device
import com.project.rootsync.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeviceUiState(
    val isLoading: Boolean = true,
    val devices: List<Device> = emptyList(),
    val selectedDevice: Device? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceUiState())
    val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

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

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
