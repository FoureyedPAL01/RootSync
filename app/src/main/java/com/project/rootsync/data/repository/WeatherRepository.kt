package com.project.rootsync.data.repository

import com.project.rootsync.data.remote.HourlyData
import com.project.rootsync.data.remote.OpenMeteoApi
import com.project.rootsync.data.remote.WeatherResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor() {

    private val api: OpenMeteoApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenMeteoApi::class.java)
    }

    suspend fun getWeatherData(
        latitude: Double,
        longitude: Double
    ): Result<WeatherResponse> = try {
        val response = api.getWeatherData(
            latitude = latitude,
            longitude = longitude
        )
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getHourlyForecast(
        latitude: Double,
        longitude: Double
    ): Result<HourlyData> = try {
        val response = api.getWeatherData(
            latitude = latitude,
            longitude = longitude
        )
        Result.success(response.hourly)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getCurrentTemperature(
        latitude: Double,
        longitude: Double
    ): Result<Double> = try {
        val response = api.getWeatherData(
            latitude = latitude,
            longitude = longitude
        )
        val currentTemp = response.hourly.temperature_2m.firstOrNull() ?: 0.0
        Result.success(currentTemp)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
