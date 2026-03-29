package com.project.rootsync.ui.screens.waterusage

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.project.rootsync.util.UnitConverter
import com.project.rootsync.util.toStringAsFixed
import com.project.rootsync.viewmodel.DailyLog
import com.project.rootsync.viewmodel.WaterUsageViewModel
import java.time.format.DateTimeFormatter

// Theme colors for the water usage screen
private val Teal = Color(0xFF2E7D32)
private val Purple = Color(0xFF6650a4)
private val Amber = Color(0xFFFFA000)
private val Orange = Color(0xFFFF6D00)

/**
 * Main water usage screen composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterUsageScreen(
    viewModel: WaterUsageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val volumeUnit by viewModel.volumeUnit.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Water Usage") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error!!,
                        onRetry = { viewModel.refresh() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    WaterUsageContent(
                        weeklyData = uiState.weeklyData,
                        allLogs = uiState.allLogs,
                        volumeUnit = volumeUnit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun WaterUsageContent(
    weeklyData: List<DailyLog>,
    allLogs: List<DailyLog>,
    volumeUnit: String,
    modifier: Modifier = Modifier
) {
    // Calculate 7-day aggregates
    val totalWater = weeklyData.fold(0.0) { sum, log -> sum + log.waterLitres }
    val totalRuntime = weeklyData.fold(0) { sum, log -> sum + log.runtimeMinutes }
    val avgDailyWater = totalWater / 7.0
    val avgDailyRuntime = totalRuntime / 7

    val totalDisplay = UnitConverter.formatVolume(totalWater, volumeUnit)
    val avgDailyDisplay = UnitConverter.formatVolume(avgDailyWater, volumeUnit)
    val totalRuntimeHours = totalRuntime / 60.0

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stat cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    icon = Icons.Default.WaterDrop,
                    iconColor = MaterialTheme.colorScheme.primary,
                    value = totalDisplay,
                    label = "TOTAL WATER (7d)",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Rounded.TrendingUp,
                    iconColor = Teal,
                    value = avgDailyDisplay,
                    label = "AVG DAILY",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    icon = Icons.Default.Timer,
                    iconColor = Purple,
                    value = "${totalRuntimeHours.toStringAsFixed(1)} hrs",
                    label = "TOTAL RUNTIME (7d)",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Default.AccessTime,
                    iconColor = Amber,
                    value = "$avgDailyRuntime min",
                    label = "AVG RUNTIME/DAY",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Efficiency trend chart
        item {
            ChartCard(
                icon = Icons.Default.ShowChart,
                iconColor = MaterialTheme.colorScheme.primary,
                title = "Efficiency Trend",
                subtitle = "Last 7 days"
            ) {
                val entries = weeklyData.mapIndexed { index, log ->
                    index.toFloat() to log.efficiency.toFloat()
                }

                if (entries.isNotEmpty()) {
                    Chart(
                        chart = lineChart(),
                        model = entryModelOf(*entries.toTypedArray()),
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis(
                            valueFormatter = { value, _ ->
                                val index = value.toInt()
                                if (index in weeklyData.indices) {
                                    val day = weeklyData[index].date
                                    day.format(DateTimeFormatter.ofPattern("EEE"))
                                } else ""
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No data available")
                    }
                }
            }
        }

        // Pump runtime bar chart
        item {
            ChartCard(
                icon = Icons.Default.Timer,
                iconColor = Teal,
                title = "Pump Runtime per Day",
                subtitle = "Minutes"
            ) {
                val barEntries = weeklyData.mapIndexed { index, log ->
                    index.toFloat() to log.runtimeMinutes.toFloat()
                }

                if (barEntries.isNotEmpty()) {
                    Chart(
                        chart = columnChart(),
                        model = entryModelOf(*barEntries.toTypedArray()),
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis(
                            valueFormatter = { value, _ ->
                                val index = value.toInt()
                                if (index in weeklyData.indices) {
                                    val day = weeklyData[index].date
                                    day.format(DateTimeFormatter.ofPattern("EEE"))
                                } else ""
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No data available")
                    }
                }
            }
        }

        // Daily log table
        item {
            DailyLogTable(
                logs = allLogs,
                volumeUnit = volumeUnit
            )
        }

        item { Spacer(modifier = Modifier.height(36.dp)) }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    iconColor: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
private fun ChartCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
private fun DailyLogTable(
    logs: List<DailyLog>,
    volumeUnit: String
) {
    val mutedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Log",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Last 14 days",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = mutedColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Column headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DATE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = mutedColor,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(0.3f)
                )
                Text(
                    text = "WATER USED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = mutedColor,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(0.25f)
                )
                Text(
                    text = "RUNTIME",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = mutedColor,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(0.2f)
                )
                Text(
                    text = "EFFICIENCY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = mutedColor,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(0.25f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (logs.isEmpty()) {
                Text(
                    text = "No pump activity in the last 14 days.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = mutedColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                logs.forEach { log ->
                    DailyLogRow(
                        log = log,
                        volumeUnit = volumeUnit
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyLogRow(
    log: DailyLog,
    volumeUnit: String
) {
    val mutedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val dateStr = log.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val waterStr = UnitConverter.formatVolume(log.waterLitres, volumeUnit)
    val runtimeStr = if (log.runtimeMinutes >= 60) {
        "${(log.runtimeMinutes / 60.0).toStringAsFixed(1)} hrs"
    } else {
        "${log.runtimeMinutes} min"
    }
    val effPct = log.efficiency.toInt()

    val barColor = when {
        effPct >= 75 -> Teal
        effPct >= 50 -> Orange
        else -> MaterialTheme.colorScheme.error
    }

    Column {
        HorizontalDivider(
            color = mutedColor.copy(alpha = 0.2f),
            thickness = 1.dp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(0.3f)
            )
            Text(
                text = waterStr,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.weight(0.25f)
            )
            Text(
                text = runtimeStr,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = mutedColor
                ),
                modifier = Modifier.weight(0.2f)
            )
            Row(
                modifier = Modifier.weight(0.25f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { (log.efficiency / 100f).toFloat() },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = barColor,
                    trackColor = mutedColor.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$effPct%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = mutedColor
                    ),
                    modifier = Modifier.width(34.dp)
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}
