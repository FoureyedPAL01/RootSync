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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.project.rootsync.data.remote.HourlyData
import com.project.rootsync.data.repository.WeatherRepository
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    navController: NavController,
    weatherRepository: WeatherRepository = hiltViewModel<WeatherRepoViewModel>().weatherRepository
) {
    var hourlyData by remember { mutableStateOf<HourlyData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // TODO: Get location from user profile or DataStore
    val latitude = 19.076  // Mumbai default
    val longitude = 72.877

    LaunchedEffect(Unit) {
        isLoading = true
        weatherRepository.getWeatherData(latitude, longitude)
            .onSuccess { response ->
                hourlyData = response.hourly
                isLoading = false
            }
            .onFailure { e ->
                errorMessage = e.message
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Weather Forecast") })
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text("Error: $errorMessage") }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Current weather card
                item {
                    val currentTemp = hourlyData?.temperature_2m?.firstOrNull() ?: 0.0
                    val currentHumidity = hourlyData?.relative_humidity_2m?.firstOrNull() ?: 0.0
                    val currentRain = hourlyData?.precipitation?.firstOrNull() ?: 0.0

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("Current Conditions", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                WeatherStat(
                                    icon = Icons.Rounded.Thermostat,
                                    label = "Temperature",
                                    value = "${String.format("%.1f", currentTemp)}°C"
                                )
                                WeatherStat(
                                    icon = Icons.Rounded.Air,
                                    label = "Humidity",
                                    value = "${currentHumidity.toInt()}%"
                                )
                                WeatherStat(
                                    icon = Icons.Rounded.WaterDrop,
                                    label = "Rain",
                                    value = "${String.format("%.1f", currentRain)}mm"
                                )
                            }
                        }
                    }
                }

                // Evapotranspiration
                item {
                    val currentET = hourlyData?.et0_fao_evapotranspiration?.firstOrNull() ?: 0.0
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Evapotranspiration (ET₀)", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "${String.format("%.2f", currentET)} mm/hr",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Hourly forecast
                item {
                    Text("Hourly Forecast", style = MaterialTheme.typography.titleMedium)
                }

                val hours = hourlyData?.time?.size ?: 0
                itemsIndexed(
                    (0 until minOf(hours, 24)).toList()
                ) { _, index ->
                    val time = hourlyData?.time?.getOrNull(index) ?: ""
                    val temp = hourlyData?.temperature_2m?.getOrNull(index) ?: 0.0
                    val humidity = hourlyData?.relative_humidity_2m?.getOrNull(index) ?: 0.0
                    val rain = hourlyData?.precipitation?.getOrNull(index) ?: 0.0

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                time.substringAfter("T", time),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.width(60.dp)
                            )
                            Icon(Icons.Rounded.Thermostat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("${temp.toInt()}°", modifier = Modifier.width(40.dp))
                            Icon(Icons.Rounded.Air, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("${humidity.toInt()}%", modifier = Modifier.width(40.dp))
                            Icon(Icons.Rounded.WaterDrop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("${String.format("%.1f", rain)}mm")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@dagger.hilt.android.lifecycle.HiltViewModel
class WeatherRepoViewModel @javax.inject.Inject constructor(
    val weatherRepository: WeatherRepository
) : androidx.lifecycle.ViewModel()
