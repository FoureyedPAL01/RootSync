package com.project.rootsync.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.project.rootsync.data.model.SensorReading
import com.project.rootsync.util.PumpStatus
import com.project.rootsync.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val liveReading by viewModel.liveReading.collectAsState()

    // TODO: Get deviceId from DataStore or auth state
    val deviceId = "default-device"

    LaunchedEffect(deviceId) {
        viewModel.loadDashboard(deviceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = { viewModel.refresh(deviceId) }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live status indicator
                if (liveReading != null) {
                    Text(
                        text = "Live",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Sensor cards row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SensorValueCard(
                        icon = Icons.Rounded.Opacity,
                        label = "Soil Moisture",
                        value = "${uiState.sensorReading?.soilMoisture?.toInt() ?: 0}%",
                        modifier = Modifier.weight(1f)
                    )
                    SensorValueCard(
                        icon = Icons.Rounded.Thermostat,
                        label = "Temperature",
                        value = "${uiState.sensorReading?.temperature?.toInt() ?: 0}°C",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Sensor cards row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SensorValueCard(
                        icon = Icons.Rounded.Air,
                        label = "Humidity",
                        value = "${uiState.sensorReading?.humidity?.toInt() ?: 0}%",
                        modifier = Modifier.weight(1f)
                    )
                    SensorValueCard(
                        icon = if (uiState.sensorReading?.isRaining == true)
                            Icons.Rounded.WaterDrop else Icons.Rounded.Warning,
                        label = "Rain",
                        value = if (uiState.sensorReading?.isRaining == true) "Yes" else "No",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Pump control card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (uiState.pumpStatus) {
                            PumpStatus.RUNNING -> MaterialTheme.colorScheme.primaryContainer
                            PumpStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = when (uiState.pumpStatus) {
                                    PumpStatus.RUNNING -> MaterialTheme.colorScheme.primary
                                    PumpStatus.ERROR -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pump Control",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Status: ${uiState.pumpStatus.name}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (uiState.pumpStatus == PumpStatus.RUNNING) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { viewModel.togglePump(deviceId, true) },
                                modifier = Modifier.weight(1f),
                                enabled = uiState.pumpStatus != PumpStatus.RUNNING
                            ) {
                                Text("Start Pump")
                            }
                            FilledTonalButton(
                                onClick = { viewModel.togglePump(deviceId, false) },
                                modifier = Modifier.weight(1f),
                                enabled = uiState.pumpStatus == PumpStatus.RUNNING
                            ) {
                                Text("Stop Pump")
                            }
                        }
                    }
                }

                // Error message
                if (uiState.errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                // Recent readings summary
                if (uiState.recentReadings.isNotEmpty()) {
                    Text(
                        text = "Recent Readings (${uiState.recentReadings.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Last updated: ${uiState.recentReadings.lastOrNull()?.createdAt ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SensorValueCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
