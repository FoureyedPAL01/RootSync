package com.project.rootsync.data.remote

import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.math.ln

interface WeatherApiService {
    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"
    }

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m,wind_gusts_10m,wind_direction_10m,is_day,uv_index",
        @Query("hourly") hourly: String = "temperature_2m,weather_code,precipitation_probability",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,precipitation_sum,uv_index_max,sunrise,sunset",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 7
    ): com.project.rootsync.data.model.WeatherResponse
}

interface AirQualityApiService {
    companion object {
        const val BASE_URL = "https://air-quality-api.open-meteo.com/"
    }

    @GET("v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "us_aqi",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_hours") forecastHours: Int = 1
    ): com.project.rootsync.data.model.AirQualityResponse
}

/**
 * Unit conversion utilities for weather display.
 */
object WeatherUnitConverter {
    fun formatTemp(tempCelsius: Double, unit: String): String {
        return when (unit.lowercase()) {
            "fahrenheit" -> "${celsiusToFahrenheit(tempCelsius).toInt()}°"
            else -> "${tempCelsius.toInt()}°"
        }
    }

    fun celsiusToFahrenheit(celsius: Double): Double = celsius * 9 / 5 + 32

    fun formatWindKmh(kmh: Double, unit: String): String {
        return when (unit.lowercase()) {
            "mph" -> "${(kmh * 0.621371).toInt()} mph"
            "m/s" -> "${(kmh / 3.6).toInt()} m/s"
            "knots" -> "${(kmh * 0.539957).toInt()} kn"
            else -> "${kmh.toInt()} km/h"
        }
    }

    fun formatPrecipMm(mm: Double, unit: String): String {
        return when (unit.lowercase()) {
            "in" -> "${String.format("%.2f", mm / 25.4)} in"
            else -> "${formatMm(mm)} mm"
        }
    }

    private fun formatMm(mm: Double): String {
        val text = if (mm >= 10) String.format("%.0f", mm) else String.format("%.1f", mm)
        return text.trimEnd('0').trimEnd('.')
    }
}

/**
 * WMO weather code helpers for mapping codes to labels and icons.
 */
object WmoWeatherHelper {
    fun wmoLabel(code: Int): String = when {
        code == 0 -> "Clear Sky"
        code <= 2 -> "Partly Cloudy"
        code == 3 -> "Overcast"
        code <= 48 -> "Foggy"
        code <= 55 -> "Drizzle"
        code <= 65 -> "Rain"
        code <= 75 -> "Snow"
        code <= 82 -> "Rain Showers"
        code <= 99 -> "Thunderstorm"
        else -> "Unknown"
    }

    fun wmoIconName(code: Int, night: Boolean = false): String = when {
        code == 0 -> if (night) "clear_night" else "sunny"
        code <= 2 -> if (night) "mostly_cloudy_night" else "mostly_sunny"
        code == 3 -> "cloudy"
        code <= 48 -> "windy"
        code <= 55 -> "drizzle"
        code <= 65 -> "heavy_rain"
        code <= 75 -> "icy"
        code <= 82 -> "sleet_hail"
        else -> "strong_thunderstorms"
    }
}

/**
 * UV index helpers.
 */
object UvHelper {
    fun uvLabel(uv: Double): String = when {
        uv < 3 -> "Low"
        uv < 6 -> "Moderate"
        uv < 8 -> "High"
        uv < 11 -> "Very High"
        else -> "Extreme"
    }
}

/**
 * Air Quality Index helpers.
 */
object AqiHelper {
    fun aqiLabel(aqi: Int): String = when {
        aqi < 0 -> "No data"
        aqi <= 50 -> "Good"
        aqi <= 100 -> "Moderate"
        aqi <= 150 -> "Unhealthy for sensitive"
        aqi <= 200 -> "Unhealthy"
        aqi <= 300 -> "Very Unhealthy"
        else -> "Hazardous"
    }
}

/**
 * Wind direction helper.
 */
object WindDirectionHelper {
    private val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")

    fun compassLabel(degrees: Int): String {
        val index = ((degrees + 22.5) / 45).toInt() % 8
        return directions[index]
    }
}

/**
 * Date/time helpers.
 */
object WeatherDateHelper {
    private val weekdayNames = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    private val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    private val fullWeekdayNames = listOf(
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    )

    fun dayLabel(isoDate: String): String {
        return try {
            val date = java.time.LocalDate.parse(isoDate.take(10))
            weekdayNames[date.dayOfWeek.value - 1]
        } catch (e: Exception) {
            ""
        }
    }

    fun todayFormatted(): String {
        val now = java.time.LocalDate.now()
        return "${fullWeekdayNames[now.dayOfWeek.value - 1]}, ${monthNames[now.monthValue - 1]} ${now.dayOfMonth}"
    }

    fun hourLabel(isoDateTime: String): String {
        return try {
            val dt = java.time.LocalDateTime.parse(isoDateTime.take(19))
            val hour = dt.hour
            val amPm = if (hour >= 12) "PM" else "AM"
            val hour12 = when {
                hour == 0 -> 12
                hour in 1..11 -> hour
                else -> hour - 12
            }
            "$hour12 $amPm"
        } catch (e: Exception) {
            "--"
        }
    }

    fun nowHourIndex(times: List<String>): Int {
        val now = java.time.LocalDateTime.now()
        val cutoff = now.truncatedTo(java.time.temporal.ChronoUnit.HOURS)

        for (i in times.indices) {
            try {
                val dt = java.time.LocalDateTime.parse(times[i].take(19))
                if (!dt.isBefore(cutoff)) return i
            } catch (_: Exception) {}
        }
        return 0
    }
}

/**
 * Dew point calculation using Magnus formula.
 */
object DewPointCalculator {
    private const val A = 17.27
    private const val B = 237.7

    fun dewPoint(tempC: Double, humidity: Double): Double {
        val alpha = A * tempC / (B + tempC) + ln(humidity / 100.0)
        return B * alpha / (A - alpha)
    }
}
