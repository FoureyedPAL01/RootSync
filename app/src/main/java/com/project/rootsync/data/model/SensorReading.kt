package com.project.rootsync.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sensor reading data class representing a telemetry record.
 * Matches Supabase sensor_readings table schema.
 */
@Serializable
data class SensorReading(
    val id: Long,
    @SerialName("device_id") val deviceId: String,
    @SerialName("soil_moisture") val soilMoisture: Int,
    @SerialName("temperature_c") val temperatureC: Double,
    val humidity: Double,
    @SerialName("rain_detected") val rainDetected: Boolean = false,
    @SerialName("flow_litres") val flowLitres: Double? = null,
    @SerialName("recorded_at") val recordedAt: String,
    @SerialName("created_at") val createdAt: String? = null
) {
    val isRaining: Boolean get() = rainDetected
    val flowRate: Double? get() = flowLitres
}
