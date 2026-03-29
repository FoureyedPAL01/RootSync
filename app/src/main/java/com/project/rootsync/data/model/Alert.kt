package com.project.rootsync.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * System alert data class representing an alert record.
 */
@Serializable
data class Alert(
    val id: Long,
    @SerialName("device_id") val deviceId: String,
    @SerialName("alert_type") val alertType: String,
    val message: String,
    val resolved: Boolean = false,
    @SerialName("created_at") val createdAt: String
)
