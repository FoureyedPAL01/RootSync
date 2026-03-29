package com.project.rootsync.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Device data class representing an ESP32 device linked to the system.
 * Matches Supabase devices table schema.
 */
@Serializable
data class Device(
    val id: String,
    @SerialName("user_id") val userId: String? = null,
    val name: String,
    val status: String = "offline",
    @SerialName("last_seen") val lastSeen: String? = null,
    @SerialName("crop_profile_id") val cropProfileId: Long? = null,
    @SerialName("claimed_at") val claimedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null
) {
    val isOnline: Boolean get() = status == "online"
}
