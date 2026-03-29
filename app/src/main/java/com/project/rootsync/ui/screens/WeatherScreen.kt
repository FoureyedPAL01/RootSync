package com.project.rootsync.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.rootsync.data.remote.AqiHelper
import com.project.rootsync.data.remote.DewPointCalculator
import com.project.rootsync.data.remote.UvHelper
import com.project.rootsync.data.remote.WeatherDateHelper
import com.project.rootsync.data.remote.WeatherUnitConverter
import com.project.rootsync.data.remote.WindDirectionHelper
import com.project.rootsync.data.remote.WmoWeatherHelper
import com.project.rootsync.ui.screens.weather.AqiTile
import com.project.rootsync.ui.screens.weather.DailyForecastSection
import com.project.rootsync.ui.screens.weather.HeroCard
import com.project.rootsync.ui.screens.weather.HourlyStrip
import com.project.rootsync.ui.screens.weather.HumidityTile
import com.project.rootsync.ui.screens.weather.PrecipTile
import com.project.rootsync.ui.screens.weather.UvTile
import com.project.rootsync.ui.screens.weather.WindTile
import com.project.rootsync.viewmodel.WeatherUiState
import com.project.rootsync.viewmodel.WeatherViewModel

/**
 * Main Weather screen composable.
 * Displays current weather, hourly forecast, 7-day forecast, and weather conditions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val units by viewModel.units.collectAsState()

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Weather Forecast") })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            when (val state = uiState) {
                is WeatherUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                is WeatherUiState.Error -> {
                    WeatherErrorContent(
                        message = state.message,
                        onRetry = viewModel::refresh
                    )
                }

                is WeatherUiState.Success -> {
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = viewModel::refresh
                    ) {
                        WeatherContent(
                            data = state.data,
                            units = units,
                            modifier = Modifier.verticalScroll(scrollState)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

@Composable
private fun WeatherContent(
    data: com.project.rootsync.data.model.WeatherData,
    units: com.project.rootsync.data.model.WeatherUnits,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val startIdx = WeatherDateHelper.nowHourIndex(data.hourlyTimes)

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        // 1 — Hero card
        HeroCard(
            date = WeatherDateHelper.todayFormatted(),
            temp = WeatherUnitConverter.formatTemp(data.temperature, units.tempUnit),
            maxTemp = WeatherUnitConverter.formatTemp(data.todayMax, units.tempUnit),
            minTemp = WeatherUnitConverter.formatTemp(data.todayMin, units.tempUnit),
            feels = WeatherUnitConverter.formatTemp(data.feelsLike, units.tempUnit),
            condition = WmoWeatherHelper.wmoLabel(data.weatherCode),
            weatherCode = data.weatherCode,
            isDay = data.isDay,
            willRain = data.willRain,
            colors = colors
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2 — Hourly strip
        HourlyStrip(
            times = data.hourlyTimes,
            temps = data.hourlyTemps,
            codes = data.hourlyCodes,
            precipitations = data.hourlyPrecip,
            startIndex = startIdx,
            sunrise = data.sunrise,
            sunset = data.sunset,
            tempUnit = units.tempUnit,
            colors = colors
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 3 — Daily forecast
        DailyForecastSection(
            forecast = data.forecast7,
            tempUnit = units.tempUnit,
            colors = colors
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 4 — Conditions grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HumidityTile(
                humidity = data.humidity,
                tempC = data.temperature,
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            WindTile(
                speedKmh = data.windSpeed,
                gustsKmh = data.windGusts,
                directionDeg = data.windDirection,
                windUnit = units.windUnit,
                colors = colors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UvTile(
                uvNow = data.uvCurrent.takeIf { it >= 0 } ?: data.todayUv,
                uvDayMax = data.todayUv,
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            PrecipTile(
                amountMm = data.todayRain,
                precipUnit = units.precipUnit,
                colors = colors,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AqiTile(
            aqi = data.aqi,
            colors = colors
        )

        Spacer(modifier = Modifier.height(36.dp))
    }
}
