package com.project.rootsync.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CropProfile(
    val id: String,
    @SerialName("device_id") val deviceId: String,
    val name: String,
    @SerialName("moisture_threshold") val moistureThreshold: Int,
    @SerialName("rain_probability_threshold") val rainProbabilityThreshold: Int
)
