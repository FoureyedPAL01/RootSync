package com.project.rootsync.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Pump log data class representing a pump operation record.
 */
@Serializable
data class PumpLog(
    val id: Long? = null,
    @SerialName("device_id") val deviceId: String,
    @SerialName("pump_on_at") val pumpOnAt: String,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    @SerialName("water_used_litres") val waterUsedLitres: Double? = null,
    @SerialName("moisture_before") val moistureBefore: Int? = null,
    @SerialName("moisture_after") val moistureAfter: Int? = null,
    @SerialName("rain_detected") val rainDetected: Boolean? = null,
    @SerialName("trigger_type") val triggerType: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
