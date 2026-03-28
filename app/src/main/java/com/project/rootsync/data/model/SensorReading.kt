package com.project.rootsync.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SensorReading(
    val id: String,
    @SerialName("device_id") val deviceId: String,
    @SerialName("soil_moisture") val soilMoisture: Float,
    val temperature: Float,
    val humidity: Float,
    @SerialName("is_raining") val isRaining: Boolean,
    @SerialName("flow_rate") val flowRate: Float? = null,
    @SerialName("created_at") val createdAt: String
)
