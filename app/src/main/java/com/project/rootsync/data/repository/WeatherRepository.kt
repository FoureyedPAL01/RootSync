package com.project.rootsync.data.repository

import com.project.rootsync.data.model.AirQualityResponse
import com.project.rootsync.data.model.DayForecast
import com.project.rootsync.data.model.WeatherData
import com.project.rootsync.data.model.WeatherResponse
import com.project.rootsync.data.remote.AirQualityApiService
import com.project.rootsync.data.remote.WeatherApiService
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ln

/**
 * Repository for fetching weather data from Open-Meteo API.
 * Both APIs are free and don't require an API key.
 */
@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApi: WeatherApiService,
    private val airQualityApi: AirQualityApiService
) {

    /**
     * Fetches weather data and air quality concurrently.
     */
    suspend fun fetchWeatherData(
        latitude: Double,
        longitude: Double
    ): Result<WeatherData> {
        return try {
            val weatherResponse = weatherApi.getForecast(
                latitude = latitude,
                longitude = longitude
            )
            val airQualityResponse = try {
                airQualityApi.getAirQuality(
                    latitude = latitude,
                    longitude = longitude
                )
            } catch (e: Exception) {
                null
            }

            val weatherData = parseWeatherData(weatherResponse, airQualityResponse)
            Result.success(weatherData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Parses raw API responses into UI-friendly WeatherData.
     */
    private fun parseWeatherData(
        fc: WeatherResponse,
        aq: AirQualityResponse?
    ): WeatherData {
        val current = fc.current ?: throw IllegalStateException("No current weather data")
        val daily = fc.daily ?: throw IllegalStateException("No daily weather data")
        val hourly = fc.hourly ?: throw IllegalStateException("No hourly weather data")

        val dTimes = daily.time
        val dCodes = daily.weatherCode
        val dMax = daily.temperatureMax
        val dMin = daily.temperatureMin
        val dPop = daily.precipitationProbabilityMax
        val dRain = daily.precipitationSum
        val dUV = daily.uvIndexMax
        val sunrises = daily.sunrise
        val sunsets = daily.sunset

        val hTimes = hourly.time
        val hTemps = hourly.temperature
        val hCodes = hourly.weatherCode
        val hPrecp = hourly.precipitationProbability

        val todayPop = dPop.firstOrNull() ?: 0

        return WeatherData(
            temperature = current.temperature,
            feelsLike = current.apparentTemperature,
            humidity = current.relativeHumidity,
            weatherCode = current.weatherCode,
            windSpeed = current.windSpeed,
            windGusts = current.windGusts,
            windDirection = current.windDirection,
            isDay = current.isDay == 1,
            todayMax = dMax.firstOrNull() ?: 0.0,
            todayMin = dMin.firstOrNull() ?: 0.0,
            todayPop = todayPop,
            todayRain = dRain.firstOrNull() ?: 0.0,
            uvCurrent = current.uvIndex ?: -1.0,
            todayUv = dUV.firstOrNull() ?: 0.0,
            sunrise = sunrises.firstOrNull() ?: "",
            sunset = sunsets.firstOrNull() ?: "",
            willRain = todayPop > 50,
            aqi = aq?.current?.usAqi ?: -1,
            forecast7 = dTimes.mapIndexed { i, date ->
                DayForecast(
                    date = date,
                    code = dCodes.getOrElse(i) { 0 },
                    max = dMax.getOrElse(i) { 0.0 },
                    min = dMin.getOrElse(i) { 0.0 },
                    pop = dPop.getOrElse(i) { 0 }
                )
            },
            hourlyTimes = hTimes,
            hourlyTemps = hTemps,
            hourlyCodes = hCodes,
            hourlyPrecip = hPrecp
        )
    }
}
