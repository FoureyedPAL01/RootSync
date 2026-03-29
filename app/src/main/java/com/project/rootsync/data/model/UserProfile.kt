package com.project.rootsync.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * User profile data class representing user settings and preferences.
 * Matches Supabase users table schema.
 */
@Serializable
data class UserProfile(
    val id: String,
    val username: String? = null,
    val email: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("temp_unit") val tempUnit: String = "celsius",
    @SerialName("volume_unit") val volumeUnit: String = "litres",
    val timezone: String = "UTC",
    @SerialName("pump_alerts") val pumpAlerts: Boolean = true,
    @SerialName("soil_moisture_alerts") val soilMoistureAlerts: Boolean = true,
    @SerialName("weather_alerts") val weatherAlerts: Boolean = true,
    @SerialName("fertigation_reminders") val fertigationReminders: Boolean = true,
    @SerialName("device_offline_alerts") val deviceOfflineAlerts: Boolean = true,
    @SerialName("weekly_summary") val weeklySummary: Boolean = false,
    @SerialName("location_lat") val locationLat: String = "19.0760",
    @SerialName("location_lon") val locationLon: String = "72.8777",
    @SerialName("wind_unit") val windUnit: String = "km/h",
    @SerialName("precipitation_unit") val precipitationUnit: String = "mm",
    @SerialName("aqi_type") val aqiType: String = "us"
)
