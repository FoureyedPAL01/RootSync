package com.project.rootsync.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.rootsync.data.model.Alert
import com.project.rootsync.data.repository.AlertRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for alerts screen.
 */
data class AlertsUiState(
    val alerts: List<Alert> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedAlertType: String? = null
)

/**
 * ViewModel for alerts screen.
 */
@HiltViewModel
class AlertViewModel @Inject constructor(
    private val alertRepository: AlertRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    init {
        // TODO: Get deviceId from DataStore
        loadAlerts("current-device-id")
    }

    fun loadAlerts(deviceId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val alerts = alertRepository.fetchAlerts(deviceId)
                _uiState.value = _uiState.value.copy(
                    alerts = alerts,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("AlertViewModel", "Error loading alerts", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun setFilter(type: String?) {
        _uiState.value = _uiState.value.copy(selectedAlertType = type)
    }

    fun resolveAlert(id: Long) {
        val currentList = _uiState.value.alerts
        val index = currentList.indexOfFirst { it.id == id }
        if (index == -1) return

        // Optimistic update locally
        val updatedList = currentList.toMutableList()
        updatedList[index] = updatedList[index].copy(resolved = true)
        _uiState.value = _uiState.value.copy(alerts = updatedList)

        // Persist in background
        viewModelScope.launch {
            try {
                alertRepository.resolveAlert(id)
            } catch (e: Exception) {
                // Revert on failure
                _uiState.value = _uiState.value.copy(alerts = currentList)
                Log.e("AlertViewModel", "Error resolving alert", e)
            }
        }
    }

    fun deleteAll(deviceId: String) {
        val snapshot = _uiState.value.alerts

        // Optimistic clear
        _uiState.value = _uiState.value.copy(alerts = emptyList())

        viewModelScope.launch {
            try {
                alertRepository.deleteAllAlerts(deviceId)
            } catch (e: Exception) {
                // Revert on failure
                _uiState.value = _uiState.value.copy(alerts = snapshot)
                Log.e("AlertViewModel", "Error deleting alerts", e)
            }
        }
    }

    fun refresh() {
        // TODO: Get deviceId from DataStore
        loadAlerts("current-device-id")
    }
}
