package com.project.rootsync.ui.screens.irrigation

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
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.project.rootsync.viewmodel.IrrigationViewModel
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale

/**
 * Main irrigation screen composable.
 * Shows soil moisture trend chart for the last 7 days.
 */
@Composable
fun IrrigationScreen(
    navController: NavController,
    viewModel: IrrigationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                item {
                    ChartCard(uiState = uiState)
                }
            }
        }
    }
}

@Composable
private fun ChartCard(
    uiState: com.project.rootsync.viewmodel.IrrigationUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.WaterDrop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Soil Moisture Trend (%)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Chart body
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    uiState.error != null -> {
                        ErrorView(message = uiState.error)
                    }
                    uiState.chartData.isEmpty() -> {
                        EmptyState(deviceId = uiState.deviceId)
                    }
                    else -> {
                        MoistureChart(chartData = uiState.chartData)
                    }
                }
            }
        }
    }
}

@Composable
private fun MoistureChart(
    chartData: List<Pair<Float, Float>>
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    // Convert chart data to Vico entries
    val xValues = chartData.map { it.first }
    val yValues = chartData.map { it.second }

    // Create entry model
    val entryModel = remember(chartData) {
        val entries = chartData.mapIndexed { index, pair ->
            com.patrykandpatrick.vico.core.entry.entryOf(pair.first, pair.second)
        }
        entryModelOf(entries)
    }

    Chart(
        chart = lineChart(),
        model = entryModel,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(
            valueFormatter = { value, _ ->
                // Convert days (0-7) to day names
                val daysAgo = 7 - value.toInt()
                if (daysAgo in 0..7) {
                    val day = LocalDateTime.now().minusDays(daysAgo.toLong()).dayOfWeek
                    day.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                } else ""
            }
        ),
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun EmptyState(deviceId: String?) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.BarChart,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (deviceId == null) "No device linked" else "No recent data",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (deviceId == null)
                "Link your ESP32 device to view irrigation history."
            else
                "No soil moisture readings were found for the last 7 days.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
        )
    }
}

@Composable
private fun ErrorView(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
    }
}
