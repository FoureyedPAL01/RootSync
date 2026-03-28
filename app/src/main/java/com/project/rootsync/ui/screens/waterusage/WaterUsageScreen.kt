package com.project.rootsync.ui.screens.waterusage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.project.rootsync.data.model.PumpLog
import com.project.rootsync.data.repository.PumpRepository
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterUsageScreen(
    navController: NavController,
    pumpRepository: PumpRepository = hiltViewModel<PumpRepoViewModel>().pumpRepository
) {
    var pumpLogs by remember { mutableStateOf<List<PumpLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var totalWater by remember { mutableDoubleStateOf(0.0) }

    // TODO: Get deviceId from DataStore
    val deviceId = "default-device"

    LaunchedEffect(deviceId) {
        isLoading = true
        pumpLogs = pumpRepository.getPumpLogs(deviceId, limit = 30)
        totalWater = pumpLogs.sumOf { (it.waterUsedLiters ?: 0f).toDouble() }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Water Usage") })
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Total Water", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "${String.format("%.1f", totalWater)}L",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Sessions", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "${pumpLogs.size}",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }

                // Chart
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Daily Usage", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (pumpLogs.isNotEmpty()) {
                            val chartData = pumpLogs.take(7).reversed()
                                .mapIndexed { index, log ->
                                    (log.waterUsedLiters ?: 0f).toDouble()
                                }
                                .toFloatArray()

                            Chart(
                                chart = columnChart(),
                                model = entryModelOf(*chartData),
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis(),
                                modifier = Modifier.fillMaxWidth().height(200.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No data available")
                            }
                        }
                    }
                }

                // Efficiency score placeholder
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Efficiency Score", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Good",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Based on water usage vs. irrigation cycles",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@dagger.hilt.android.lifecycle.HiltViewModel
class PumpRepoViewModel @javax.inject.Inject constructor(
    val pumpRepository: PumpRepository
) : androidx.lifecycle.ViewModel()
