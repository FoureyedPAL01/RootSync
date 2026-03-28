package com.project.rootsync.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val hourly: HourlyData
)

data class HourlyData(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val relative_humidity_2m: List<Double>,
    val precipitation: List<Double>,
    val et0_fao_evapotranspiration: List<Double>
)

interface WeatherApiService {

    @GET("v1/forecast")
    suspend fun getWeatherData(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String = "temperature_2m,relative_humidity_2m,precipitation,et0_fao_evapotranspiration",
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse
}
