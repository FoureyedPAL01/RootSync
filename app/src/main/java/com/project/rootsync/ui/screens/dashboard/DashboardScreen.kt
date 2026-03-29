package com.project.rootsync.ui.screens.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Umbrella
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.project.rootsync.util.Enums
import com.project.rootsync.util.PumpStatus
import com.project.rootsync.viewmodel.ChartPoint
import com.project.rootsync.viewmodel.DashboardViewModel
import java.time.Duration
import java.time.Instant

// Sensor accent colors
private val CMoisture = Color(0xFF4A90E2)
private val CTemp = Color(0xFFE87722)
private val CHumid = Color(0xFF7C6FCD)
private val CFlow = Color(0xFF00B4C4)
private val ColorOn = Color(0xFF2D9D5C)
private val ColorOff = Color(0xFFEE4E4E)

/**
 * Main dashboard screen composable.
 */
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val liveReading by viewModel.liveReading.collectAsState()
    val colors = MaterialTheme.colorScheme
    val bg = colors.surfaceContainerHighest
    val comp = colors.surfaceContainerLow
    val text = colors.onSurface
    val muted = text.copy(alpha = 0.45f)

    var isRefreshing by remember { mutableStateOf(false) }

    // Extract sensor data
    val sensorData = extractSensorData(uiState)

    Scaffold(
        containerColor = bg
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.refresh()
                isRefreshing = false
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }

                uiState.deviceId == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No device linked to this account.",
                            style = TextStyle(color = text, fontSize = 15.sp)
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 36.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Live indicator
                        if (liveReading != null) {
                            Text(
                                text = "● Live",
                                style = TextStyle(
                                    color = colors.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        // Sensor cards grid
                        SensorCardsGrid(
                            comp = comp,
                            text = text,
                            moisture = sensorData.moisture,
                            tempC = sensorData.tempC,
                            humidity = sensorData.humidity,
                            flowRate = sensorData.flowRate,
                            tempUnit = uiState.tempUnit
                        )

                        // Pump and Rain row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PumpCard(
                                modifier = Modifier.weight(1f),
                                comp = comp,
                                text = text,
                                muted = muted,
                                isRunning = uiState.pumpStatus == PumpStatus.RUNNING,
                                onToggle = { viewModel.togglePump(it) }
                            )
                            RainCard(
                                modifier = Modifier.weight(1f),
                                comp = comp,
                                text = text,
                                muted = muted,
                                isRaining = sensorData.isRaining
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

/**
 * Extracted sensor data from UI state.
 */
private data class ExtractedSensorData(
    val moisture: Double?,
    val tempC: Double?,
    val humidity: Double?,
    val flowRate: Double?,
    val isRaining: Boolean,
    val moistureHistory: List<ChartPoint>,
    val tempHistory: List<ChartPoint>,
    val humidHistory: List<ChartPoint>
)

@Composable
private fun extractSensorData(
    uiState: com.project.rootsync.viewmodel.DashboardUiState
): ExtractedSensorData {
    val d = uiState.latestSensorData
    val history = uiState.sensorHistory

    // Stale check — 5 minutes
    val lastUpdated: Instant? = (d["recorded_at"] as? String)?.let {
        try {
            Instant.parse(it)
        } catch (_: Exception) {
            null
        }
    }
    val staleLimit = Duration.ofMinutes(5)
    val isStale = lastUpdated == null || Duration.between(lastUpdated, Instant.now()) > staleLimit

    val moisture = if (!isStale) (d["soil_moisture"] as? Double) else null
    val tempC = if (!isStale) (d["temperature_c"] as? Double) else null
    val humidity = if (!isStale) (d["humidity"] as? Double) else null
    val flowRate = if (!isStale) (d["flow_litres"] as? Double) else null
    val isRaining = !isStale && (d["rain_detected"] as? Boolean) == true

    // Build chart points from history
    val moistureHistory = buildChartFromHistory(history, "soil_moisture")
    val tempHistory = buildChartFromHistory(history, "temperature_c")
    val humidHistory = buildChartFromHistory(history, "humidity")

    return ExtractedSensorData(
        moisture = moisture,
        tempC = tempC,
        humidity = humidity,
        flowRate = flowRate,
        isRaining = isRaining,
        moistureHistory = moistureHistory,
        tempHistory = tempHistory,
        humidHistory = humidHistory
    )
}

private fun buildChartFromHistory(
    history: List<Map<String, Any>>,
    field: String
): List<ChartPoint> {
    if (history.isEmpty()) return emptyList()
    return history.mapNotNull { row ->
        val time = (row["recorded_at"] as? String)?.let {
            try {
                Instant.parse(it)
            } catch (_: Exception) {
                null
            }
        } ?: return@mapNotNull null
        val value = (row[field] as? Number)?.toDouble() ?: return@mapNotNull null
        ChartPoint(time, value)
    }
}

/**
 * Sensor cards grid (2x2 layout).
 */
@Composable
private fun SensorCardsGrid(
    comp: Color,
    text: Color,
    moisture: Double?,
    tempC: Double?,
    humidity: Double?,
    flowRate: Double?,
    tempUnit: Enums.TemperatureUnit
) {
    val tempDisplay = if (tempC != null) {
        when (tempUnit) {
            Enums.TemperatureUnit.FAHRENHEIT -> "%.1f°F".format(tempC * 9.0 / 5.0 + 32)
            else -> "%.1f°C".format(tempC)
        }
    } else {
        "—"
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SensorCard(
                modifier = Modifier.weight(1f),
                comp = comp,
                label = "Moisture",
                value = moisture?.let { "%.1f%%".format(it) } ?: "—",
                accentColor = CMoisture,
                icon = Icons.Rounded.WaterDrop,
                progress = ((moisture ?: 0.0) / 100.0).coerceIn(0.0, 1.0).toFloat()
            )
            SensorCard(
                modifier = Modifier.weight(1f),
                comp = comp,
                label = "Temperature",
                value = tempDisplay,
                accentColor = CTemp,
                icon = Icons.Rounded.Thermostat,
                progress = ((tempC ?: 0.0) / 50.0).coerceIn(0.0, 1.0).toFloat()
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SensorCard(
                modifier = Modifier.weight(1f),
                comp = comp,
                label = "Humidity",
                value = humidity?.let { "%.1f%%".format(it) } ?: "—",
                accentColor = CHumid,
                icon = Icons.Rounded.Air,
                progress = ((humidity ?: 0.0) / 100.0).coerceIn(0.0, 1.0).toFloat()
            )
            SensorCard(
                modifier = Modifier.weight(1f),
                comp = comp,
                label = "Flow rate",
                value = flowRate?.let { "%.1f L/m".format(it) } ?: "—",
                accentColor = CFlow,
                icon = Icons.Rounded.GraphicEq,
                progress = ((flowRate ?: 0.0) / 10.0).coerceIn(0.0, 1.0).toFloat()
            )
        }
    }
}

/**
 * Individual sensor card with radial gauge.
 */
@Composable
private fun SensorCard(
    modifier: Modifier = Modifier,
    comp: Color,
    label: String,
    value: String,
    accentColor: Color,
    icon: ImageVector,
    progress: Float
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 900),
        label = "gauge"
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = comp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Radial gauge
            Box(
                modifier = Modifier.size(50.dp),
                contentAlignment = Alignment.Center
            ) {
                // Track
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(50.dp),
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round,
                    trackColor = Color.Transparent
                )
                // Progress
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(50.dp),
                    strokeWidth = 4.dp,
                    color = accentColor,
                    strokeCap = StrokeCap.Round,
                    trackColor = Color.Transparent
                )
                // Icon
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Value
            Text(
                text = value,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Label
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            )
        }
    }
}

