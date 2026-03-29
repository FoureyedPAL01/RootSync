package com.project.rootsync.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Device data class representing an ESP32 device linked to the system.
 */
@Serializable
data class Device(
    val id: String,
    @SerialName("user_id") val userId: String? = null,
    val name: String,
    @SerialName("is_online") val isOnline: Boolean,
    @SerialName("last_seen") val lastSeen: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
