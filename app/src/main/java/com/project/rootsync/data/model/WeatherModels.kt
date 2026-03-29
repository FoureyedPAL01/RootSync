package com.project.rootsync.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Weather data models matching the Open-Meteo API response structure.
 * All raw values stored in SI/base units (°C, km/h, mm).
 * Unit conversion happens at render time using user preferences.
 */
@Serializable
data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    @SerialName("current")
    val current: CurrentWeather? = null,
    @SerialName("hourly")
    val hourly: HourlyWeather? = null,
    @SerialName("daily")
    val daily: DailyWeather? = null
)

@Serializable
data class CurrentWeather(
    val time: String,
    @SerialName("temperature_2m")
    val temperature: Double,
    @SerialName("apparent_temperature")
    val apparentTemperature: Double,
    @SerialName("relative_humidity_2m")
    val relativeHumidity: Int,
    @SerialName("weather_code")
    val weatherCode: Int,
    @SerialName("wind_speed_10m")
    val windSpeed: Double,
    @SerialName("wind_gusts_10m")
    val windGusts: Double,
    @SerialName("wind_direction_10m")
    val windDirection: Int,
    @SerialName("is_day")
    val isDay: Int,
    @SerialName("uv_index")
    val uvIndex: Double? = null
)

@Serializable
data class HourlyWeather(
    val time: List<String>,
    @SerialName("temperature_2m")
    val temperature: List<Double>,
    @SerialName("weather_code")
    val weatherCode: List<Int>,
    @SerialName("precipitation_probability")
    val precipitationProbability: List<Int>
)

@Serializable
data class DailyWeather(
    val time: List<String>,
    @SerialName("weather_code")
    val weatherCode: List<Int>,
    @SerialName("temperature_2m_max")
    val temperatureMax: List<Double>,
    @SerialName("temperature_2m_min")
    val temperatureMin: List<Double>,
    @SerialName("precipitation_probability_max")
    val precipitationProbabilityMax: List<Int>,
    @SerialName("precipitation_sum")
    val precipitationSum: List<Double>,
    @SerialName("uv_index_max")
    val uvIndexMax: List<Double>,
    val sunrise: List<String>,
    val sunset: List<String>
)

@Serializable
data class AirQualityResponse(
    val latitude: Double,
    val longitude: Double,
    @SerialName("current")
    val current: AirQualityCurrent? = null
)

@Serializable
data class AirQualityCurrent(
    @SerialName("us_aqi")
    val usAqi: Int
)

/**
 * Parsed weather data for UI consumption.
 */
data class WeatherData(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val weatherCode: Int,
    val windSpeed: Double,
    val windGusts: Double,
    val windDirection: Int,
    val isDay: Boolean,
    val todayMax: Double,
    val todayMin: Double,
    val todayPop: Int,
    val todayRain: Double,
    val uvCurrent: Double,
    val todayUv: Double,
    val sunrise: String,
    val sunset: String,
    val willRain: Boolean,
    val aqi: Int,
    val forecast7: List<DayForecast>,
    val hourlyTimes: List<String>,
    val hourlyTemps: List<Double>,
    val hourlyCodes: List<Int>,
    val hourlyPrecip: List<Int>
)

data class DayForecast(
    val date: String,
    val code: Int,
    val max: Double,
    val min: Double,
    val pop: Int
)

/**
 * User preferences for weather units.
 */
data class WeatherUnits(
    val tempUnit: String = "celsius",
    val windUnit: String = "km/h",
    val precipUnit: String = "mm"
)