/**
 * Pump control card with start/stop functionality.
 */
@Composable
private fun PumpCard(
    modifier: Modifier = Modifier,
    comp: Color,
    text: Color,
    muted: Color,
    isRunning: Boolean,
    onToggle: (Boolean) -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var confirmStart by remember { mutableStateOf(false) }

    val btnColor = if (isRunning) ColorOn else ColorOff

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    if (confirmStart) "Start Pump" else "Stop Pump",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (confirmStart) {
                        "Start the pump manually?\nAuto-irrigation will be bypassed.\nPump stops automatically after 2 minutes."
                    } else {
                        "Stop the pump and end manual override?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onToggle(confirmStart)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (confirmStart) ColorOn else ColorOff
                    )
                ) {
                    Text(if (confirmStart) "Start" else "Stop")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = comp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: label + status
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "PUMP",
                    style = TextStyle(
                        color = muted,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    if (isRunning) "RUNNING" else "IDLE",
                    style = TextStyle(
                        color = text,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    if (isRunning) "Tap to stop" else "Tap to start",
                    style = TextStyle(
                        color = muted,
                        fontSize = 10.sp
                    )
                )
            }

            // Right: power button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(btnColor)
                    .padding(0.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PowerSettingsNew,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

/**
 * Rain status card.
 */
@Composable
private fun RainCard(
    modifier: Modifier = Modifier,
    comp: Color,
    text: Color,
    muted: Color,
    isRaining: Boolean
) {
    val rainingIconColor = Color(0xFF2196F3)
    val dryIconColor = Color(0xFF888888)
    val iconColor = if (isRaining) rainingIconColor else dryIconColor
    val iconBgColor = if (isRaining) rainingIconColor.copy(alpha = 0.15f) else dryIconColor.copy(alpha = 0.1f)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = comp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "RAIN STATUS",
                    style = TextStyle(
                        color = muted,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    if (isRaining) "RAINING" else "NO RAIN",
                    style = TextStyle(
                        color = text,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRaining) Icons.Rounded.Umbrella else Icons.Rounded.CloudOff,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
