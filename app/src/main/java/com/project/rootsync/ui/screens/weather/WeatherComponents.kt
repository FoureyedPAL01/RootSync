package com.project.rootsync.ui.screens.weather

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.rootsync.data.model.DayForecast
import com.project.rootsync.data.remote.AqiHelper
import com.project.rootsync.data.remote.DewPointCalculator
import com.project.rootsync.data.remote.UvHelper
import com.project.rootsync.data.remote.WeatherDateHelper
import com.project.rootsync.data.remote.WeatherUnitConverter
import com.project.rootsync.data.remote.WindDirectionHelper
import com.project.rootsync.data.remote.WmoWeatherHelper

// ============================
// 1 — Hero card
// ============================

@Composable
fun HeroCard(
    date: String,
    temp: String,
    maxTemp: String,
    minTemp: String,
    feels: String,
    condition: String,
    weatherCode: Int,
    isDay: Boolean,
    willRain: Boolean,
    colors: androidx.compose.material3.ColorScheme,
    modifier: Modifier = Modifier
) {
    val gradientColors = if (isDay) {
        listOf(Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4))
    } else {
        listOf(Color(0xFF1A237E), Color(0xFF311B92), Color(0xFF4A148C))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(gradientColors),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Weather icon placeholder
                Text(
                    text = getWeatherEmoji(weatherCode, isDay),
                    fontSize = 64.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = temp,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Light
                    ),
                    color = Color.White
                )

                Text(
                    text = condition,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "H: $maxTemp",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "L: $minTemp",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Feels like $feels",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )

                if (willRain) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Rain expected",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

private fun getWeatherEmoji(code: Int, isDay: Boolean): String = when {
    code == 0 -> if (isDay) "☀️" else "🌙"
    code <= 2 -> if (isDay) "⛅" else "☁️"
    code == 3 -> "☁️"
    code <= 48 -> "🌫️"
    code <= 55 -> "🌧️"
    code <= 65 -> "🌧️"
    code <= 75 -> "❄️"
    code <= 82 -> "🌦️"
    else -> "⛈️"
}

// ============================
// 2 — Hourly strip
// ============================

@Composable
fun HourlyStrip(
    times: List<String>,
    temps: List<Double>,
    codes: List<Int>,
    precipitations: List<Int>,
    startIndex: Int,
    sunrise: String,
    sunset: String,
    tempUnit: String,
    colors: androidx.compose.material3.ColorScheme,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = colors.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "HOURLY FORECAST",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Mini sparkline
        if (temps.isNotEmpty()) {
            val displayTemps = temps.drop(startIndex).take(24)
            if (displayTemps.isNotEmpty()) {
                MiniSparkLine(
                    temps = displayTemps,
                    color = colors.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal scroll
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            val endIndex = minOf(startIndex + 24, times.size)
            val displayTimes = times.subList(startIndex, endIndex)
            val displayTemps = temps.subList(startIndex, endIndex)
            val displayCodes = codes.subList(startIndex, endIndex)
            val displayPrecip = precipitations.subList(startIndex, endIndex)

            itemsIndexed(displayTimes) { _, time ->
                HourChip(
                    time = WeatherDateHelper.hourLabel(time),
                    temp = WeatherUnitConverter.formatTemp(displayTemps.getOrElse(0) { 0.0 }, tempUnit),
                    emoji = getWeatherEmoji(displayCodes.getOrElse(0) { 0 }, true),
                    pop = displayPrecip.getOrElse(0) { 0 },
                    isNow = time == displayTimes.first(),
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun HourChip(
    time: String,
    temp: String,
    emoji: String,
    pop: Int,
    isNow: Boolean,
    colors: androidx.compose.material3.ColorScheme
) {
    Column(
        modifier = Modifier
            .width(64.dp)
            .background(
                color = if (isNow) colors.primary.copy(alpha = 0.1f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isNow) "Now" else time,
            style = MaterialTheme.typography.labelSmall,
            color = if (isNow) colors.primary else colors.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = temp,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal
        )
        if (pop > 0) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${pop}%",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF2196F3)
            )
        }
    }
}

@Composable
private fun MiniSparkLine(
    temps: List<Double>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (temps.size < 2) return@Canvas

        val minTemp = temps.min()
        val maxTemp = temps.max()
        val range = (maxTemp - minTemp).coerceAtLeast(1.0)

        val stepX = size.width / (temps.size - 1)
        val path = Path()

        temps.forEachIndexed { index, temp ->
            val x = index * stepX
            val y = size.height - ((temp - minTemp) / range * size.height).toFloat()

            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

// ============================
// 3 — Daily forecast
// ============================

@Composable
fun DailyForecastSection(
    forecast: List<DayForecast>,
    tempUnit: String,
    colors: androidx.compose.material3.ColorScheme,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = colors.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "7-DAY FORECAST",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                forecast.forEachIndexed { index, day ->
                    DayRow(
                        day = day,
                        tempUnit = tempUnit,
                        colors = colors,
                        isToday = index == 0
                    )
                    if (index < forecast.size - 1) {
                        androidx.compose.material3.HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = colors.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayRow(
    day: DayForecast,
    tempUnit: String,
    colors: androidx.compose.material3.ColorScheme,
    isToday: Boolean
) {
    val minNorm = (day.min - day.min) / ((day.max - day.min).coerceAtLeast(1.0))
    val maxNorm = 1.0

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day label
        Text(
            text = if (isToday) "Today" else WeatherDateHelper.dayLabel(day.date),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.width(52.dp)
        )

        // Pop
        if (day.pop > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.width(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Color(0xFF2196F3)
                )
                Text(
                    text = "${day.pop}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2196F3)
                )
            }
        } else {
            Spacer(modifier = Modifier.width(40.dp))
        }

        // Emoji
        Text(
            text = getWeatherEmoji(day.code, true),
            fontSize = 20.sp,
            modifier = Modifier.width(32.dp)
        )

        // Min temp
        Text(
            text = WeatherUnitConverter.formatTemp(day.min, tempUnit),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.width(40.dp)
        )

        // Temperature bar
        TemperatureBar(
            minNorm = minNorm.toFloat(),
            maxNorm = maxNorm.toFloat(),
            color = colors.primary,
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .padding(horizontal = 8.dp)
        )

        // Max temp
        Text(
            text = WeatherUnitConverter.formatTemp(day.max, tempUnit),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(40.dp)
        )
    }
}

@Composable
private fun TemperatureBar(
    minNorm: Float,
    maxNorm: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val startX = minNorm * size.width
        val endX = maxNorm * size.width

        drawRoundRect(
            color = color.copy(alpha = 0.2f),
            topLeft = Offset(0f, 0f),
            size = androidx.compose.ui.geometry.Size(size.width, size.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
        )

        drawRoundRect(
            color = color,
            topLeft = Offset(startX, 0f),
            size = androidx.compose.ui.geometry.Size((endX - startX).coerceAtLeast(0f), size.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
        )
    }
}

// ============================
// 4 — Condition tiles
// ============================

@Composable
fun HumidityTile(
    humidity: Int,
    tempC: Double,
    colors: androidx.compose.material3.ColorScheme,
    modifier: Modifier = Modifier
) {
    val dewPoint = DewPointCalculator.dewPoint(tempC, humidity.toDouble())

    ConditionTile(
        title = "HUMIDITY",
        value = "$humidity%",
        subtitle = "Dew point ${WeatherUnitConverter.formatTemp(dewPoint, "celsius")}",
        icon = {
            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(20.dp)
            )
        },
        colors = colors,
        modifier = modifier
    )
}

@Composable
fun WindTile(
    speedKmh: Double,
    gustsKmh: Double,
    directionDeg: Int,
    windUnit: String,
    colors: androidx.compose.material3.ColorScheme,
    modifier: Modifier = Modifier
) {
    ConditionTile(
        title = "WIND",
        value = WeatherUnitConverter.formatWindKmh(speedKmh, windUnit),
        subtitle = "Gusts ${WeatherUnitConverter.formatWindKmh(gustsKmh, windUnit)} ${WindDirectionHelper.compassLabel(directionDeg)}",
        icon = {
            Icon(
                imageVector = Icons.Default.Air,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
        },
        colors = colors,
        modifier = modifier
    )
}

@Composable
fun UvTile(
    uvNow: Double,
    uvDayMax: Double,
    colors: androidx.compose.material3.ColorScheme,
    modifier: Modifier = Modifier
) {
    ConditionTile(
        title = "UV INDEX",
        value = "${uvNow.toInt()} ${UvHelper.uvLabel(uvNow)}",
        subtitle = "Today's max ${uvDayMax.toInt()}",
        icon = {
            Icon(
                imageVector = Icons.Default.BrightnessHigh,
                contentDescription = null,
                tint = Color(0xFFFFA000),
                modifier = Modifier.size(20.dp)
            )
        },
        colors = colors,
        modifier = modifier
    )
}

@Composable
fun PrecipTile(
    amountMm: Double,
    precipUnit: String,
    colors: androidx.compose.material3.ColorScheme,
    modifier: Modifier = Modifier
) {
    ConditionTile(
        title = "PRECIPITATION",
        value = WeatherUnitConverter.formatPrecipMm(amountMm, precipUnit),
        subtitle = "Last 24 hrs",
        icon = {
            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(20.dp)
            )
        },
        colors = colors,
        modifier = modifier
    )
}

@Composable
fun AqiTile(
    aqi: Int,
    colors: androidx.compose.material3.ColorScheme,
    modifier: Modifier = Modifier
) {
    val aqiColor = when {
        aqi < 0 -> colors.onSurfaceVariant
        aqi <= 50 -> Color(0xFF4CAF50)
        aqi <= 100 -> Color(0xFFFFC107)
        aqi <= 150 -> Color(0xFFFF9800)
        aqi <= 200 -> Color(0xFFF44336)
        aqi <= 300 -> Color(0xFF9C27B0)
        else -> Color(0xFF7B1FA2)
    }

    ConditionTile(
        title = "AIR QUALITY",
        value = if (aqi >= 0) "AQI $aqi" else "--",
        subtitle = AqiHelper.aqiLabel(aqi),
        valueColor = aqiColor,
        icon = {
            Icon(
                imageVector = Icons.Default.Thunderstorm,
                contentDescription = null,
                tint = aqiColor,
                modifier = Modifier.size(20.dp)
            )
        },
        colors = colors,
        modifier = modifier
    )
}

@Composable
private fun ConditionTile(
    title: String,
    value: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    colors: androidx.compose.material3.ColorScheme,
    valueColor: Color = colors.onSurface,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}
