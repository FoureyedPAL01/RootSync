package com.project.rootsync.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PumpLog(
    val id: String,
    @SerialName("device_id") val deviceId: String,
    @SerialName("started_at") val startedAt: String,
    @SerialName("ended_at") val endedAt: String? = null,
    @SerialName("water_used_liters") val waterUsedLiters: Float? = null,
    val duration: Int? = null
)
